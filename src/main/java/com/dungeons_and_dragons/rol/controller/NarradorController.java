package com.dungeons_and_dragons.rol.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.dungeons_and_dragons.rol.model.Batalla;
import com.dungeons_and_dragons.rol.model.Condicion;
import com.dungeons_and_dragons.rol.model.Hechizo;
import com.dungeons_and_dragons.rol.model.ItemBase;
import com.dungeons_and_dragons.rol.model.ItemInventario;
import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.repository.ItemBaseRepository;
import com.dungeons_and_dragons.rol.repository.ItemInventarioRepository;
import com.dungeons_and_dragons.rol.service.BatallaService;
import com.dungeons_and_dragons.rol.service.DadosService;
import com.dungeons_and_dragons.rol.service.PersonajeService;

@Controller
public class NarradorController {

    private final PersonajeService personajeService;
    private final DadosService dadosService;
    private final ItemInventarioRepository itemInventarioRepository;
    private final ItemBaseRepository itemBaseRepository;
    private final BatallaService batallaService;

    public NarradorController(PersonajeService personajeService, DadosService dadosService,
            ItemInventarioRepository itemInventarioRepository, ItemBaseRepository itemBaseRepository,
            BatallaService batallaService) {
        this.personajeService = personajeService;
        this.dadosService = dadosService;
        this.itemInventarioRepository = itemInventarioRepository;
        this.itemBaseRepository = itemBaseRepository;
        this.batallaService = batallaService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/narrador";
    }

    @GetMapping("/narrador")
    public String mostrarPantalla(Model model) {
        List<Personaje> personajes = personajeService.listar();
        model.addAttribute("personajes", personajes);
        model.addAttribute("newPersonaje", new Personaje());
        System.out.println("Personajes encontrados: " + personajes.size());
        return "narrador";
    }

    @GetMapping("/jugador/{id}")
    public String mostrarVistaJugador(@PathVariable Long id, Model model) {
        Personaje personaje = buscarPersonaje(id);
        Batalla batallaActiva = batallaService.buscarActivaPorPersonaje(id).orElse(null);

        model.addAttribute("personaje", personaje);
        model.addAttribute("batallaActiva", batallaActiva);
        model.addAttribute("esTurnoJugador", batallaActiva != null && personaje.isTurnoActual() && !personaje.isDerrotado());
        return "jugador";
    }

    @PostMapping("addData")
    public String postMethodName(@RequestBody String entity) {
        personajeService.guardar(new Personaje());
        return entity;
    }

    @PostMapping("/personaje/{id}/iniciativa")
    @ResponseBody
    public DadosService.ResultadoIniciativa tirarIniciativa(@PathVariable Long id) {
        Personaje personaje = buscarPersonaje(id);
        return dadosService.tirarIniciativa(personaje);
    }

    @PostMapping("/personaje/{id}/moneda")
    @ResponseBody
    public Map<String, Object> actualizarMoneda(
            @PathVariable Long id,
            @RequestParam String tipo,
            @RequestParam Integer valor) {

        Personaje personaje = buscarPersonaje(id);

        if (personaje.getCobre() == null) personaje.setCobre(0);
        if (personaje.getPlata() == null) personaje.setPlata(0);
        if (personaje.getOro() == null) personaje.setOro(0);
        if (personaje.getPlatino() == null) personaje.setPlatino(0);

        if (valor == null || valor < 0) {
            valor = 0;
        }

        switch (tipo.toLowerCase()) {
            case "cobre" -> personaje.setCobre(valor);
            case "plata" -> personaje.setPlata(valor);
            case "oro" -> personaje.setOro(valor);
            case "platino" -> personaje.setPlatino(valor);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de moneda desconocido");
        }

        Personaje actualizado = personajeService.guardar(personaje);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", actualizado.getId());
        response.put("cobre", actualizado.getCobre());
        response.put("plata", actualizado.getPlata());
        response.put("oro", actualizado.getOro());
        response.put("platino", actualizado.getPlatino());
        return response;
    }

    @PostMapping("/inventario/{id}/usar")
    @ResponseBody
    public Map<String, Object> usarItem(@PathVariable Long id) {
        return actualizarItemInventario(id, ItemInventario::usar);
    }

    @PostMapping("/inventario/{id}/equipar")
    @ResponseBody
    public Map<String, Object> equiparItem(@PathVariable Long id) {
        return actualizarItemInventario(id, ItemInventario::equipar);
    }

    @PostMapping("/inventario/{id}/desequipar")
    @ResponseBody
    public Map<String, Object> desequiparItem(@PathVariable Long id) {
        return actualizarItemInventario(id, ItemInventario::desequipar);
    }

    @DeleteMapping("/inventario/{id}")
    @ResponseBody
    public Map<String, Object> eliminarItemInventario(@PathVariable Long id) {
        ItemInventario itemInventario = itemInventarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item no encontrado"));

        Map<String, Object> response = construirRespuestaItemInventario(itemInventario);
        itemInventarioRepository.delete(itemInventario);
        return response;
    }

    @PostMapping("/personaje/{id}/inventario")
    @ResponseBody
    public Map<String, Object> agregarItemInventario(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(defaultValue = "miscelaneo") String tipo,
            @RequestParam(defaultValue = "") String descripcion,
            @RequestParam(defaultValue = "1") Integer cantidad,
            @RequestParam(defaultValue = "false") Boolean apilable,
            @RequestParam(defaultValue = "false") Boolean magico) {

        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del ítem es obligatorio");
        }

        Personaje personaje = buscarPersonaje(id);

        ItemBase itemBase = new ItemBase();
        itemBase.setNombre(nombre.trim());
        itemBase.setTipo(tipo == null || tipo.isBlank() ? "miscelaneo" : tipo.trim());
        itemBase.setDescripcion(descripcion == null ? "" : descripcion.trim());
        itemBase.setPeso(0);
        itemBase.setValorOro(0);
        itemBase.setRareza("comun");
        itemBase.setApilable(Boolean.TRUE.equals(apilable));
        itemBase.setMagico(Boolean.TRUE.equals(magico));

        ItemBase itemBaseGuardado = itemBaseRepository.save(itemBase);

        ItemInventario itemInventario = new ItemInventario();
        itemInventario.setItemBase(itemBaseGuardado);
        itemInventario.setPersonaje(personaje);
        itemInventario.setCantidad(cantidad == null || cantidad <= 0 ? 1 : cantidad);
        itemInventario.setEquipado(false);
        itemInventario.setConsumido(false);

        ItemInventario itemGuardado = itemInventarioRepository.save(itemInventario);
        return construirRespuestaItemInventario(itemGuardado);
    }

    @PostMapping("/personaje/{id}/condiciones")
    @ResponseBody
    public Map<String, Object> agregarCondicion(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(defaultValue = "") String descripcion,
            @RequestParam(defaultValue = "0") Integer duracion) {

        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la condición es obligatorio");
        }

        Personaje personaje = buscarPersonaje(id);
        if (personaje.getCondiciones() == null) {
            personaje.setCondiciones(new ArrayList<>());
        }

        Condicion condicion = new Condicion();
        condicion.setNombre(nombre.trim());
        condicion.setDescripcion(descripcion == null ? "" : descripcion.trim());
        condicion.setDuracion(duracion == null || duracion < 0 ? 0 : duracion);

        personaje.getCondiciones().add(condicion);
        Personaje actualizado = personajeService.guardar(personaje);

        Condicion condicionGuardada = actualizado.getCondiciones()
                .get(actualizado.getCondiciones().size() - 1);

        return construirRespuestaCondicion(condicionGuardada);
    }

    @DeleteMapping("/personaje/{personajeId}/condiciones/{condicionId}")
    @ResponseBody
    public Map<String, Object> eliminarCondicion(@PathVariable Long personajeId, @PathVariable Long condicionId) {
        Personaje personaje = buscarPersonaje(personajeId);

        Condicion condicion = personaje.getCondiciones()
                .stream()
                .filter(item -> condicionId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Condición no encontrada"));

        personaje.getCondiciones().remove(condicion);
        personajeService.guardar(personaje);
        return construirRespuestaCondicion(condicion);
    }

    @PostMapping("/personaje/{id}/hechizos")
    @ResponseBody
    public Map<String, Object> agregarHechizo(
            @PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(defaultValue = "1") Integer nivel,
            @RequestParam(defaultValue = "utilidad") String tipo,
            @RequestParam(defaultValue = "") String descripcion,
            @RequestParam(name = "danio", defaultValue = "0") Integer danio,
            @RequestParam(defaultValue = "0") Integer duracion,
            @RequestParam(defaultValue = "false") Boolean requiereConcentracion) {

        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del hechizo es obligatorio");
        }

        Personaje personaje = buscarPersonaje(id);
        if (personaje.getHechizos() == null) {
            personaje.setHechizos(new ArrayList<>());
        }

        Hechizo hechizo = new Hechizo();
        hechizo.setNombre(nombre.trim());
        hechizo.setNivel(nivel == null || nivel < 0 ? 1 : nivel);
        hechizo.setTipo(tipo == null || tipo.isBlank() ? "utilidad" : tipo.trim());
        hechizo.setDescripcion(descripcion == null ? "" : descripcion.trim());
        hechizo.setDaño(danio == null || danio < 0 ? 0 : danio);
        hechizo.setDuracion(duracion == null || duracion < 0 ? 0 : duracion);
        hechizo.setRequiereConcentracion(Boolean.TRUE.equals(requiereConcentracion));

        personaje.getHechizos().add(hechizo);
        Personaje actualizado = personajeService.guardar(personaje);
        Hechizo hechizoGuardado = actualizado.getHechizos().get(actualizado.getHechizos().size() - 1);

        return construirRespuestaHechizo(actualizado, hechizoGuardado, 0);
    }

    @PostMapping("/personaje/{personajeId}/hechizos/{hechizoId}/lanzar")
    @ResponseBody
    public Map<String, Object> lanzarHechizo(@PathVariable Long personajeId, @PathVariable Long hechizoId) {
        Personaje personaje = buscarPersonaje(personajeId);

        Hechizo hechizo = personaje.getHechizos()
                .stream()
                .filter(item -> hechizoId.equals(item.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hechizo no encontrado"));

        int energiaActual = Math.max(personaje.getPuntosEnergia(), 0);
        int costeEnergia = Math.max(1, hechizo.getNivel());

        if (energiaActual < costeEnergia) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay suficiente energía para lanzar este hechizo");
        }

        personaje.setPuntosEnergia(energiaActual - costeEnergia);
        Personaje actualizado = personajeService.guardar(personaje);

        return construirRespuestaHechizo(actualizado, hechizo, costeEnergia);
    }

    @GetMapping("/jugador/{id}/batalla/estado")
    @ResponseBody
    public Map<String, Object> obtenerEstadoBatallaJugador(@PathVariable Long id) {
        Personaje personaje = buscarPersonaje(id);
        Batalla batalla = batallaService.buscarActivaPorPersonaje(id).orElse(null);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("personajeId", personaje.getId());
        response.put("enBatalla", batalla != null);
        response.put("esTurnoJugador", personaje.isTurnoActual() && !personaje.isDerrotado());

        if (batalla != null) {
            response.put("batallaId", batalla.getId());
            response.put("nombreBatalla", batalla.getNombre());
            response.put("estado", batalla.getEstado());
            response.put("rondaActual", batalla.getRondaActual());
        }
        return response;
    }

    @PostMapping("/jugador/{personajeId}/batalla/atacar")
    @ResponseBody
    public Map<String, Object> atacarDesdeJugador(@PathVariable Long personajeId, @RequestParam Long objetivoId) {
        return batallaService.atacarDesdeJugador(personajeId, objetivoId);
    }

    @PostMapping("/jugador/{personajeId}/batalla/hechizos/{hechizoId}/lanzar")
    @ResponseBody
    public Map<String, Object> lanzarHechizoEnBatalla(
            @PathVariable Long personajeId,
            @PathVariable Long hechizoId,
            @RequestParam(required = false) Long objetivoId) {
        return batallaService.lanzarHechizoDesdeJugador(personajeId, hechizoId, objetivoId);
    }

    @PostMapping("/jugador/{personajeId}/batalla/turno/finalizar")
    @ResponseBody
    public Map<String, Object> finalizarTurnoJugador(@PathVariable Long personajeId) {
        return batallaService.finalizarTurnoJugador(personajeId);
    }

    @PostMapping("/personaje")
    public String crearPersonaje(Personaje personaje) {
        personajeService.guardar(personaje);
        return "redirect:/narrador";
    }

    @DeleteMapping("/personaje/{id}")
    @ResponseBody
    public void borrarPersonaje(@PathVariable Long id) {
        personajeService.borrar(id);
    }

    private Personaje buscarPersonaje(Long id) {
        return personajeService.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personaje no encontrado"));
    }

    private Map<String, Object> actualizarItemInventario(Long id, Consumer<ItemInventario> accion) {
        ItemInventario item = itemInventarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item no encontrado"));

        accion.accept(item);
        ItemInventario actualizado = itemInventarioRepository.save(item);
        return construirRespuestaItemInventario(actualizado);
    }

    private Map<String, Object> construirRespuestaCondicion(Condicion condicion) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", condicion.getId());
        response.put("nombre", condicion.getNombre());
        response.put("descripcion", condicion.getDescripcion());
        response.put("duracion", condicion.getDuracion());
        return response;
    }

    private Map<String, Object> construirRespuestaItemInventario(ItemInventario itemInventario) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", itemInventario.getId());
        response.put("nombre", itemInventario.getItemBase() != null ? itemInventario.getItemBase().getNombre() : "Ítem");
        response.put("tipo", itemInventario.getItemBase() != null ? itemInventario.getItemBase().getTipo() : "miscelaneo");
        response.put("descripcion", itemInventario.getItemBase() != null ? itemInventario.getItemBase().getDescripcion() : "");
        response.put("cantidad", itemInventario.getCantidad());
        response.put("equipado", itemInventario.isEquipado());
        response.put("consumido", itemInventario.isConsumido());
        response.put("apilable", itemInventario.getItemBase() != null && itemInventario.getItemBase().isApilable());
        response.put("magico", itemInventario.getItemBase() != null && itemInventario.getItemBase().isMagico());
        return response;
    }

    private Map<String, Object> construirRespuestaHechizo(Personaje personaje, Hechizo hechizo, int costeEnergia) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", hechizo.getId());
        response.put("nombre", hechizo.getNombre());
        response.put("tipo", hechizo.getTipo());
        response.put("descripcion", hechizo.getDescripcion());
        response.put("nivel", hechizo.getNivel());
        response.put("daño", hechizo.getDaño());
        response.put("danio", hechizo.getDaño());
        response.put("duracion", hechizo.getDuracion());
        response.put("requiereConcentracion", hechizo.isRequiereConcentracion());
        response.put("costeEnergia", costeEnergia);
        response.put("energiaRestante", personaje.getPuntosEnergia());
        response.put("nombrePersonaje", personaje.getNombre());
        return response;
    }
}
