package com.dungeons_and_dragons.rol.service;

import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonajeService {

    private final PersonajeRepository personajeRepository;

    public PersonajeService(PersonajeRepository personajeRepository) {
        this.personajeRepository = personajeRepository;
    }

    // Guardar o actualizar personaje
    public Personaje guardar(Personaje personaje) {
        return personajeRepository.save(personaje);
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
}