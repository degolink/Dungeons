package com.dungeons_and_dragons.rol.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.dungeons_and_dragons.rol.model.Batalla;
import com.dungeons_and_dragons.rol.repository.BatallaRepository;
import com.dungeons_and_dragons.rol.repository.PersonajeRepository;

/**
 * Pruebas de integración: BatallaController → BatallaService → H2
 * Usa MockMvc para ejercitar la capa HTTP completa con Spring Security.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Integración: BatallaController (HTTP → Service → JPA)")
class BatallaControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private BatallaRepository batallaRepository;

    @Autowired
    private PersonajeRepository personajeRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();

        batallaRepository.deleteAll();
        personajeRepository.deleteAll();
    }

    // ------------------------------------------------------------------
    // GET /batallas
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "jugador@test.com", roles = "JUGADOR")
    @DisplayName("GET /batallas crea una batalla demo y redirige a ella")
    void getBatallasRedirigeADemo() throws Exception {
        mockMvc.perform(get("/batallas"))
                .andExpect(status().is3xxRedirection());

        assertThat(batallaRepository.count()).isGreaterThanOrEqualTo(1);
    }

    // ------------------------------------------------------------------
    // GET /batallas/{id}
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "jugador@test.com", roles = "JUGADOR")
    @DisplayName("GET /batallas/{id} devuelve 200 para una batalla existente")
    void getBatallaExistente() throws Exception {
        Batalla batalla = crearBatalla("Batalla de Helm");

        mockMvc.perform(get("/batallas/" + batalla.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "jugador@test.com", roles = "JUGADOR")
    @DisplayName("GET /batallas/{id} devuelve 404 para una batalla inexistente")
    void getBatallaInexistente() throws Exception {
        mockMvc.perform(get("/batallas/999999"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // POST /batallas/{id}/iniciar
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "narrador@test.com", roles = "NARRADOR")
    @DisplayName("POST /iniciar cambia el estado a EN_CURSO y devuelve JSON correcto")
    void iniciarBatalla() throws Exception {
        Batalla batalla = crearBatalla("Batalla del Abismo");

        mockMvc.perform(post("/batallas/" + batalla.getId() + "/iniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.id").value(batalla.getId()))
                .andExpect(jsonPath("$.mensaje").value("Batalla iniciada"));
    }

    @Test
    @WithMockUser(username = "narrador@test.com", roles = "NARRADOR")
    @DisplayName("POST /iniciar sobre batalla inexistente devuelve 404")
    void iniciarBatallaInexistente() throws Exception {
        mockMvc.perform(post("/batallas/999999/iniciar"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // POST /batallas/{id}/reiniciar
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "narrador@test.com", roles = "NARRADOR")
    @DisplayName("POST /reiniciar vuelve al estado PREPARADA con ronda 0")
    void reiniciarBatalla() throws Exception {
        Batalla batalla = crearBatalla("Batalla de Minas Tirith");

        // Iniciar primero
        mockMvc.perform(post("/batallas/" + batalla.getId() + "/iniciar"));

        // Luego reiniciar
        mockMvc.perform(post("/batallas/" + batalla.getId() + "/reiniciar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PREPARADA"))
                .andExpect(jsonPath("$.rondaActual").value(0))
                .andExpect(jsonPath("$.mensaje").value("Batalla reiniciada"));
    }

    // ------------------------------------------------------------------
    // POST /batallas/{id}/turno/siguiente
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "narrador@test.com", roles = "NARRADOR")
    @DisplayName("POST /turno/siguiente avanza el turno en una batalla activa")
    void siguienteTurno() throws Exception {
        Batalla batalla = crearBatalla("Test Turno");

        mockMvc.perform(post("/batallas/" + batalla.getId() + "/iniciar"));

        mockMvc.perform(post("/batallas/" + batalla.getId() + "/turno/siguiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Turno actualizado"));
    }

    // ------------------------------------------------------------------
    // Flujo completo: iniciar → siguiente turno → reiniciar
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "narrador@test.com", roles = "NARRADOR")
    @DisplayName("Flujo completo: iniciar → turno/siguiente → reiniciar")
    void flujoCompleto() throws Exception {
        Batalla batalla = crearBatalla("Flujo Completo");
        Long id = batalla.getId();

        // 1. Iniciar
        mockMvc.perform(post("/batallas/" + id + "/iniciar"))
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));

        // 2. Avanzar turno
        mockMvc.perform(post("/batallas/" + id + "/turno/siguiente"))
                .andExpect(status().isOk());

        // 3. Reiniciar → vuelve al estado inicial
        mockMvc.perform(post("/batallas/" + id + "/reiniciar"))
                .andExpect(jsonPath("$.estado").value("PREPARADA"))
                .andExpect(jsonPath("$.rondaActual").value(0));
    }

    // ------------------------------------------------------------------
    // Seguridad
    // ------------------------------------------------------------------

    @Test
    @DisplayName("GET /batallas sin autenticación redirige a login")
    void sinAutenticacionRedirigeALogin() throws Exception {
        mockMvc.perform(get("/batallas"))
                .andExpect(status().is3xxRedirection());
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private Batalla crearBatalla(String nombre) {
        Batalla b = new Batalla();
        b.setNombre(nombre);
        b.setEstado("PREPARADA");
        b.setRondaActual(0);
        return batallaRepository.save(b);
    }
}
