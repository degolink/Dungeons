package com.dungeons_and_dragons.rol.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;

class PersonajeServiceTest {

    @Mock
    private PersonajeRepository personajeRepository;

    @InjectMocks
    private PersonajeService personajeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deberiaGuardarUnPersonaje() {
        Personaje personaje = new Personaje();
        personaje.setNombre("Arthas");

        when(personajeRepository.save(personaje)).thenReturn(personaje);

        Personaje resultado = personajeService.guardar(personaje);

        assertNotNull(resultado);
        assertEquals("Arthas", resultado.getNombre());
        verify(personajeRepository, times(1)).save(personaje);
    }

    @Test
    void deberiaListarTodosLosPersonajes() {
        Personaje p1 = new Personaje();
        p1.setNombre("Arthas");

        Personaje p2 = new Personaje();
        p2.setNombre("Jaina");

        when(personajeRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Personaje> resultado = personajeService.listar();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Arthas", resultado.get(0).getNombre());
        assertEquals("Jaina", resultado.get(1).getNombre());

        verify(personajeRepository, times(1)).findAll();
    }

    @Test
    void deberiaBuscarPersonajePorIdCuandoExiste() {
        Personaje personaje = new Personaje();
        personaje.setNombre("Thrall");

        when(personajeRepository.findById(1L)).thenReturn(Optional.of(personaje));

        Optional<Personaje> resultado = personajeService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Thrall", resultado.get().getNombre());

        verify(personajeRepository, times(1)).findById(1L);
    }

    @Test
    void deberiaRetornarVacioCuandoNoExisteElPersonaje() {
        when(personajeRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Personaje> resultado = personajeService.buscarPorId(99L);

        assertFalse(resultado.isPresent());

        verify(personajeRepository, times(1)).findById(99L);
    }

    @Test
    void deberiaEliminarPersonajePorId() {
        doNothing().when(personajeRepository).deleteById(1L);

        personajeService.borrar(1L  );
        verify(personajeRepository, times(1)).deleteById(1L);
    }

    @Test
    void deberiaAsignarEnergiaInicialAlCrearPersonajeSiNoSeIndica() {
        Personaje personaje = new Personaje();
        personaje.setNombre("Merlín");
        personaje.setClase("Mago");
        personaje.setNivel(3);
        personaje.setPuntosEnergia(0);

        when(personajeRepository.save(personaje)).thenAnswer(invocation -> invocation.getArgument(0));

        Personaje resultado = personajeService.prepararNuevoPersonaje(personaje);

        assertEquals(24, resultado.getPuntosEnergia());
        verify(personajeRepository, times(1)).save(personaje);
    }

    @Test
    void deberiaAsignarHechizosInicialesAlCrearUnPersonajeNuevo() {
        Personaje personaje = new Personaje();
        personaje.setNombre("Kael");
        personaje.setClase("Mago");
        personaje.setNivel(2);

        when(personajeRepository.save(personaje)).thenAnswer(invocation -> invocation.getArgument(0));

        Personaje resultado = personajeService.prepararNuevoPersonaje(personaje);

        assertNotNull(resultado.getHechizos());
        assertFalse(resultado.getHechizos().isEmpty());
        assertEquals("Bola de Fuego", resultado.getHechizos().get(0).getNombre());
        verify(personajeRepository, times(1)).save(personaje);
    }
}