package com.dungeons_and_dragons.rol.controller;

import com.dungeons_and_dragons.rol.model.Usuario;
import com.dungeons_and_dragons.rol.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Email o contrasena incorrectos");
        }
        if (logout != null) {
            model.addAttribute("success", "Sesion cerrada correctamente");
        }
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String processRegistro(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contrasenas no coinciden");
            return "registro";
        }

        if (usuarioService.existeEmail(email)) {
            model.addAttribute("error", "El email ya esta registrado");
            return "registro";
        }

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(nombre)
                .email(email)
                .password(password)
                .rol(Usuario.Rol.JUGADOR)
                .activo(true)
                .build();

        usuarioService.guardarUsuario(nuevoUsuario);
        redirectAttributes.addFlashAttribute("success", "Registro exitoso. Por favor, inicia sesion.");
        return "redirect:/login";
    }
}
