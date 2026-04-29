package com.dungeons_and_dragons.configuration;

import com.dungeons_and_dragons.rol.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/registro", "/error").permitAll()
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/img/**").permitAll()
                        .requestMatchers("/narrador/**").hasRole("NARRADOR")
                        .requestMatchers("/villano", "/villano/**").hasRole("NARRADOR")
                        .requestMatchers("/api/dnd/**").hasRole("NARRADOR")
                        .requestMatchers("/jugador/**", "/batallas/**")
                        .hasAnyRole("JUGADOR", "NARRADOR")
                        .requestMatchers("/personaje/**", "/inventario/**")
                        .hasRole("NARRADOR")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll()
                        .failureUrl("/login?error=true")
                        .successHandler((request, response, authentication) -> {
                            boolean esNarrador = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> "ROLE_NARRADOR".equals(authority.getAuthority()));
                            response.sendRedirect(esNarrador ? "/narrador" : "/batallas");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}







