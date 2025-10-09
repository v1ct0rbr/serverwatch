package com.victorqueiroga.serverwatch.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provider customizado para autenticação com Keycloak usando JWT
 * Processa tokens JWT do Keycloak e extrai as roles/autoridades
 */
@Component
public class KeycloakAuthenticationProvider implements AuthenticationProvider {

    /**
     * Autentica o usuário usando o token JWT do Keycloak
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken) authentication;
        Jwt jwt = jwtToken.getToken();
        
        // Extrair as autoridades do token JWT
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        
        // Criar um novo token com as autoridades extraídas
        return new JwtAuthenticationToken(jwt, authorities);
    }

    /**
     * Extrai as autoridades/roles do token JWT do Keycloak
     */
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        // Extrair realm_access roles
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        List<String> realmRoles = realmAccess != null ? 
            (List<String>) realmAccess.get("roles") : List.of();

        // Extrair resource_access roles (se necessário para uma aplicação específica)
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        
        // Converter roles do realm em GrantedAuthority
        List<GrantedAuthority> authorities = realmRoles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());

        // Adicionar roles de recursos específicos se necessário
        if (resourceAccess != null) {
            for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                Map<String, Object> resource = (Map<String, Object>) entry.getValue();
                List<String> resourceRoles = (List<String>) resource.get("roles");
                if (resourceRoles != null) {
                    resourceRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .forEach(authorities::add);
                }
            }
        }

        return authorities;
    }

    /**
     * Verifica se este provider suporta o tipo de autenticação
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}