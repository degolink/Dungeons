package com.dungeons_and_dragons.rol.service;

import com.dungeons_and_dragons.rol.model.Usuario;
import com.dungeons_and_dragons.rol.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + email);
        }

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                mapRoleToAuthority(usuario.getRol())
        );
    }

    private Collection<? extends GrantedAuthority> mapRoleToAuthority(Usuario.Rol rol) {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }
}
