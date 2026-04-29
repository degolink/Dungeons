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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests para CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Test User")
                .email("test@example.com")
                .password("hashedPassword")
                .rol(Usuario.Rol.JUGADOR)
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debería cargar usuario por email")
    void testLoadUserByUsernameValido() {
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("hashedPassword", userDetails.getPassword());
        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el usuario no existe")
    void testLoadUserByUsernameNoExistente() {
        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("noexiste@example.com");
        });

        verify(usuarioRepository, times(1)).findByEmail("noexiste@example.com");
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el usuario está inactivo")
    void testLoadUserByUsernameUsuarioInactivo() {
        usuario.setActivo(false);
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("test@example.com");
        });

        verify(usuarioRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Debería asignar rol JUGADOR correctamente")
    void testLoadUserWithJugadorRole() {
        usuario.setRol(Usuario.Rol.JUGADOR);
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertFalse(authorities.isEmpty());
        assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_JUGADOR")));
    }

    @Test
    @DisplayName("Debería asignar rol NARRADOR correctamente")
    void testLoadUserWithNarradorRole() {
        usuario.setRol(Usuario.Rol.NARRADOR);
        when(usuarioRepository.findByEmail("test@example.com")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertFalse(authorities.isEmpty());
        assertTrue(authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_NARRADOR")));
    }
}
