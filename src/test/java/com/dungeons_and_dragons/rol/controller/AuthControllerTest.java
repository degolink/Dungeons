package com.dungeons_and_dragons.rol.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.dungeons_and_dragons.rol.model.Usuario;
import com.dungeons_and_dragons.rol.service.UsuarioService;

/**
 * Tests unitarios de AuthController con Mockito.
 *
 * Nota: Spring Security gestiona por sí mismo POST /login y GET /logout,
 * por lo que esos flujos se prueban en SecurityIntegrationTest.
 * Aquí solo se prueban los métodos que el controlador define:
 *   - GET  /login
 *   - GET  /registro
 *   - POST /registro
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController — Tests unitarios")
class AuthControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Model model;

    @InjectMocks
    private AuthController authController;

    private Usuario usuarioBase;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    void setUp() {
        redirectAttributes = new RedirectAttributesModelMap();
        usuarioBase = Usuario.builder()
                .id(1L)
                .nombre("Test User")
                .email("test@example.com")
                .password("hashedPassword")
                .rol(Usuario.Rol.JUGADOR)
                .activo(true)
                .build();
    }

    // =========================================================================
    // GET /login
    // =========================================================================

    @Nested
    @DisplayName("GET /login")
    class GetLogin {

        @Test
        @DisplayName("sin parámetros devuelve la vista 'login' sin atributos de error")
        void sinParametros_devuelveVistaLogin() {
            String vista = authController.login(null, null, model);

            assertEquals("login", vista);
            verify(model, never()).addAttribute(any(), any());
        }

        @Test
        @DisplayName("con param error expone mensaje de error")
        void conParamError_exponeMensaje() {
            String vista = authController.login("true", null, model);

            assertEquals("login", vista);
            verify(model, times(1)).addAttribute("error", "Email o contrasena incorrectos");
        }

        @Test
        @DisplayName("con param logout expone mensaje de éxito")
        void conParamLogout_exponeMensajeExito() {
            String vista = authController.login(null, "true", model);

            assertEquals("login", vista);
            verify(model, times(1)).addAttribute("success", "Sesion cerrada correctamente");
        }
    }

    // =========================================================================
    // GET /registro
    // =========================================================================

    @Nested
    @DisplayName("GET /registro")
    class GetRegistro {

        @Test
        @DisplayName("devuelve la vista 'registro' e inyecta un Usuario vacío en el modelo")
        void devuelveVistaRegistro() {
            String vista = authController.registro(model);

            assertEquals("registro", vista);
            verify(model, times(1)).addAttribute(any(String.class), any(Usuario.class));
        }
    }

    // =========================================================================
    // POST /registro
    // =========================================================================

    @Nested
    @DisplayName("POST /registro")
    class PostRegistro {

        @Test
        @DisplayName("contraseñas que no coinciden → vuelve a 'registro' con error")
        void contrasenasNoCoinciden_devuelveError() {
            String vista = authController.processRegistro(
                    "New User", "new@example.com",
                    "password123", "diferente",
                    model, redirectAttributes);

            assertEquals("registro", vista);
            verify(model, times(1)).addAttribute("error", "Las contrasenas no coinciden");
            verify(usuarioService, never()).guardarUsuario(any());
        }

        @Test
        @DisplayName("email ya registrado → vuelve a 'registro' con error")
        void emailExistente_devuelveError() {
            when(usuarioService.existeEmail("existing@example.com")).thenReturn(true);

            String vista = authController.processRegistro(
                    "New User", "existing@example.com",
                    "password123", "password123",
                    model, redirectAttributes);

            assertEquals("registro", vista);
            verify(model, times(1)).addAttribute("error", "El email ya esta registrado");
            verify(usuarioService, never()).guardarUsuario(any());
        }

        @Test
        @DisplayName("datos válidos → guarda usuario y redirige a /login")
        void datosValidos_guardaYRedirige() {
            when(usuarioService.existeEmail("new@example.com")).thenReturn(false);
            when(usuarioService.guardarUsuario(any(Usuario.class))).thenReturn(usuarioBase);

            String vista = authController.processRegistro(
                    "New User", "new@example.com",
                    "password123", "password123",
                    model, redirectAttributes);

            assertEquals("redirect:/login", vista);
            verify(usuarioService, times(1)).guardarUsuario(any(Usuario.class));
        }

        @Test
        @DisplayName("datos válidos → el usuario guardado tiene rol JUGADOR y está activo")
        void datosValidos_usuarioTieneRolYEstadoCorrectos() {
            when(usuarioService.existeEmail("new@example.com")).thenReturn(false);
            when(usuarioService.guardarUsuario(any(Usuario.class))).thenAnswer(inv -> {
                Usuario u = inv.getArgument(0);
                assertEquals(Usuario.Rol.JUGADOR, u.getRol());
                assertEquals(true, u.getActivo());
                assertEquals("new@example.com", u.getEmail());
                return u;
            });

            authController.processRegistro(
                    "New User", "new@example.com",
                    "password123", "password123",
                    model, redirectAttributes);

            verify(usuarioService, times(1)).guardarUsuario(any(Usuario.class));
        }

        @Test
        @DisplayName("datos válidos → añade flash attribute de éxito")
        void datosValidos_anadeFlashSuccess() {
            when(usuarioService.existeEmail("new@example.com")).thenReturn(false);
            when(usuarioService.guardarUsuario(any(Usuario.class))).thenReturn(usuarioBase);

            authController.processRegistro(
                    "New User", "new@example.com",
                    "password123", "password123",
                    model, redirectAttributes);

            assertEquals("Registro exitoso. Por favor, inicia sesion.",
                    redirectAttributes.getFlashAttributes().get("success"));
        }
    }
}
