package com.victorqueiroga.serverwatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança simples para desenvolvimento (sem Keycloak)
 */
@Configuration
@EnableWebSecurity
@Profile("dev")
public class DevSecurityConfiguration {

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions().sameOrigin()) // Para o console H2
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers(
                    "/",
                    "/login**",
                    "/logout**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/lib/**",
                    "/error/**",
                    "/h2-console/**",
                    "/test-api.html",
                    "/favicon.ico").permitAll()
                
                // APIs e páginas com autenticação simples
                .anyRequest().authenticated())
            
            .httpBasic(httpBasic -> httpBasic.realmName("ServerWatch Dev"))
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());

        return http.build();
    }

    @Bean
    public UserDetailsService devUserDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin"))
                .roles("ADMIN", "USER")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("user"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}