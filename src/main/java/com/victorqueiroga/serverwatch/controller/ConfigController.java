package com.victorqueiroga.serverwatch.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.victorqueiroga.serverwatch.config.KeycloakProperties;
import com.victorqueiroga.serverwatch.config.ServerWatchProperties;

import lombok.RequiredArgsConstructor;

/**
 * Controller para exibir informações de configuração da aplicação
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final KeycloakProperties keycloakProperties;
    private final ServerWatchProperties serverWatchProperties;

    /**
     * Endpoint para exibir informações básicas de configuração
     */
    @GetMapping("/info")
    public Map<String, Object> getConfigInfo() {
        return Map.of(
            "application", Map.of(
                "name", serverWatchProperties.getApplication().getName(),
                "version", serverWatchProperties.getApplication().getVersion(),
                "organization", serverWatchProperties.getApplication().getOrganization(),
                "developmentMode", serverWatchProperties.getApplication().isDevelopmentMode()
            ),
            "keycloak", Map.of(
                "realm", keycloakProperties.getRealm(),
                "resource", keycloakProperties.getResource(),
                "authServerUrl", keycloakProperties.getAuthServerUrl(),
                "issuerUrl", keycloakProperties.getUrls().getIssuer(),
                "jwksUrl", keycloakProperties.getUrls().getJwks()
            ),
            "monitoring", Map.of(
                "serverCheckInterval", serverWatchProperties.getMonitoring().getServerCheckIntervalSeconds(),
                "pingTimeout", serverWatchProperties.getMonitoring().getServerPingTimeoutSeconds(),
                "maxRetryAttempts", serverWatchProperties.getMonitoring().getMaxRetryAttempts()
            ),
            "ui", Map.of(
                "theme", serverWatchProperties.getUi().getTheme(),
                "language", serverWatchProperties.getUi().getDefaultLanguage(),
                "itemsPerPage", serverWatchProperties.getUi().getItemsPerPage(),
                "autoRefresh", serverWatchProperties.getUi().getAutoRefreshSeconds()
            )
        );
    }

    /**
     * Endpoint para exibir URLs calculadas do Keycloak
     */
    @GetMapping("/keycloak-urls")
    public Map<String, String> getKeycloakUrls() {
        return Map.of(
            "auth", keycloakProperties.getUrls().getAuth(),
            "token", keycloakProperties.getUrls().getToken(),
            "userinfo", keycloakProperties.getUrls().getUserinfo(),
            "jwks", keycloakProperties.getUrls().getJwks(),
            "logout", keycloakProperties.getUrls().getLogout(),
            "issuer", keycloakProperties.getUrls().getIssuer(),
            "admin", keycloakProperties.getUrls().getAdmin()
        );
    }

    /**
     * Endpoint para verificar se está em modo desenvolvimento
     */
    @GetMapping("/development-mode")
    public Map<String, Object> getDevelopmentMode() {
        return Map.of(
            "developmentMode", serverWatchProperties.getApplication().isDevelopmentMode(),
            "debug", serverWatchProperties.getApplication().getDebug()
        );
    }
}