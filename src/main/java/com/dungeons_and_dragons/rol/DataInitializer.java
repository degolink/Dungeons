package com.dungeons_and_dragons.rol;

import com.dungeons_and_dragons.rol.model.Usuario;
import com.dungeons_and_dragons.rol.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos para crear usuarios de prueba al arrancar la aplicación.
 * 
 * Usuarios creados:
 * - narrador@test.com / password123 (Rol: NARRADOR)
 * - jugador@test.com / password123 (Rol: JUGADOR)
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioService usuarioService;

    @Override
    public void run(String... args) throws Exception {
        // Crear usuario Narrador si no existe
        if (!usuarioService.existeEmail("narrador@test.com")) {
            usuarioService.guardarUsuario(Usuario.builder()
                    .nombre("Narrador Admin")
                    .email("narrador@test.com")
                    .password("password123")
                    .rol(Usuario.Rol.NARRADOR)
                    .activo(true)
                    .build());
            System.out.println("✅ Usuario NARRADOR creado: narrador@test.com / password123");
        }

        // Crear usuario Jugador si no existe
        if (!usuarioService.existeEmail("jugador@test.com")) {
            usuarioService.guardarUsuario(Usuario.builder()
                    .nombre("Jugador Test")
                    .email("jugador@test.com")
                    .password("password123")
                    .rol(Usuario.Rol.JUGADOR)
                    .activo(true)
                    .build());
            System.out.println("✅ Usuario JUGADOR creado: jugador@test.com / password123");
        }
    }
}
