package com.victorqueiroga.serverwatch.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler de sucesso do logout que redireciona para o endpoint
 * de logout do Keycloak para finalizar a sessão SSO
 */
@Slf4j
@Component
public class KeycloakLogoutSuccessHandler implements LogoutSuccessHandler {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.resource}")
    private String clientId;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, 
                               Authentication authentication) throws IOException, ServletException {
        
        log.info("Logout local bem-sucedido, redirecionando para Keycloak");
        
        // Construir URL base da aplicação
        String baseUrl = getBaseUrl(request);
        
        // Construir URL de logout do Keycloak
        String keycloakLogoutUrl = UriComponentsBuilder
                .fromUriString(keycloakServerUrl)
                .path("/realms/{realm}/protocol/openid-connect/logout")
                .queryParam("post_logout_redirect_uri", baseUrl + "/login?logout=true")
                .queryParam("client_id", clientId)
                .buildAndExpand(realm)
                .toUriString();
        
        log.info("Redirecionando para logout do Keycloak: {}", keycloakLogoutUrl);
        
        // Redirecionar para o Keycloak
        response.sendRedirect(keycloakLogoutUrl);
    }
    
    /**
     * Constrói a URL base da aplicação
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        
        // Adicionar porta apenas se não for a padrão
        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }
        
        return url.toString();
    }
}