package com.manufacturing.manufacturingindex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // QUEM PODE ACESSAR O QUÊ
            .authorizeHttpRequests(auth -> auth
                // H2 liberado
                .requestMatchers("/h2/**").permitAll()
                // tudo o resto precisa estar logado
                .anyRequest().authenticated()
            )
            // LOGIN PADRÃO DO SPRING (sem loginPage customizada)
            .formLogin(form -> form
                .permitAll()   // /login é liberado automaticamente
            )
            // LOGOUT PADRÃO
            .logout(logout -> logout
                .permitAll()
            );

        // Necessário para console H2 funcionar
        http.csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
