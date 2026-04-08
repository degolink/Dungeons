package com.dungeons_and_dragons.rol.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dungeons_and_dragons.rol.model.Batalla;
import com.dungeons_and_dragons.rol.service.BatallaService;

@Controller
public class BatallaController {

    private final BatallaService batallaService;

    public BatallaController(BatallaService batallaService) {
        this.batallaService = batallaService;
    }

    @GetMapping("/batallas")
    public String irABatallaDemo() {
        Batalla batalla = batallaService.obtenerOCrearBatallaDemo();
        return "redirect:/batallas/" + batalla.getId();
    }

    @GetMapping("/batallas/{id}")
    public String mostrarBatalla(@PathVariable Long id, Model model) {
        model.addAttribute("batalla", batallaService.buscarPorId(id));
        return "batalla";
    }

    @PostMapping("/batallas/{id}/iniciar")
    @ResponseBody
    public Map<String, Object> iniciarBatalla(@PathVariable Long id) {
        Batalla batalla = batallaService.iniciarBatalla(id);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", batalla.getId());
        response.put("estado", batalla.getEstado());
        response.put("rondaActual", batalla.getRondaActual());
        response.put("mensaje", "Batalla iniciada");
        return response;
    }

    @PostMapping("/batallas/{id}/reiniciar")
    @ResponseBody
    public Map<String, Object> reiniciarBatalla(@PathVariable Long id) {
        Batalla batalla = batallaService.reiniciarBatalla(id);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", batalla.getId());
        response.put("estado", batalla.getEstado());
        response.put("rondaActual", batalla.getRondaActual());
        response.put("mensaje", "Batalla reiniciada");
        return response;
    }

    @PostMapping("/batallas/{id}/atacar")
    @ResponseBody
    public Map<String, Object> atacar(
            @PathVariable Long id,
            @RequestParam String atacanteTipo,
            @RequestParam Long atacanteId,
            @RequestParam String objetivoTipo,
            @RequestParam Long objetivoId) {
        return batallaService.atacar(id, atacanteTipo, atacanteId, objetivoTipo, objetivoId);
    }

    @PostMapping("/batallas/{id}/turno/siguiente")
    @ResponseBody
    public Map<String, Object> siguienteTurno(@PathVariable Long id) {
        Batalla batalla = batallaService.siguienteTurno(id);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", batalla.getId());
        response.put("estado", batalla.getEstado());
        response.put("rondaActual", batalla.getRondaActual());
        response.put("mensaje", "Turno actualizado");
        return response;
    }
}
