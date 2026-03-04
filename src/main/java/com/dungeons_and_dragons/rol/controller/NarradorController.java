package com.dungeons_and_dragons.rol.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.service.PersonajeService;


@Controller
public class NarradorController {

    private final PersonajeService personajeService;

    public NarradorController(PersonajeService personajeService) {
        this.personajeService = personajeService;
    }




    

    @GetMapping("/")
    public String home() {
    return "redirect:/narrador";
    }

    @GetMapping("/narrador")
    public String mostrarPantalla(Model model) {
        List<Personaje> personajes = personajeService.listar();  // lista los 4 personajes
        model.addAttribute("personajes", personajes);    
        model.addAttribute("newPersonaje", new Personaje());
        System.out.println("Personajes encontrados: " + personajes.size());
       // lo manda al HTML
        return "narrador";  // Thymeleaf buscará resources/templates/narrador.html
    }

    @PostMapping("addData")
    public String postMethodName(@RequestBody String entity) {
        personajeService.guardar(new Personaje()); // Aquí deberías convertir el String a un Personaje real
        
        return entity;
    }

    /**
     * Actualiza la cantidad de una moneda para un personaje.
     * Recibe el id del personaje en la ruta y los parámetros 'tipo' y 'valor'.
     * Devuelve el personaje actualizado (serializado como JSON).
     */
    @PostMapping("/personaje/{id}/moneda")
    @org.springframework.web.bind.annotation.ResponseBody
    public Personaje actualizarMoneda(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestParam String tipo,
            @org.springframework.web.bind.annotation.RequestParam Integer valor) {

        Personaje personaje = personajeService.buscarPorId(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Personaje no encontrado"));

        // asegurarse de que no haya nulls en las monedas para evitar errores de integridad
        if (personaje.getCobre() == null) personaje.setCobre(0);
        if (personaje.getPlata() == null) personaje.setPlata(0);
        if (personaje.getOro() == null) personaje.setOro(0);
        if (personaje.getPlatino() == null) personaje.setPlatino(0);

        // normalizamos el valor para que no sea negativo
        if (valor == null || valor < 0) {
            valor = 0;
        }

        switch (tipo.toLowerCase()) {
            case "cobre":
                personaje.setCobre(valor);
                break;
            case "plata":
                personaje.setPlata(valor);
                break;
            case "oro":
                personaje.setOro(valor);
                break;
            case "platino":
                personaje.setPlatino(valor);
                break;
            default:
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Tipo de moneda desconocido");
        }

        return personajeService.guardar(personaje);
    }
    
    // crear personaje nuevo
    @PostMapping("/personaje")
    public String crearPersonaje(Personaje personaje) {
        personajeService.guardar(personaje);
        return "redirect:/narrador";
    }

    // eliminar personaje
    @DeleteMapping("/personaje/{id}")
    @ResponseBody
    public void borrarPersonaje(@PathVariable Long id) {
        personajeService.borrar(id);
    }

}