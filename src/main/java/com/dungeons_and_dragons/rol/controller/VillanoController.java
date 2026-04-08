package com.dungeons_and_dragons.rol.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import com.dungeons_and_dragons.rol.model.Batalla;
import com.dungeons_and_dragons.rol.model.Hechizo;
import com.dungeons_and_dragons.rol.model.Villano;
import com.dungeons_and_dragons.rol.repository.VillanoRepository;
import com.dungeons_and_dragons.rol.service.BatallaService;
import com.dungeons_and_dragons.rol.service.DadosService;

@Controller
public class VillanoController {

    private final VillanoRepository villanoRepository;
    private final BatallaService batallaService;
    private final DadosService dadosService;

    public VillanoController(VillanoRepository villanoRepository, BatallaService batallaService, DadosService dadosService) {
        this.villanoRepository = villanoRepository;
        this.batallaService = batallaService;
        this.dadosService = dadosService;
    }

    @GetMapping("/villano/{id}")
    public String mostrarVistaVillano(@PathVariable Long id, Model model) {
        Villano villano = buscarVillano(id);
        Batalla batallaActiva = batallaService.buscarActivaPorVillano(id).orElse(null);

        model.addAttribute("villano", villano);
        model.addAttribute("batallaActiva", batallaActiva);
        model.addAttribute("esTurnoVillano", batallaActiva != null && villano.isTurnoActual() && !villano.isDerrotado());
        return "villano";
    }

    @PostMapping("/villano/{id}/iniciativa")
    @ResponseBody
    public DadosService.ResultadoIniciativa tirarIniciativa(@PathVariable Long id) {
        Villano villano = buscarVillano(id);
        int tirada = dadosService.lanzarDado(20);
        int modificador = dadosService.calcularModificador(villano.getDestreza());
        int total = tirada + modificador;
        villano.setIniciativaActual(total);
        villanoRepository.save(villano);
        return new DadosService.ResultadoIniciativa(villano.getNombre(), tirada, modificador, total);
    }

    @PostMapping("/villano/{villanoId}/hechizos/{hechizoId}/lanzar")
    @ResponseBody
    public Map<String, Object> lanzarHechizo(@PathVariable Long villanoId, @PathVariable Long hechizoId) {
        Villano villano = buscarVillano(villanoId);
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
        Villano actualizado = villanoRepository.save(villano);
        return construirRespuestaHechizo(actualizado, hechizo, costeEnergia);
    }

    @GetMapping("/villano/{id}/batalla/estado")
    @ResponseBody
    public Map<String, Object> obtenerEstadoBatallaVillano(@PathVariable Long id) {
        Villano villano = buscarVillano(id);
        Batalla batalla = batallaService.buscarActivaPorVillano(id).orElse(null);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("villanoId", villano.getId());
        response.put("enBatalla", batalla != null);
        response.put("esTurnoVillano", villano.isTurnoActual() && !villano.isDerrotado());

        if (batalla != null) {
            response.put("batallaId", batalla.getId());
            response.put("nombreBatalla", batalla.getNombre());
            response.put("estado", batalla.getEstado());
            response.put("rondaActual", batalla.getRondaActual());
        }
        return response;
    }

    @PostMapping("/villano/{villanoId}/batalla/atacar")
    @ResponseBody
    public Map<String, Object> atacarDesdeVillano(@PathVariable Long villanoId, @RequestParam Long objetivoId) {
        return batallaService.atacarDesdeVillano(villanoId, objetivoId);
    }

    @PostMapping("/villano/{villanoId}/batalla/hechizos/{hechizoId}/lanzar")
    @ResponseBody
    public Map<String, Object> lanzarHechizoEnBatalla(
            @PathVariable Long villanoId,
            @PathVariable Long hechizoId,
            @RequestParam(required = false) Long objetivoId) {
        return batallaService.lanzarHechizoDesdeVillano(villanoId, hechizoId, objetivoId);
    }

    @PostMapping("/villano/{villanoId}/batalla/turno/finalizar")
    @ResponseBody
    public Map<String, Object> finalizarTurnoVillano(@PathVariable Long villanoId) {
        return batallaService.finalizarTurnoVillano(villanoId);
    }

    private Villano buscarVillano(Long id) {
        Villano villano = villanoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Villano no encontrado"));

        boolean actualizado = false;
        if (villano.getHechizos() == null) {
            villano.setHechizos(new ArrayList<>());
            actualizado = true;
        }
        if (villano.getHechizos().isEmpty()) {
            villano.getHechizos().add(crearHechizo("Golpe oscuro", Math.max(1, villano.getNivel()), "ataque",
                    "Un impacto sombrío contra su objetivo.", 3 + Math.max(1, villano.getNivel()), 0, false));
            villano.getHechizos().add(crearHechizo("Sello siniestro", 1, "curacion",
                    "Recupera algo de vitalidad oscura.", 2 + Math.max(1, villano.getNivel()), 0, false));
            actualizado = true;
        }

        if (actualizado) {
            villanoRepository.save(villano);
        }
        return villano;
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

    private Map<String, Object> construirRespuestaHechizo(Villano villano, Hechizo hechizo, int costeEnergia) {
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
        response.put("energiaRestante", villano.getPuntosEnergia());
        response.put("nombreVillano", villano.getNombre());
        return response;
    }
}
