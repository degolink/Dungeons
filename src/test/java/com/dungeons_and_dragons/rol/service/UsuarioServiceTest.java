package com.dungeons_and_dragons.rol.service;

import com.dungeons_and_dragons.rol.model.Usuario;
import com.dungeons_and_dragons.rol.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Test Usuario")
                .email("test@example.com")
                .password("password123")
                .rol(Usuario.Rol.JUGADOR)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debería guardar un usuario con contraseña encriptada")
    void testGuardarUsuario() {
        Usuario usuarioParaGuardar = Usuario.builder()
                .nombre("Nuevo Usuario")
                .email("nuevo@example.com")
                .password("password123")
                .rol(Usuario.Rol.NARRADOR)
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("encriptedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioParaGuardar);

        Usuario resultado = usuarioService.guardarUsuario(usuarioParaGuardar);

        assertNotNull(resultado);
        assertEquals("Nuevo Usuario", resultado.getNombre());
        assertEquals("nuevo@example.com", resultado.getEmail());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioRepository, times(1)).save(usuarioParaGuardar);
    }

    @Test
    @DisplayName("Debería obtener un usuario por email")
    void testObtenerPorEmail() {
        when(usuarioRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.obtenerPorEmail("test@example.com");

        assertTrue(resultado.isPresent());
        assertEquals("Test Usuario", resultado.get().getNombre());
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Debería retornar vacío cuando el email no existe")
    void testObtenerPorEmailNoExistente() {
        when(usuarioRepository.findByEmail("noexiste@example.com"))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.obtenerPorEmail("noexiste@example.com");

        assertFalse(resultado.isPresent());
        verify(usuarioRepository, times(1)).findByEmail("noexiste@example.com");
    }

    @Test
    @DisplayName("Debería verificar que existe un email")
    void testExisteEmail() {
        when(usuarioRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean existe = usuarioService.existeEmail("test@example.com");

        assertTrue(existe);
        verify(usuarioRepository, times(1)).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Debería retornar false cuando el email no existe")
    void testNoExisteEmail() {
        when(usuarioRepository.existsByEmail("noexiste@example.com")).thenReturn(false);

        boolean existe = usuarioService.existeEmail("noexiste@example.com");

        assertFalse(existe);
        verify(usuarioRepository, times(1)).existsByEmail("noexiste@example.com");
    }

    @Test
    @DisplayName("Debería validar credenciales correctas")
    void testValidarCredencialesCorrectas() {
        when(usuarioRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", usuario.getPassword()))
                .thenReturn(true);

        boolean valido = usuarioService.validarCredenciales("test@example.com", "password123");

        assertTrue(valido);
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("password123", usuario.getPassword());
    }

    @Test
    @DisplayName("Debería rechazar credenciales incorrectas")
    void testValidarCredencialesIncorrectas() {
        when(usuarioRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("passwordIncorrecto", usuario.getPassword()))
                .thenReturn(false);

        boolean valido = usuarioService.validarCredenciales("test@example.com", "passwordIncorrecto");

        assertFalse(valido);
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).matches("passwordIncorrecto", usuario.getPassword());
    }

    @Test
    @DisplayName("Debería rechazar login de usuario inactivo")
    void testValidarCredencialesUsuarioInactivo() {
        usuario.setActivo(false);
        when(usuarioRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(usuario));

        boolean valido = usuarioService.validarCredenciales("test@example.com", "password123");

        assertFalse(valido);
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Debería retornar false cuando el usuario no existe")
    void testValidarCredencialesUsuarioNoExistente() {
        when(usuarioRepository.findByEmail("noexiste@example.com"))
                .thenReturn(Optional.empty());

        boolean valido = usuarioService.validarCredenciales("noexiste@example.com", "password123");

        assertFalse(valido);
        verify(usuarioRepository, times(1)).findByEmail("noexiste@example.com");
    }

    @Test
    @DisplayName("Debería obtener un usuario por ID")
    void testObtenerPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Test Usuario", resultado.get().getNombre());
        verify(usuarioRepository, times(1)).findById(1L);
    }
}
