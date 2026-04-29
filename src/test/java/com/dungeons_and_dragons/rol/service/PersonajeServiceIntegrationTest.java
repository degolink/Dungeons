package com.dungeons_and_dragons.rol.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.dungeons_and_dragons.rol.model.Personaje;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;

/**
 * Pruebas de integración: PersonajeService + PersonajeRepository + H2
 * Se ejercita la pila real (Service → Repository → JPA → H2) sin mocks.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Integración: PersonajeService + JPA (H2)")
class PersonajeServiceIntegrationTest {

    @Autowired
    private PersonajeService personajeService;

    @Autowired
    private PersonajeRepository personajeRepository;

    @BeforeEach
    void limpiarBD() {
        personajeRepository.deleteAll();
    }

    // ------------------------------------------------------------------
    // Guardar y recuperar
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Crear y recuperar un personaje por ID")
    void crearYRecuperarPersonaje() {
        Personaje nuevo = construirPersonaje("Aragorn", "Guerrero", 5);
        Personaje guardado = personajeService.prepararNuevoPersonaje(nuevo);

        assertThat(guardado.getId()).isNotNull();

        Optional<Personaje> encontrado = personajeService.buscarPorId(guardado.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Aragorn");
    }

    @Test
    @DisplayName("listar() devuelve todos los personajes guardados")
    void listarPersonajes() {
        personajeService.prepararNuevoPersonaje(construirPersonaje("Frodo", "Pícaro", 3));
        personajeService.prepararNuevoPersonaje(construirPersonaje("Gandalf", "Mago", 10));

        List<Personaje> lista = personajeService.listar();
        assertThat(lista).hasSize(2);
    }

    // ------------------------------------------------------------------
    // Normalización de campos base
    // ------------------------------------------------------------------

    @Test
    @DisplayName("prepararNuevoPersonaje rellena campos base nulos")
    void camposBaseSeNormalizan() {
        Personaje p = construirPersonaje("Legolas", "Guerrero", 4);
        p.setFuerzaBase(null);
        p.setDestrezaBase(null);

        Personaje guardado = personajeService.prepararNuevoPersonaje(p);

        assertThat(guardado.getFuerzaBase()).isEqualTo(guardado.getFuerza());
        assertThat(guardado.getDestrezaBase()).isEqualTo(guardado.getDestreza());
    }

    @Test
    @DisplayName("prepararNuevoPersonaje asigna energía inicial según clase (Mago → nivel*8)")
    void energiaInicialSeAsignaSegunClase() {
        Personaje mago = construirPersonaje("Merlin", "Mago", 5);
        mago.setPuntosEnergia(0);

        Personaje guardado = personajeService.prepararNuevoPersonaje(mago);

        assertThat(guardado.getPuntosEnergia()).isEqualTo(40); // 5 * 8
    }

    @Test
    @DisplayName("prepararNuevoPersonaje asigna hechizos iniciales a un Mago")
    void hechizosInicialesMago() {
        Personaje mago = construirPersonaje("Saruman", "Mago", 3);

        Personaje guardado = personajeService.prepararNuevoPersonaje(mago);

        assertThat(guardado.getHechizos()).isNotEmpty();
        assertThat(guardado.getHechizos().stream().map(h -> h.getNombre()))
                .contains("Bola de Fuego", "Escudo Arcano");
    }

    @Test
    @DisplayName("prepararNuevoPersonaje asigna hechizos iniciales a un Clérigo")
    void hechizosInicialesClerigo() {
        Personaje clerigo = construirPersonaje("Radagast", "Clérigo", 4);

        Personaje guardado = personajeService.prepararNuevoPersonaje(clerigo);

        assertThat(guardado.getHechizos().stream().map(h -> h.getNombre()))
                .contains("Luz Sanadora", "Llama Sagrada");
    }

    // ------------------------------------------------------------------
    // Actualizar energía
    // ------------------------------------------------------------------

    @Test
    @DisplayName("actualizarEnergia persiste el nuevo valor en BD")
    void actualizarEnergiaPersiste() {
        Personaje p = personajeService.prepararNuevoPersonaje(construirPersonaje("Boromir", "Guerrero", 6));

        Personaje actualizado = personajeService.actualizarEnergia(p, 15);

        assertThat(actualizado.getPuntosEnergia()).isEqualTo(15);
        assertThat(personajeService.buscarPorId(actualizado.getId())
                .map(Personaje::getPuntosEnergia).orElse(-1))
                .isEqualTo(15);
    }

    @Test
    @DisplayName("actualizarEnergia no permite valores negativos (queda en 0)")
    void actualizarEnergiaNegativaQuedaEnCero() {
        Personaje p = personajeService.prepararNuevoPersonaje(construirPersonaje("Gimli", "Guerrero", 4));

        Personaje actualizado = personajeService.actualizarEnergia(p, -50);

        assertThat(actualizado.getPuntosEnergia()).isEqualTo(0);
    }

    // ------------------------------------------------------------------
    // Eliminar
    // ------------------------------------------------------------------

    @Test
    @DisplayName("borrar() elimina el personaje de la BD")
    void borrarPersonaje() {
        Personaje p = personajeService.prepararNuevoPersonaje(construirPersonaje("Sauron", "Mago", 20));
        Long id = p.getId();

        personajeService.borrar(id);

        assertThat(personajeService.buscarPorId(id)).isEmpty();
    }

    // ------------------------------------------------------------------
    // Validaciones de guard
    // ------------------------------------------------------------------

    @Test
    @DisplayName("prepararNuevoPersonaje lanza excepción con personaje nulo")
    void personajeNuloLanzaExcepcion() {
        assertThatThrownBy(() -> personajeService.prepararNuevoPersonaje(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nulo");
    }

    @Test
    @DisplayName("actualizarEnergia lanza excepción con personaje nulo")
    void actualizarEnergiaNulaLanzaExcepcion() {
        assertThatThrownBy(() -> personajeService.actualizarEnergia(null, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private Personaje construirPersonaje(String nombre, String clase, int nivel) {
        return Personaje.builder()
                .nombre(nombre)
                .clase(clase)
                .nivel(nivel)
                .fuerza(15)
                .destreza(12)
                .constitucion(14)
                .inteligencia(10)
                .sabiduria(10)
                .carisma(8)
                .puntosVida(40)
                .puntosVidaMax(40)
                .build();
    }
}
