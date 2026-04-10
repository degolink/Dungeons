package com.dungeons_and_dragons.rol.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dungeons_and_dragons.rol.model.Batalla;
import com.dungeons_and_dragons.rol.model.Condicion;
import com.dungeons_and_dragons.rol.model.Hechizo;
import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.model.Villano;
import com.dungeons_and_dragons.rol.repository.BatallaRepository;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;

@Service
public class BatallaService {

    private final BatallaRepository batallaRepository;
    private final PersonajeRepository personajeRepository;
    private final DadosService dadosService;

    public BatallaService(BatallaRepository batallaRepository, PersonajeRepository personajeRepository,
            DadosService dadosService) {
        this.batallaRepository = batallaRepository;
        this.personajeRepository = personajeRepository;
        this.dadosService = dadosService;
    }

    public Batalla obtenerOCrearBatallaDemo() {
        return batallaRepository.findAll().stream().findFirst().orElseGet(this::crearBatallaDemo);
    }

    public Batalla buscarPorId(Long id) {
        return batallaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Batalla no encontrada"));
    }

    public Optional<Batalla> buscarActivaPorPersonaje(Long personajeId) {
        return batallaRepository.findAll().stream()
                .filter(this::estaDisponibleParaJugador)
                .filter(batalla -> batalla.getPersonajes().stream()
                        .anyMatch(personaje -> personajeId.equals(personaje.getId())))
                .max(Comparator.comparing(Batalla::getId));
    }

    public Optional<Batalla> buscarActivaPorVillano(Long villanoId) {
        return batallaRepository.findAll().stream()
                .filter(this::estaDisponibleParaJugador)
                .filter(batalla -> batalla.getVillanos().stream()
                        .anyMatch(villano -> villanoId.equals(villano.getId())))
                .max(Comparator.comparing(Batalla::getId));
    }

    public Map<String, Object> atacarDesdeJugador(Long personajeId, Long objetivoId) {
        Batalla batalla = buscarBatallaActivaObligatoria(personajeId);
        return atacar(batalla.getId(), "personaje", personajeId, "villano", objetivoId);
    }

    public Map<String, Object> lanzarHechizoDesdeJugador(Long personajeId, Long hechizoId, Long objetivoId) {
        Batalla batalla = buscarBatallaActivaObligatoria(personajeId);
        Personaje personaje = buscarPersonajeEnBatalla(batalla, personajeId);
        validarTurnoJugador(batalla, personaje);

        Hechizo hechizo = personaje.getHechizos().stream()
                .filter(item -> hechizoId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hechizo no encontrado"));

        int energiaActual = Math.max(personaje.getPuntosEnergia(), 0);
        int costeEnergia = Math.max(1, hechizo.getNivel());
        if (energiaActual < costeEnergia) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay suficiente energía para lanzar este hechizo");
        }

        personaje.setPuntosEnergia(energiaActual - costeEnergia);

        Map<String, Object> response = new LinkedHashMap<>();
        String evento;

        if (esHechizoCuracion(hechizo)) {
            int curacion = Math.max(1, Math.max(hechizo.getNivel(), hechizo.getDaño()));
            int vidaNueva = Math.min(personaje.getPuntosVidaMax(), personaje.getPuntosVida() + curacion);
            int vidaRecuperada = vidaNueva - personaje.getPuntosVida();
            personaje.setPuntosVida(vidaNueva);
            String detalleCondiciones = aplicarCondiciones(personaje, hechizo);
            evento = personaje.getNombre() + " lanzó " + hechizo.getNombre() + " y recuperó " + vidaRecuperada
                    + " puntos de vida." + detalleCondiciones;
            response.put("vidaJugador", vidaNueva);
        } else {
            if (objetivoId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un objetivo para este hechizo");
            }

            Villano objetivo = buscarVillanoEnBatalla(batalla, objetivoId);
            if (objetivo.isDerrotado()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ese enemigo ya fue derrotado");
            }

            int modificador = Math.max(0, dadosService.calcularModificador(personaje.getInteligencia()));
            int baseDanio = hechizo.getDaño() > 0 ? hechizo.getDaño() : Math.max(1, hechizo.getNivel() + modificador);
            int danio = Math.max(1, baseDanio + dadosService.lanzarDado(4) - 1);
            int vidaRestante = Math.max(0, objetivo.getPuntosVida() - danio);
            objetivo.setPuntosVida(vidaRestante);
            objetivo.setDerrotado(vidaRestante <= 0);

            String detalleCondiciones = aplicarCondiciones(objetivo, hechizo);
            evento = personaje.getNombre() + " lanzó " + hechizo.getNombre() + " sobre " + objetivo.getNombre()
                    + " e infligió " + danio + " de daño." + detalleCondiciones;
            response.put("objetivoId", objetivo.getId());
            response.put("objetivoTipo", "villano");
            response.put("vidaRestante", vidaRestante);
            response.put("derrotado", vidaRestante <= 0);
        }

        if (hechizo.getCondiciones() != null && !hechizo.getCondiciones().isEmpty()) {
            response.put("condicionesAplicadas", hechizo.getCondiciones().stream().map(Condicion::getNombre).toList());
        }

        batalla.getLogCombate().add(0, evento);
        actualizarEstadoBatalla(batalla);
        personajeRepository.save(personaje);
        Batalla guardada = batallaRepository.save(batalla);

        response.put("evento", evento);
        response.put("energiaRestante", personaje.getPuntosEnergia());
        response.put("costeEnergia", costeEnergia);
        response.put("nombre", hechizo.getNombre());
        response.put("estadoBatalla", guardada.getEstado());
        response.put("rondaActual", guardada.getRondaActual());
        return response;
    }

    public Map<String, Object> finalizarTurnoJugador(Long personajeId) {
        Batalla batalla = buscarBatallaActivaObligatoria(personajeId);
        Personaje personaje = buscarPersonajeEnBatalla(batalla, personajeId);
        validarTurnoJugador(batalla, personaje);

        Batalla actualizada = siguienteTurno(batalla.getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", actualizada.getId());
        response.put("estado", actualizada.getEstado());
        response.put("rondaActual", actualizada.getRondaActual());
        response.put("mensaje", "Turno finalizado");
        return response;
    }

    public Map<String, Object> atacarDesdeVillano(Long villanoId, Long objetivoId) {
        Batalla batalla = buscarBatallaActivaObligatoriaVillano(villanoId);
        return atacar(batalla.getId(), "villano", villanoId, "personaje", objetivoId);
    }

    public Map<String, Object> lanzarHechizoDesdeVillano(Long villanoId, Long hechizoId, Long objetivoId) {
        Batalla batalla = buscarBatallaActivaObligatoriaVillano(villanoId);
        Villano villano = buscarVillanoEnBatalla(batalla, villanoId);
        validarTurnoVillano(batalla, villano);

        Hechizo hechizo = villano.getHechizos().stream()
                .filter(item -> hechizoId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hechizo no encontrado"));

        int energiaActual = Math.max(villano.getPuntosEnergia(), 0);
        int costeEnergia = Math.max(1, hechizo.getNivel());
        if (energiaActual < costeEnergia) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay suficiente energía para lanzar este hechizo");
        }

        villano.setPuntosEnergia(energiaActual - costeEnergia);

        Map<String, Object> response = new LinkedHashMap<>();
        String evento;

        if (esHechizoCuracion(hechizo)) {
            int curacion = Math.max(1, Math.max(hechizo.getNivel(), hechizo.getDaño()));
            int vidaNueva = Math.min(villano.getPuntosVidaMax(), villano.getPuntosVida() + curacion);
            int vidaRecuperada = vidaNueva - villano.getPuntosVida();
            villano.setPuntosVida(vidaNueva);
            String detalleCondiciones = aplicarCondiciones(villano, hechizo);
            evento = villano.getNombre() + " lanzó " + hechizo.getNombre() + " y recuperó " + vidaRecuperada
                    + " puntos de vida." + detalleCondiciones;
            response.put("vidaVillano", vidaNueva);
        } else {
            if (objetivoId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un objetivo para este hechizo");
            }

            Personaje objetivo = buscarPersonajeEnBatalla(batalla, objetivoId);
            if (objetivo.isDerrotado()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ese personaje ya fue derrotado");
            }

            int modificador = Math.max(0, dadosService.calcularModificador(villano.getInteligencia()));
            int baseDanio = hechizo.getDaño() > 0 ? hechizo.getDaño() : Math.max(1, hechizo.getNivel() + modificador);
            int danio = Math.max(1, baseDanio + dadosService.lanzarDado(4) - 1);
            int vidaRestante = Math.max(0, objetivo.getPuntosVida() - danio);
            objetivo.setPuntosVida(vidaRestante);
            objetivo.setDerrotado(vidaRestante <= 0);

            String detalleCondiciones = aplicarCondiciones(objetivo, hechizo);
            evento = villano.getNombre() + " lanzó " + hechizo.getNombre() + " sobre " + objetivo.getNombre()
                    + " e infligió " + danio + " de daño." + detalleCondiciones;
            response.put("objetivoId", objetivo.getId());
            response.put("objetivoTipo", "personaje");
            response.put("vidaRestante", vidaRestante);
            response.put("derrotado", vidaRestante <= 0);
        }

        if (hechizo.getCondiciones() != null && !hechizo.getCondiciones().isEmpty()) {
            response.put("condicionesAplicadas", hechizo.getCondiciones().stream().map(Condicion::getNombre).toList());
        }

        batalla.getLogCombate().add(0, evento);
        actualizarEstadoBatalla(batalla);
        Batalla guardada = batallaRepository.save(batalla);

        response.put("evento", evento);
        response.put("energiaRestante", villano.getPuntosEnergia());
        response.put("costeEnergia", costeEnergia);
        response.put("nombre", hechizo.getNombre());
        response.put("estadoBatalla", guardada.getEstado());
        response.put("rondaActual", guardada.getRondaActual());
        return response;
    }

    public Map<String, Object> finalizarTurnoVillano(Long villanoId) {
        Batalla batalla = buscarBatallaActivaObligatoriaVillano(villanoId);
        Villano villano = buscarVillanoEnBatalla(batalla, villanoId);
        validarTurnoVillano(batalla, villano);

        Batalla actualizada = siguienteTurno(batalla.getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", actualizada.getId());
        response.put("estado", actualizada.getEstado());
        response.put("rondaActual", actualizada.getRondaActual());
        response.put("mensaje", "Turno finalizado");
        return response;
    }

    public Batalla iniciarBatalla(Long id) {
        Batalla batalla = buscarPorId(id);
        List<ParticipanteTurno> turnos = new ArrayList<>();

        for (Personaje personaje : batalla.getPersonajes()) {
            personaje.setEnBatalla(true);
            personaje.setTurnoActual(false);
            personaje.setDerrotado(personaje.getPuntosVida() <= 0);
            personaje.setIniciativaActual(dadosService.calcularIniciativa(personaje));
            turnos.add(new ParticipanteTurno("personaje", personaje.getId(), personaje.getIniciativaActual(), personaje.getNombre(), 0));
        }

        for (Villano villano : batalla.getVillanos()) {
            villano.setEnBatalla(true);
            villano.setTurnoActual(false);
            villano.setDerrotado(villano.getPuntosVida() <= 0);
            int iniciativa = dadosService.lanzarDado(20) + dadosService.calcularModificador(villano.getDestreza());
            villano.setIniciativaActual(iniciativa);
            turnos.add(new ParticipanteTurno("villano", villano.getId(), iniciativa, villano.getNombre(), 0));
        }

        turnos.sort(Comparator.comparingInt(ParticipanteTurno::iniciativa).reversed()
                .thenComparing(ParticipanteTurno::nombre));

        for (int i = 0; i < turnos.size(); i++) {
            ParticipanteTurno turno = turnos.get(i);
            asignarOrdenYTurno(batalla, turno, i + 1, i == 0);
        }

        batalla.setEstado("EN_CURSO");
        batalla.setRondaActual(1);
        batalla.setIndiceTurnoActual(0);
        batalla.getLogCombate().add(0, "La batalla ha comenzado. Orden de iniciativa calculado.");
        return batallaRepository.save(batalla);
    }

    public Batalla reiniciarBatalla(Long id) {
        Batalla batalla = buscarPorId(id);

        for (Personaje personaje : batalla.getPersonajes()) {
            reiniciarEstadoPersonaje(personaje);
        }

        for (Villano villano : batalla.getVillanos()) {
            reiniciarEstadoVillano(villano);
        }

        batalla.setEstado("PREPARADA");
        batalla.setRondaActual(0);
        batalla.setIndiceTurnoActual(0);
        batalla.getLogCombate().clear();
        batalla.getLogCombate().add("La batalla se reinició. Pulsa 'Iniciar batalla' para comenzar de nuevo.");

        personajeRepository.saveAll(batalla.getPersonajes());
        return batallaRepository.save(batalla);
    }

    public Batalla siguienteTurno(Long id) {
        Batalla batalla = buscarPorId(id);
        List<ParticipanteTurno> orden = obtenerOrdenActiva(batalla);
        if (orden.isEmpty()) {
            return batalla;
        }

        Integer indiceActual = batalla.getIndiceTurnoActual();
        int actual = indiceActual != null ? indiceActual : 0;
        limpiarTurnos(batalla);

        int siguiente = (actual + 1) % orden.size();
        if (siguiente == 0) {
            batalla.setRondaActual(batalla.getRondaActual() + 1);
        }

        ParticipanteTurno turno = orden.get(siguiente);
        asignarOrdenYTurno(batalla, turno, turno.orden(), true);
        batalla.setIndiceTurnoActual(siguiente);
        batalla.getLogCombate().add(0, "Turno de " + turno.nombre() + ".");
        return batallaRepository.save(batalla);
    }

    public Map<String, Object> atacar(Long batallaId, String atacanteTipo, Long atacanteId, String objetivoTipo, Long objetivoId) {
        Batalla batalla = buscarPorId(batallaId);

        ParticipanteBatalla atacante = buscarParticipante(batalla, atacanteTipo, atacanteId);
        ParticipanteBatalla objetivo = buscarParticipante(batalla, objetivoTipo, objetivoId);
        validarAccionDeCombate(batalla, atacante, objetivo);

        int tirada = dadosService.lanzarDado(20);
        int modificador = dadosService.calcularModificador(atacante.destreza());
        int danio = Math.max(1, dadosService.lanzarDado(8) + modificador);
        boolean critico = tirada == 20;
        if (critico) {
            danio *= 2;
        }

        int vidaRestante = Math.max(0, objetivo.vidaActual() - danio);
        objetivo.aplicarVida().accept(vidaRestante);
        objetivo.aplicarDerrota().accept(vidaRestante <= 0);

        String evento = atacante.nombre() + " atacó a " + objetivo.nombre() + " e infligió " + danio
                + " de daño" + (critico ? " crítico" : "") + ".";
        batalla.getLogCombate().add(0, evento);
        if (vidaRestante <= 0) {
            batalla.getLogCombate().add(0, objetivo.nombre() + " ha sido derrotado.");
        }
        actualizarEstadoBatalla(batalla);
        Batalla actualizada = batallaRepository.save(batalla);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("evento", evento);
        response.put("objetivoTipo", objetivo.tipo());
        response.put("objetivoId", objetivo.id());
        response.put("vidaRestante", vidaRestante);
        response.put("derrotado", vidaRestante <= 0);
        response.put("danio", danio);
        response.put("critico", critico);
        response.put("estadoBatalla", actualizada.getEstado());
        response.put("rondaActual", actualizada.getRondaActual());
        return response;
    }

    private Batalla crearBatallaDemo() {
        List<Personaje> personajes = new ArrayList<>(personajeRepository.findAll());
        if (personajes.isEmpty()) {
            Personaje heroe = new Personaje();
            heroe.setNombre("Aventurero Demo");
            heroe.setClase("Guerrero");
            heroe.setNivel(1);
            heroe.setFuerza(14);
            heroe.setDestreza(12);
            heroe.setConstitucion(12);
            heroe.setInteligencia(10);
            heroe.setSabiduria(10);
            heroe.setCarisma(10);
            heroe.setPuntosVida(18);
            heroe.setPuntosVidaMax(18);
            heroe.setPuntosEnergia(4);
            personajes.add(personajeRepository.save(heroe));
        }

        Batalla batalla = new Batalla();
        batalla.setNombre("Convergencia de la Torre Carmesí");
        batalla.setEstado("PREPARADA");
        batalla.setRondaActual(0);
        batalla.setPersonajes(new ArrayList<>(personajes.subList(0, Math.min(4, personajes.size()))));
        batalla.getVillanos().add(crearArchimagoOscuro());
        batalla.getVillanos().add(crearSenorDeLaGuerra());
        batalla.getLogCombate().add("El Archimago Oscuro y el Señor de la Guerra aguardan. Pulsa 'Iniciar batalla' para tirar iniciativa.");
        return batallaRepository.save(batalla);
    }

    private Villano crearArchimagoOscuro() {
        return crearVillano(
                "Archimago Oscuro",
                "control + daño mágico",
                "/img/Archimago%20Oscuro.png",
                20,
                9,
                14,
                16,
                20,
                16,
                18,
                170,
                220,
                List.of(
                        crearHechizo("Fire Bolt", 3, "ataque", "Un rayo de fuego comprimido impacta con violencia arcana.", 18, 0, false),
                        crearHechizo("Fireball", 5, "ataque", "Una explosión infernal arrasa a su objetivo.", 28, 0, false),
                        crearHechizo("Chain Lightning", 6, "ataque", "Rayos encadenados recorren el campo de batalla.", 32, 0, false),
                        crearHechizoConCondicion("Dominate Person", 5, "utilidad", "Somete la mente del enemigo a su voluntad.", 14, 2, true,
                                "Dominado", "El objetivo queda manipulado por la magia oscura.", 2),
                        crearHechizo("Finger of Death", 7, "ataque", "Una descarga mortal de energía necrótica.", 40, 0, false)));
    }

    private Villano crearSenorDeLaGuerra() {
        return crearVillano(
                "Señor de la Guerra",
                "presión física + frontline",
                "/img/Se%C3%B1or%20de%20la%20Guerra.jpeg",
                20,
                22,
                14,
                20,
                12,
                14,
                18,
                240,
                140,
                List.of(
                        crearHechizo("Espada gigante", 4, "ataque", "Tres golpes brutales con una hoja descomunal.", 20, 0, false),
                        crearHechizo("Ataque múltiple", 5, "ataque", "Una cadena de embestidas mantiene la presión sobre el frente.", 24, 0, false),
                        crearHechizo("Golpe devastador", 6, "ataque", "Un barrido demoledor que aplasta todo a su paso.", 30, 0, false),
                        crearHechizoConCondicion("Presencia dominante", 3, "ataque", "Su mera presencia infunde terror en el enemigo.", 12, 2, false,
                                "Asustado", "El objetivo vacila ante la autoridad aplastante del caudillo.", 2),
                        crearHechizo("Ataque pesado", 4, "ataque", "Un tajo cargado con toda su fuerza marcial.", 22, 0, false)));
    }

    private Villano crearVillano(String nombre, String tipo, String imagen, int nivel, int fuerza, int destreza,
            int constitucion, int inteligencia, int sabiduria, int carisma, int vida, int energia,
            List<Hechizo> hechizos) {
        Villano villano = new Villano();
        villano.setNombre(nombre);
        villano.setTipo(tipo);
        villano.setImagen(imagen);
        villano.setNivel(nivel);
        villano.setPuntosVida(vida);
        villano.setPuntosVidaMax(vida);
        villano.setPuntosEnergia(Math.max(0, energia));
        villano.setFuerza(fuerza);
        villano.setDestreza(destreza);
        villano.setConstitucion(constitucion);
        villano.setInteligencia(inteligencia);
        villano.setSabiduria(sabiduria);
        villano.setCarisma(carisma);
        villano.getHechizos().addAll(hechizos);
        return villano;
    }

    private Hechizo crearHechizoConCondicion(String nombre, int nivel, String tipo, String descripcion, int danio,
            int duracion, boolean requiereConcentracion, String condicionNombre, String condicionDescripcion,
            int condicionDuracion) {
        Hechizo hechizo = crearHechizo(nombre, nivel, tipo, descripcion, danio, duracion, requiereConcentracion);
        Condicion condicion = new Condicion();
        condicion.setNombre(condicionNombre);
        condicion.setDescripcion(condicionDescripcion);
        condicion.setDuracion(condicionDuracion);
        condicion.setHechizoOrigen(hechizo);
        hechizo.getCondiciones().add(condicion);
        return hechizo;
    }

    private Hechizo crearHechizo(String nombre, int nivel, String tipo, String descripcion, int danio, int duracion,
            boolean requiereConcentracion) {
        Hechizo hechizo = new Hechizo();
        hechizo.setNombre(nombre);
        hechizo.setNivel(nivel);
        hechizo.setTipo(tipo);
        hechizo.setDescripcion(descripcion);
        hechizo.setDaño(danio);
        hechizo.setDuracion(duracion);
        hechizo.setRequiereConcentracion(requiereConcentracion);
        return hechizo;
    }

    private List<ParticipanteTurno> obtenerOrdenActiva(Batalla batalla) {
        List<ParticipanteTurno> orden = new ArrayList<>();
        for (Personaje personaje : batalla.getPersonajes()) {
            if (!personaje.isDerrotado()) {
                Integer ordenActual = personaje.getOrdenTurno();
                int ordenPersonaje = ordenActual != null ? ordenActual.intValue() : 0;
                orden.add(new ParticipanteTurno("personaje", personaje.getId(), personaje.getIniciativaActual(),
                        personaje.getNombre(), ordenPersonaje));
            }
        }
        for (Villano villano : batalla.getVillanos()) {
            if (!villano.isDerrotado()) {
                Integer ordenActual = villano.getOrdenTurno();
                int ordenVillano = ordenActual != null ? ordenActual.intValue() : 0;
                orden.add(new ParticipanteTurno("villano", villano.getId(), villano.getIniciativaActual(),
                        villano.getNombre(), ordenVillano));
            }
        }
        orden.sort(Comparator.comparingInt(ParticipanteTurno::orden));
        return orden;
    }

    private void reiniciarEstadoPersonaje(Personaje personaje) {
        personaje.setPuntosVida(personaje.getPuntosVidaMax());
        personaje.setPuntosEnergia(Math.max(personaje.getPuntosEnergia(), Math.max(4, personaje.getNivel() * 2 + 2)));
        personaje.setIniciativaActual(0);
        personaje.setEnBatalla(false);
        personaje.setTurnoActual(false);
        personaje.setDerrotado(false);
        personaje.setOrdenTurno(0);
    }

    private void reiniciarEstadoVillano(Villano villano) {
        villano.setPuntosVida(villano.getPuntosVidaMax());
        villano.setPuntosEnergia(Math.max(villano.getPuntosEnergia(), Math.max(2, villano.getNivel() * 2)));
        villano.setIniciativaActual(0);
        villano.setEnBatalla(false);
        villano.setTurnoActual(false);
        villano.setDerrotado(false);
        villano.setOrdenTurno(0);
    }

    private void limpiarTurnos(Batalla batalla) {
        batalla.getPersonajes().forEach(personaje -> personaje.setTurnoActual(false));
        batalla.getVillanos().forEach(villano -> villano.setTurnoActual(false));
    }

    private void asignarOrdenYTurno(Batalla batalla, ParticipanteTurno turno, int orden, boolean turnoActual) {
        if ("personaje".equals(turno.tipo())) {
            batalla.getPersonajes().stream()
                    .filter(personaje -> turno.id().equals(personaje.getId()))
                    .findFirst()
                    .ifPresent(personaje -> {
                        personaje.setOrdenTurno(orden);
                        personaje.setTurnoActual(turnoActual);
                    });
        } else {
            batalla.getVillanos().stream()
                    .filter(villano -> turno.id().equals(villano.getId()))
                    .findFirst()
                    .ifPresent(villano -> {
                        villano.setOrdenTurno(orden);
                        villano.setTurnoActual(turnoActual);
                    });
        }
    }

    private ParticipanteBatalla buscarParticipante(Batalla batalla, String tipo, Long id) {
        if ("personaje".equalsIgnoreCase(tipo)) {
            Personaje personaje = buscarPersonajeEnBatalla(batalla, id);
            return new ParticipanteBatalla("personaje", personaje.getId(), personaje.getNombre(), personaje.getDestreza(),
                    personaje.getPuntosVida(), personaje.isTurnoActual(), personaje.isDerrotado(), personaje::setPuntosVida,
                    personaje::setDerrotado);
        }

        Villano villano = buscarVillanoEnBatalla(batalla, id);
        return new ParticipanteBatalla("villano", villano.getId(), villano.getNombre(), villano.getDestreza(),
                villano.getPuntosVida(), villano.isTurnoActual(), villano.isDerrotado(), villano::setPuntosVida,
                villano::setDerrotado);
    }

    private Batalla buscarBatallaActivaObligatoria(Long personajeId) {
        return buscarActivaPorPersonaje(personajeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El personaje no está en una batalla activa"));
    }

    private Batalla buscarBatallaActivaObligatoriaVillano(Long villanoId) {
        return buscarActivaPorVillano(villanoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El villano no está en una batalla activa"));
    }

    private Personaje buscarPersonajeEnBatalla(Batalla batalla, Long personajeId) {
        return batalla.getPersonajes().stream()
                .filter(item -> personajeId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado en la batalla"));
    }

    private Villano buscarVillanoEnBatalla(Batalla batalla, Long villanoId) {
        return batalla.getVillanos().stream()
                .filter(item -> villanoId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Villano no encontrado en la batalla"));
    }

    private void validarTurnoJugador(Batalla batalla, Personaje personaje) {
        if (!"EN_CURSO".equalsIgnoreCase(batalla.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La batalla no está en curso");
        }
        if (personaje.isDerrotado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El personaje está derrotado");
        }
        if (!personaje.isTurnoActual()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todavía no es el turno de " + personaje.getNombre());
        }
    }

    private void validarTurnoVillano(Batalla batalla, Villano villano) {
        if (!"EN_CURSO".equalsIgnoreCase(batalla.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La batalla no está en curso");
        }
        if (villano.isDerrotado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El villano está derrotado");
        }
        if (!villano.isTurnoActual()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Todavía no es el turno de " + villano.getNombre());
        }
    }

    private void validarAccionDeCombate(Batalla batalla, ParticipanteBatalla atacante, ParticipanteBatalla objetivo) {
        if (!"EN_CURSO".equalsIgnoreCase(batalla.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La batalla no está en curso");
        }
        if (atacante.derrotado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, atacante.nombre() + " no puede actuar porque está derrotado");
        }
        if (objetivo.derrotado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, objetivo.nombre() + " ya fue derrotado");
        }
        if (!atacante.turnoActual()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No es el turno de " + atacante.nombre());
        }
    }

    private void actualizarEstadoBatalla(Batalla batalla) {
        boolean personajesDerrotados = batalla.getPersonajes().stream().allMatch(Personaje::isDerrotado);
        boolean villanosDerrotados = batalla.getVillanos().stream().allMatch(Villano::isDerrotado);

        if (villanosDerrotados) {
            batalla.setEstado("VICTORIA_PERSONAJES");
            limpiarTurnos(batalla);
            if (batalla.getLogCombate().stream().noneMatch(item -> item.contains("Los personajes han ganado"))) {
                batalla.getLogCombate().add(0, "Los personajes han ganado la batalla.");
            }
        } else if (personajesDerrotados) {
            batalla.setEstado("VICTORIA_VILLANOS");
            limpiarTurnos(batalla);
            if (batalla.getLogCombate().stream().noneMatch(item -> item.contains("Los villanos han ganado"))) {
                batalla.getLogCombate().add(0, "Los villanos han ganado la batalla.");
            }
        }
    }

    private boolean estaDisponibleParaJugador(Batalla batalla) {
        String estado = batalla.getEstado() == null ? "PREPARADA" : batalla.getEstado();
        return !"VICTORIA_PERSONAJES".equalsIgnoreCase(estado)
                && !"VICTORIA_VILLANOS".equalsIgnoreCase(estado)
                && !"FINALIZADA".equalsIgnoreCase(estado);
    }

    private String aplicarCondiciones(Personaje objetivo, Hechizo hechizo) {
        if (objetivo.getCondiciones() == null) {
            objetivo.setCondiciones(new ArrayList<>());
        }
        return aplicarCondicionesALista(objetivo.getCondiciones(), hechizo);
    }

    private String aplicarCondiciones(Villano objetivo, Hechizo hechizo) {
        if (objetivo.getCondiciones() == null) {
            objetivo.setCondiciones(new ArrayList<>());
        }
        return aplicarCondicionesALista(objetivo.getCondiciones(), hechizo);
    }

    private String aplicarCondicionesALista(List<Condicion> condicionesObjetivo, Hechizo hechizo) {
        if (hechizo.getCondiciones() == null || hechizo.getCondiciones().isEmpty()) {
            return "";
        }

        List<String> nombresAplicados = new ArrayList<>();
        for (Condicion base : hechizo.getCondiciones()) {
            if (base == null || base.getNombre() == null || base.getNombre().isBlank()) {
                continue;
            }

            Condicion aplicada = new Condicion();
            aplicada.setNombre(base.getNombre().trim());
            aplicada.setDescripcion(base.getDescripcion() == null ? "" : base.getDescripcion().trim());
            aplicada.setDuracion(base.getDuracion() > 0 ? base.getDuracion() : Math.max(0, hechizo.getDuracion()));
            aplicada.setHechizoOrigen(hechizo);
            condicionesObjetivo.add(aplicada);

            String resumen = aplicada.getNombre();
            if (aplicada.getDuracion() > 0) {
                resumen += " (" + aplicada.getDuracion() + " turnos)";
            }
            nombresAplicados.add(resumen);
        }

        if (nombresAplicados.isEmpty()) {
            return "";
        }

        String prefijo = nombresAplicados.size() == 1 ? " la condición " : " las condiciones ";
        return " También aplicó" + prefijo + String.join(", ", nombresAplicados) + ".";
    }

    private boolean esHechizoCuracion(Hechizo hechizo) {
        String tipo = hechizo.getTipo() == null ? "" : hechizo.getTipo().toLowerCase();
        return tipo.contains("cura") || tipo.contains("san") || tipo.contains("heal");
    }

    private record ParticipanteTurno(String tipo, Long id, int iniciativa, String nombre, int orden) {
    }

    @FunctionalInterface
    private interface BooleanSetter {
        void accept(boolean value);
    }

    private record ParticipanteBatalla(String tipo, Long id, String nombre, int destreza, int vidaActual,
            boolean turnoActual, boolean derrotado, java.util.function.IntConsumer aplicarVida,
            BooleanSetter aplicarDerrota) {
    }
}
