package com.dungeons_and_dragons.rol.service;

import com.dungeons_and_dragons.rol.model.Usuario;
import com.dungeons_and_dragons.rol.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests de Integracion para Seguridad")
class SecurityIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /login deberia ser accesible sin autenticacion")
    void testLoginAccesibleSinAutenticacion() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /registro deberia ser accesible sin autenticacion")
    void testRegistroAccesibleSinAutenticacion() throws Exception {
        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /narrador deberia redirigir a login sin autenticacion")
    void testNarradorRedirigeASinAutenticacion() throws Exception {
        mockMvc.perform(get("/narrador"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("POST /registro deberia crear nuevo usuario")
    void testRegistroCrearNuevoUsuario() throws Exception {
        mockMvc.perform(post("/registro")
                .with(csrf())
                .param("nombre", "Nuevo Usuario")
                .param("email", "nuevo@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        Usuario usuario = usuarioRepository.findByEmail("nuevo@example.com").orElse(null);
        assert usuario != null : "Usuario deberia haber sido creado";
        assert passwordEncoder.matches("password123", usuario.getPassword()) : "Contrasena deberia estar encriptada";
    }

    @Test
    @DisplayName("POST /login con credenciales invalidas deberia retornar error")
    void testLoginConCredencialesInvalidas() throws Exception {
        Usuario usuario = Usuario.builder()
                .nombre("Test User")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .rol(Usuario.Rol.JUGADOR)
                .activo(true)
                .build();
        usuarioRepository.save(usuario);

        mockMvc.perform(post("/login")
                .with(csrf())
                .param("email", "test@example.com")
                .param("password", "wrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("GET /logout deberia limpiar la sesion")
    void testLogout() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }
}
