package com.dungeons_and_dragons.rol.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
        System.out.println("Personajes encontrados: " + personajes.size());
       // lo manda al HTML
        return "narrador";  // Thymeleaf buscará resources/templates/narrador.html
    }

    @PostMapping("addData")
    public String postMethodName(@RequestBody String entity) {
        personajeService.guardar(new Personaje()); // Aquí deberías convertir el String a un Personaje real
        
        return entity;
    }
    
}