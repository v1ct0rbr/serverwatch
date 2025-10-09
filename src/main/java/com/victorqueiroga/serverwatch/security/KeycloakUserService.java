package com.victorqueiroga.serverwatch.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Serviço para extrair informações do usuário autenticado via Keycloak
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserService {
    
    private final KeycloakJwtAuthenticationConverter jwtConverter;
    
    /**
     * Obtém o usuário atualmente autenticado
     */
    public KeycloakUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return createKeycloakUserFromJwt(jwtToken.getToken());
        }
        
        return null;
    }
    
    /**
     * Cria um KeycloakUser a partir do token JWT
     */
    private KeycloakUser createKeycloakUserFromJwt(Jwt jwt) {
        try {
            // Extrair informações básicas do JWT
            String id = jwt.getClaimAsString("sub");
            String username = jwt.getClaimAsString("preferred_username");
            String email = jwt.getClaimAsString("email");
            String firstName = jwt.getClaimAsString("given_name");
            String lastName = jwt.getClaimAsString("family_name");
            Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
            
            // Extrair authorities usando o converter
            Set<GrantedAuthority> authorities = new HashSet<>(jwtConverter.convert(jwt));
            
            return KeycloakUser.builder()
                .id(id)
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .enabled(true) // Se chegou até aqui, o token é válido
                .emailVerified(emailVerified != null ? emailVerified : false)
                .authorities(authorities)
                .build();
                
        } catch (Exception e) {
            log.error("Erro ao criar KeycloakUser a partir do JWT", e);
            return null;
        }
    }
    
    /**
     * Verifica se o usuário atual tem uma role específica
     */
    public boolean currentUserHasRole(String role) {
        KeycloakUser user = getCurrentUser();
        return user != null && user.hasRole(role);
    }
    
    /**
     * Verifica se o usuário atual tem qualquer uma das roles especificadas
     */
    public boolean currentUserHasAnyRole(String... roles) {
        KeycloakUser user = getCurrentUser();
        return user != null && user.hasAnyRole(roles);
    }
    
    /**
     * Obtém o ID do usuário atual
     */
    public String getCurrentUserId() {
        KeycloakUser user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * Obtém o email do usuário atual
     */
    public String getCurrentUserEmail() {
        KeycloakUser user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
    
    /**
     * Obtém o nome completo do usuário atual
     */
    public String getCurrentUserFullName() {
        KeycloakUser user = getCurrentUser();
        return user != null ? user.getFullName() : null;
    }
    
    /**
     * Verifica se há um usuário autenticado
     */
    public boolean isUserAuthenticated() {
        return getCurrentUser() != null;
    }
}