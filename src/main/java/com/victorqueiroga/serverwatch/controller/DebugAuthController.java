package com.victorqueiroga.serverwatch.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.victorqueiroga.serverwatch.model.User;
import com.victorqueiroga.serverwatch.security.AuthDebugService;
import com.victorqueiroga.serverwatch.security.KeycloakUser;
import com.victorqueiroga.serverwatch.security.KeycloakUserService;
import com.victorqueiroga.serverwatch.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para debug da autenticação e sincronização de roles
 * APENAS PARA DESENVOLVIMENTO!
 */
@Slf4j
@Controller
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugAuthController {

    private final AuthDebugService authDebugService;
    private final KeycloakUserService keycloakUserService;
    private final UserService userService;

    @GetMapping("/auth-info")
    @ResponseBody
    public String getAuthInfo() {
        log.info("=== DEBUG AUTH INFO ===");

        authDebugService.logCurrentAuthenticationDetails();

        KeycloakUser keycloakUser = keycloakUserService.getCurrentUser();
        if (keycloakUser != null) {
            authDebugService.logKeycloakUserDetails(keycloakUser);
        }

        return """
                <h1>Informações de Autenticação</h1>
                <p>Verifique os LOGS da aplicação para mais detalhes.</p>
                <h2>Informações Exibidas nos Logs:</h2>
                <ul>
                    <li>Tipo de autenticação</li>
                    <li>Usuário autenticado</li>
                    <li>Authorities extraídas</li>
                    <li>Claims do token JWT</li>
                    <li>Roles encontradas no Keycloak</li>
                    <li>Roles mapeadas para a aplicação</li>
                </ul>
                <p><a href="/">Voltar à Home</a></p>
                """;
    }

    @GetMapping("/user-sync")
    @ResponseBody
    public String getUserSync() {
        log.info("=== DEBUG USER SYNC ===");

        KeycloakUser keycloakUser = keycloakUserService.getCurrentUser();
        if (keycloakUser == null) {
            return "<h1>Erro</h1><p>Usuário não autenticado! Faça login primeiro.</p>";
        }

        authDebugService.logKeycloakUserDetails(keycloakUser);

        try {
            // Simular sincronização
            User localUser = userService.getOrCreateUser(keycloakUser);

            if (localUser != null) {
                authDebugService.logUserSyncDetails(keycloakUser, localUser);

                return """
                        <h1>Sincronização de Usuário</h1>
                        <h2>Keycloak User:</h2>
                        <ul>
                            <li>ID: %s</li>
                            <li>Username: %s</li>
                            <li>Email: %s</li>
                            <li>Authorities: %s</li>
                        </ul>
                        <h2>Local User:</h2>
                        <ul>
                            <li>ID: %s</li>
                            <li>Keycloak ID: %s</li>
                            <li>Username: %s</li>
                            <li>Email: %s</li>
                            <li>Application Roles: %s</li>
                        </ul>
                        <p style="color: green;"><strong>✓ Sincronização bem-sucedida!</strong></p>
                        <p><a href="/debug/auth-info">Ver detalhes de autenticação</a></p>
                        <p><a href="/">Voltar à Home</a></p>
                        """.formatted(
                        keycloakUser.getId(),
                        keycloakUser.getUsername(),
                        keycloakUser.getEmail(),
                        keycloakUser.getAuthorities(),
                        localUser.getId(),
                        localUser.getKeycloakId(),
                        localUser.getUsername(),
                        localUser.getEmail(),
                        localUser.getApplicationRoles());
            }
        } catch (Exception e) {
            log.error("Erro ao sincronizar usuário", e);
            return """
                    <h1>Erro na Sincronização</h1>
                    <p style="color: red;"><strong>✗ Erro!</strong></p>
                    <pre>%s</pre>
                    <p><a href="/">Voltar à Home</a></p>
                    """.formatted(e.getMessage());
        }

        return "<p>Erro desconhecido</p>";
    }

    @GetMapping("/current-user")
    public String getCurrentUser(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        KeycloakUser keycloakUser = keycloakUserService.getCurrentUser();

        model.addAttribute("authentication", authentication);
        model.addAttribute("keycloakUser", keycloakUser);

        if (keycloakUser != null) {
            try {
                User localUser = userService.getOrCreateUser(keycloakUser);
                model.addAttribute("localUser", localUser);
            } catch (Exception e) {
                log.error("Erro ao buscar usuário local", e);
            }
        }

        return "debug/current-user";
    }
}
