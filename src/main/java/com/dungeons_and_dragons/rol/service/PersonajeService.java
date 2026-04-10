package com.dungeons_and_dragons.rol.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dungeons_and_dragons.rol.model.Hechizo;
import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;

@Service
public class PersonajeService {

    private final PersonajeRepository personajeRepository;

    public PersonajeService(PersonajeRepository personajeRepository) {
        this.personajeRepository = personajeRepository;
    }

    // Guardar o actualizar personaje
    public Personaje guardar(Personaje personaje) {
        normalizarCamposBase(personaje);
        return personajeRepository.save(personaje);
    }

    public Personaje prepararNuevoPersonaje(Personaje personaje) {
        if (personaje == null) {
            throw new IllegalArgumentException("El personaje no puede ser nulo");
        }

        if (personaje.getPuntosEnergia() <= 0) {
            personaje.setPuntosEnergia(calcularEnergiaInicial(personaje));
        }

        if (personaje.getPuntosVida() <= 0 && personaje.getPuntosVidaMax() > 0) {
            personaje.setPuntosVida(personaje.getPuntosVidaMax());
        }

        asignarHechizosIniciales(personaje);
        return guardar(personaje);
    }

    public Personaje actualizarEnergia(Personaje personaje, Integer puntosEnergia) {
        if (personaje == null) {
            throw new IllegalArgumentException("El personaje no puede ser nulo");
        }

        personaje.setPuntosEnergia(Math.max(0, puntosEnergia == null ? 0 : puntosEnergia));
        return guardar(personaje);
    }

    // Listar todos los personajes
    public List<Personaje> listar() {
        return personajeRepository.findAll();
    }

    // Buscar por id
    public Optional<Personaje> buscarPorId(Long id) {
        return personajeRepository.findById(id);
    }

    // Borrar personaje
    public void borrar(Long id) {
        personajeRepository.deleteById(id);
    }

    private void normalizarCamposBase(Personaje personaje) {
        if (personaje == null) {
            throw new IllegalArgumentException("El personaje no puede ser nulo");
        }

        personaje.setNivel(Math.max(1, personaje.getNivel()));
        personaje.setPuntosVida(Math.max(0, personaje.getPuntosVida()));
        personaje.setPuntosVidaMax(Math.max(0, personaje.getPuntosVidaMax()));
        personaje.setPuntosEnergia(Math.max(0, personaje.getPuntosEnergia()));

        if (personaje.getCobre() == null) personaje.setCobre(0);
        if (personaje.getPlata() == null) personaje.setPlata(0);
        if (personaje.getOro() == null) personaje.setOro(0);
        if (personaje.getPlatino() == null) personaje.setPlatino(0);
        if (personaje.getHistoria() == null) personaje.setHistoria("");
        if (personaje.getMotivaciones() == null) personaje.setMotivaciones("");
        if (personaje.getFechaCreacion() == null) personaje.setFechaCreacion(LocalDateTime.now());
        personaje.setFechaModificacion(LocalDateTime.now());
    }

    private void asignarHechizosIniciales(Personaje personaje) {
        if (personaje.getHechizos() == null || !personaje.getHechizos().isEmpty()) {
            return;
        }

        int nivelBase = Math.max(1, personaje.getNivel());
        String clase = personaje.getClase() == null ? "" : personaje.getClase().trim().toLowerCase(Locale.ROOT);

        switch (clase) {
            case "guerrero" -> {
                personaje.getHechizos().add(crearHechizo("Golpe Heroico", Math.max(1, nivelBase / 3), "ataque",
                        "Un ataque reforzado por pura disciplina marcial.", 6 + nivelBase / 2, 0, false));
                personaje.getHechizos().add(crearHechizo("Grito de Guerra", 1, "defensa",
                        "Eleva la moral y la resistencia del guerrero.", 3 + nivelBase / 3, 2, false));
            }
            case "pícaro", "picaro" -> {
                personaje.getHechizos().add(crearHechizo("Daga Sombría", Math.max(1, nivelBase / 3), "ataque",
                        "Una cuchillada veloz envuelta en sombras.", 5 + nivelBase / 2, 0, false));
                personaje.getHechizos().add(crearHechizo("Nube de Humo", 1, "utilidad",
                        "Desorienta al enemigo y cubre la retirada.", 4 + nivelBase / 3, 1, false));
            }
            case "clérigo", "clerigo" -> {
                personaje.getHechizos().add(crearHechizo("Luz Sanadora", Math.max(1, nivelBase / 4), "curacion",
                        "Restaura la vitalidad con energía sagrada.", 6 + nivelBase / 2, 0, false));
                personaje.getHechizos().add(crearHechizo("Llama Sagrada", Math.max(1, nivelBase / 3), "ataque",
                        "Una llama divina castiga a los enemigos.", 5 + nivelBase / 2, 0, false));
            }
            case "mago" -> {
                personaje.getHechizos().add(crearHechizo("Bola de Fuego", Math.max(1, nivelBase / 3), "ataque",
                        "Una explosión ígnea arrasa al objetivo.", 7 + nivelBase, 0, false));
                personaje.getHechizos().add(crearHechizo("Escudo Arcano", 1, "defensa",
                        "Una barrera mágica reduce el daño recibido.", 4 + nivelBase / 3, 3, true));
            }
            default -> personaje.getHechizos().add(crearHechizo("Descarga Arcana", 1, "ataque",
                    "Una energía básica útil para cualquier aventurero.", 4 + nivelBase / 2, 0, false));
        }
    }

    private int calcularEnergiaInicial(Personaje personaje) {
        int nivel = Math.max(1, personaje.getNivel());
        String clase = personaje.getClase() == null ? "" : personaje.getClase().trim().toLowerCase(Locale.ROOT);

        return switch (clase) {
            case "mago" -> nivel * 8;
            case "clérigo", "clerigo" -> nivel * 7;
            case "pícaro", "picaro" -> nivel * 5;
            case "guerrero" -> nivel * 4;
            default -> nivel * 5;
        };
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
}