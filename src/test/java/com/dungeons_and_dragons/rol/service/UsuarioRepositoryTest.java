package com.dungeons_and_dragons.rol.service;

import com.dungeons_and_dragons.rol.model.Usuario;
import com.dungeons_and_dragons.rol.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para UsuarioRepository (Unit Tests)")
class UsuarioRepositoryTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Usuario Test")
                .email("usuario@test.com")
                .password("password123")
                .rol(Usuario.Rol.JUGADOR)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debería guardar un usuario")
    void testGuardarUsuario() {
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario guardado = usuarioRepository.save(usuario);

        assertNotNull(guardado);
        assertEquals("Usuario Test", guardado.getNombre());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Debería encontrar un usuario por email")
    void testFindByEmail() {
        when(usuarioRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("usuario@test.com");

        assertTrue(encontrado.isPresent());
        assertEquals("Usuario Test", encontrado.get().getNombre());
        verify(usuarioRepository, times(1)).findByEmail("usuario@test.com");
    }

    @Test
    @DisplayName("Debería retornar vacío cuando el email no existe")
    void testFindByEmailNoExistente() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("noexiste@test.com");

        assertFalse(encontrado.isPresent());
        verify(usuarioRepository, times(1)).findByEmail("noexiste@test.com");
    }

    @Test
    @DisplayName("Debería validar que existe un email")
    void testExistsByEmail() {
        when(usuarioRepository.existsByEmail("usuario@test.com")).thenReturn(true);

        boolean existe = usuarioRepository.existsByEmail("usuario@test.com");

        assertTrue(existe);
        verify(usuarioRepository, times(1)).existsByEmail("usuario@test.com");
    }

    @Test
    @DisplayName("Debería retornar false cuando el email no existe")
    void testExistsByEmailNoExistente() {
        when(usuarioRepository.existsByEmail("noexiste@test.com")).thenReturn(false);

        boolean existe = usuarioRepository.existsByEmail("noexiste@test.com");

        assertFalse(existe);
        verify(usuarioRepository, times(1)).existsByEmail("noexiste@test.com");
    }
}
