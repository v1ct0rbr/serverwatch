package com.victorqueiroga.serverwatch.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.victorqueiroga.serverwatch.security.CustomLogoutHandler;
import com.victorqueiroga.serverwatch.security.CustomOidcUserService;
import com.victorqueiroga.serverwatch.security.KeycloakJwtAuthenticationConverter;
import com.victorqueiroga.serverwatch.security.KeycloakLogoutSuccessHandler;

import lombok.RequiredArgsConstructor;

/**
 * Configuração de segurança integrada com Keycloak
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    private final KeycloakJwtAuthenticationConverter keycloakJwtConverter;
    private final CustomLogoutHandler customLogoutHandler;
    private final KeycloakLogoutSuccessHandler logoutSuccessHandler;
    private final CustomOidcUserService customOidcUserService;

    /**
     * Configuração do decoder JWT para Keycloak
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * Configuração do converter JWT para extrair authorities do Keycloak
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(keycloakJwtConverter);
        return converter;
    }

    /**
     * Configuração da cadeia de filtros de segurança
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            
            // Configuração OAuth2 Login para Keycloak
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(customOidcUserService)))
            
            // Configuração de logout com handlers customizados
            .logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler(logoutSuccessHandler)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID"))
            
            // Configuração de autorização de requisições
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers(
                    "/",
                    "/login**",
                    "/logout**",
                    "/oauth2/**",
                    "/public/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/lib/**",
                    "/error/**",
                    "/debug/**",
                    "/favicon.ico",
                    "/api/test/**").permitAll()
                
                // Recursos que requerem autenticação
                .requestMatchers("/dashboard", "/servers/**", "/monitoring/**").hasAnyRole("USER", "ADMIN")
                
                // APIs REST que requerem autenticação
                .requestMatchers("/api/monitoring/**", "/api/servers/**").hasAnyRole("USER", "ADMIN")
                
                // Recursos administrativos
                .requestMatchers("/admin/**", "/settings/**").hasRole("ADMIN")
                
                // Qualquer outra requisição requer autenticação
                .anyRequest().authenticated());

        return http.build();
    }

    /**
     * Configuração CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
