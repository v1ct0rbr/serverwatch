package com.victorqueiroga.serverwatch.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victorqueiroga.serverwatch.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de debug para entender o fluxo de autenticação e roles
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthDebugService {

    private final ObjectMapper objectMapper;

    public void logCurrentAuthenticationDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("=== AUTHENTICATION DEBUG INFO ===");
        log.info("Authentication type: {}",
                authentication != null ? authentication.getClass().getSimpleName() : "null");
        log.info("Is authenticated: {}", authentication != null ? authentication.isAuthenticated() : false);

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            log.info("OAuth2 Token Name: {}", oauth2Token.getName());

            Object principal = oauth2Token.getPrincipal();
            log.info("Principal type: {}", principal != null ? principal.getClass().getSimpleName() : "null");

            if (principal instanceof OidcUser oidcUser) {
                logOidcUserDetails(oidcUser);
            }

            log.info("Authorities from OAuth2Token:");
            oauth2Token.getAuthorities().forEach(auth -> log.info("  - {}", auth.getAuthority()));
        }

        log.info("=== END AUTHENTICATION DEBUG ===");
    }

    private void logOidcUserDetails(OidcUser oidcUser) {
        log.info("OidcUser Details:");
        log.info("  Subject: {}", oidcUser.getSubject());
        log.info("  Preferred Username: {}", oidcUser.getPreferredUsername());
        log.info("  Email: {}", oidcUser.getEmail());
        log.info("  Name: {}", oidcUser.getFullName());
        log.info("  Email Verified: {}", oidcUser.getEmailVerified());

        // Log all claims
        log.info("  All Claims:");
        oidcUser.getClaims().forEach((key, value) -> {
            if (key.contains("role") || key.contains("scope") || key.contains("access")) {
                log.info("    {}: {}", key, value);
            }
        });

        // Log ID Token details
        if (oidcUser.getIdToken() != null) {
            OidcIdToken idToken = oidcUser.getIdToken();
            log.info("  ID Token Claims:");

            // Check for realm_access
            Object realmAccess = idToken.getClaims().get("realm_access");
            if (realmAccess != null) {
                log.info("    realm_access: {}", realmAccess);
            }

            // Check for resource_access
            Object resourceAccess = idToken.getClaims().get("resource_access");
            if (resourceAccess != null) {
                log.info("    resource_access: {}", resourceAccess);
            }

            // Check for client_id
            log.info("    client_id: {}", idToken.getClaimAsString("client_id"));
            log.info("    aud: {}", idToken.getAudience());
        }

        // Log authorities
        log.info("  Authorities from OidcUser:");
        oidcUser.getAuthorities().forEach(auth -> log.info("    - {}", auth.getAuthority()));
    }

    public void logKeycloakUserDetails(KeycloakUser keycloakUser) {
        if (keycloakUser == null) {
            log.warn("KeycloakUser is null!");
            return;
        }

        log.info("=== KEYCLOAK USER DEBUG INFO ===");
        log.info("ID: {}", keycloakUser.getId());
        log.info("Username: {}", keycloakUser.getUsername());
        log.info("Email: {}", keycloakUser.getEmail());
        log.info("Full Name: {}", keycloakUser.getFullName());
        log.info("Enabled: {}", keycloakUser.isEnabled());
        log.info("Email Verified: {}", keycloakUser.isEmailVerified());

        log.info("Authorities:");
        keycloakUser.getAuthorities().forEach(auth -> {
            log.info("  - {}", auth.getAuthority());
        });

        log.info("Role Checks:");
        String[] rolesToCheck = { "SERVERWATCH_USER", "SERVERWATCH_MONITOR" };
        for (String role : rolesToCheck) {
            log.info("  hasRole('{}'): {}", role, keycloakUser.hasRole(role));
        }

        log.info("=== END KEYCLOAK USER DEBUG ===");
    }

    public void logUserSyncDetails(KeycloakUser keycloakUser, User localUser) {
        log.info("=== USER SYNC DEBUG INFO ===");
        log.info("Keycloak User ID: {}", keycloakUser.getId());
        log.info("Keycloak Username: {}", keycloakUser.getUsername());
        log.info("Keycloak Authorities: {}", keycloakUser.getAuthorities());

        log.info("Local User ID: {}", localUser.getId());
        log.info("Local User Keycloak ID: {}", localUser.getKeycloakId());
        log.info("Local User Username: {}", localUser.getUsername());
        log.info("Local User Application Roles: {}", localUser.getApplicationRoles());

        log.info("Comparison:");
        log.info("  IDs match: {}", keycloakUser.getId().equals(localUser.getKeycloakId()));
        log.info("  Usernames match: {}", keycloakUser.getUsername().equals(localUser.getUsername()));

        log.info("=== END USER SYNC DEBUG ===");
    }
}
