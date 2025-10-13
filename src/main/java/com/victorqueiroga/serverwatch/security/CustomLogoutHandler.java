package com.victorqueiroga.serverwatch.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler customizado para logout que limpa adequadamente
 * as sessões OAuth2/OIDC com Keycloak
 */
@Slf4j
@Component
public class CustomLogoutHandler implements LogoutHandler {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, 
                      Authentication authentication) {
        
        if (authentication == null) {
            log.info("Logout chamado sem autenticação ativa");
            return;
        }

        log.info("Executando logout customizado para usuário: {}", authentication.getName());
        
        // Limpar informações específicas do OAuth2/OIDC
        if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            log.info("Limpando sessão OIDC para usuário: {}", oidcUser.getPreferredUsername());
            
            // Aqui podemos adicionar lógica específica para limpar tokens ou caches
            // se necessário
        }
        
        // Invalidar sessão HTTP
        if (request.getSession(false) != null) {
            log.info("Invalidando sessão HTTP: {}", request.getSession().getId());
            request.getSession().invalidate();
        }
        
        // Limpar cookies relacionados à autenticação
        if (response != null) {
            // Limpar cookie JSESSIONID
            jakarta.servlet.http.Cookie sessionCookie = new jakarta.servlet.http.Cookie("JSESSIONID", null);
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(0);
            response.addCookie(sessionCookie);
            
            log.info("Cookies de sessão removidos");
        }
        
        log.info("Logout customizado finalizado para usuário: {}", authentication.getName());
    }
}