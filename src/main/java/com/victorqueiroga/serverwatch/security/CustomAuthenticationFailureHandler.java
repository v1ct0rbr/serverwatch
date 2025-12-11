package com.victorqueiroga.serverwatch.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Handler customizado para falhas de autenticação OAuth2
 * Fornece melhor logging e detecção de timeout vs erro real
 */
@Slf4j
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String errorCode = "true";
        String errorMessage = "Erro desconhecido";

        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            errorCode = oauth2Ex.getError().getErrorCode();
            errorMessage = oauth2Ex.getError().getDescription();

            log.warn("OAuth2 Authentication Failed - Code: {}, Message: {}", errorCode, errorMessage);

            // Detectar timeouts específicos
            if (exception.getCause() != null) {
                String causeMessage = exception.getCause().getMessage();
                if (causeMessage != null && (causeMessage.contains("timeout") ||
                        causeMessage.contains("Socket timeout") ||
                        causeMessage.contains("Connection timeout"))) {
                    log.warn("Detectado timeout na comunicação com Keycloak: {}", causeMessage);
                    errorCode = "timeout";
                }
            }
        } else {
            log.warn("Authentication Failed - Exception: {}", exception.getMessage(), exception);
            errorMessage = exception.getMessage();
        }

        // Redirecionar para login com código de erro específico
        getRedirectStrategy().sendRedirect(request, response, "/login?error=" + errorCode);
    }
}
