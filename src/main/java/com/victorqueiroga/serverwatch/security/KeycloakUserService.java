package com.victorqueiroga.serverwatch.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para extrair informações do usuário autenticado via Keycloak
 */
@Slf4j
@Service
public class KeycloakUserService {

    /**
     * Obtém o usuário atualmente autenticado via OAuth2/OIDC
     */
    public KeycloakUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            Object principal = oauth2Token.getPrincipal();

            if (principal instanceof OidcUser oidcUser) {
                return createKeycloakUserFromOidcUser(oidcUser);
            }
        }

        log.warn("Usuário não está autenticado via OAuth2/OIDC. Tipo de autenticação: {}",
                authentication != null ? authentication.getClass().getSimpleName() : "null");
        return null;
    }

    /**
     * Cria um KeycloakUser a partir do OidcUser
     */
    private KeycloakUser createKeycloakUserFromOidcUser(OidcUser oidcUser) {
        try {
            // Extrair informações básicas do ID Token
            OidcIdToken idToken = oidcUser.getIdToken();

            // ========== DETAILED TOKEN CLAIMS DEBUG ==========
            log.info("═══════════════════════════════════════════════════════════════════");
            log.info("[KeycloakUserService] EXAMINANDO TODOS OS CLAIMS DO TOKEN JWT");
            log.info("═══════════════════════════════════════════════════════════════════");

            idToken.getClaims().forEach((key, value) -> {
                String valueStr = String.valueOf(value);
                if (valueStr.length() > 300) {
                    valueStr = valueStr.substring(0, 300) + "...";
                }
                log.info("  {} = {}", key, valueStr);
            });

            // Check specific claims
            log.info("[KeycloakUserService] VERIFICANDO CLAIMS ESPECÍFICOS:");
            log.info("  realm_access: {}", idToken.getClaimAsMap("realm_access"));
            log.info("  resource_access: {}", idToken.getClaimAsMap("resource_access"));
            log.info("  roles: {}", idToken.getClaims().get("roles"));
            log.info("  groups: {}", idToken.getClaims().get("groups"));
            log.info("═══════════════════════════════════════════════════════════════════");

            String id = idToken.getSubject();
            String username = idToken.getClaimAsString("preferred_username");
            String email = idToken.getEmail();
            String firstName = idToken.getGivenName();
            String lastName = idToken.getFamilyName();
            Boolean emailVerified = idToken.getEmailVerified();

            // Extrair authorities do principal (já foram processadas pelo
            // CustomOidcUserService)
            Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

            log.info("[KeycloakUserService] Authorities extraídas do OidcUser: {}",
                    authorities.stream().map(GrantedAuthority::getAuthority).toList());

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
            log.error("Erro ao criar KeycloakUser a partir do OidcUser", e);
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