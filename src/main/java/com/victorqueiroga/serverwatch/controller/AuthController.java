package com.victorqueiroga.serverwatch.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.victorqueiroga.serverwatch.model.User;
import com.victorqueiroga.serverwatch.security.KeycloakUser;
import com.victorqueiroga.serverwatch.security.KeycloakUserService;
import com.victorqueiroga.serverwatch.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller responsável por gerenciar autenticação e autorização
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    private final KeycloakUserService keycloakUserService;
    private final UserService userService;

    /**
     * Página de login
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Authentication authentication,
                           Model model) {
        
        log.info("Acesso à página de login - Authentication: {}, Error: {}, Logout: {}", 
                 authentication != null ? authentication.getClass().getSimpleName() : "null", error, logout);
        
        // Verificação mais robusta para OAuth2
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser") &&
            authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            log.info("Usuário OAuth2 já autenticado: {} - redirecionando para dashboard", authentication.getName());
            return "redirect:/dashboard";
        }
        
        if (error != null) {
            model.addAttribute("error", "Erro na autenticação. Tente novamente.");
            log.warn("Erro de autenticação detectado");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Logout realizado com sucesso.");
        }
        
        // URL de login do Keycloak
        String keycloakLoginUrl = "/oauth2/authorization/keycloak";
        model.addAttribute("keycloakLoginUrl", keycloakLoginUrl);
        
        return "pages/login";
    }

    /**
     * Página inicial - redireciona para dashboard se autenticado ou login se não autenticado
     */
    @GetMapping("/")
    public String homePage(HttpServletRequest request, Authentication authentication) {
        log.info("Acesso à página inicial - Authentication: {}, Principal: {}", 
                 authentication != null ? authentication.getClass().getSimpleName() : "null",
                 authentication != null ? authentication.getName() : "null");
        
        // Verificar se está autenticado com OAuth2
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            
            // Verificar se é OAuth2AuthenticationToken (login via Keycloak)
            if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                log.info("Usuário OAuth2 autenticado: {} - redirecionando para dashboard", authentication.getName());
                return "redirect:/dashboard";
            }
            
            log.info("Usuário autenticado (não OAuth2): {} - redirecionando para dashboard", authentication.getName());
            return "redirect:/dashboard";
        }
        
        log.info("Usuário não autenticado - redirecionando para login");
        return "redirect:/login";
    }

    /**
     * Dashboard principal
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        try {
            log.info("Tentativa de acesso ao dashboard - Authentication: {}, Principal: {}", 
                     authentication != null ? authentication.getClass().getSimpleName() : "null",
                     authentication != null ? authentication.getName() : "null");
            
            // Verificar autenticação primeiro
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                log.warn("Usuário não autenticado tentando acessar dashboard - redirecionando para login");
                return "redirect:/login?error=authentication_required";
            }
            
            // Verificar se é OAuth2AuthenticationToken
            if (!(authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken)) {
                log.warn("Authentication não é OAuth2AuthenticationToken: {} - redirecionando para login", 
                         authentication.getClass().getSimpleName());
                return "redirect:/login?error=invalid_authentication";
            }
            
            log.info("Usuário OAuth2 autenticado acessando dashboard: {}", authentication.getName());
            
            // Obter ou criar usuário local integrado com Keycloak
            User localUser = userService.getOrCreateUser();
            KeycloakUser keycloakUser = keycloakUserService.getCurrentUser();
            
            if (keycloakUser == null) {
                log.warn("KeycloakUser é null para usuário autenticado: {}", authentication.getName());
                return "redirect:/login";
            }
            
            model.addAttribute("title", "Dashboard");
            model.addAttribute("user", keycloakUser); // Para compatibilidade com templates
            model.addAttribute("localUser", localUser); // Usuário local com dados específicos
            model.addAttribute("userName", localUser.getFullName() != null ? localUser.getFullName() : keycloakUser.getFullName());
            model.addAttribute("userEmail", localUser.getEmail());
            
            // Estatísticas de usuários
            UserService.UserStats userStats = userService.getUserStats();
            model.addAttribute("userStats", userStats);
            
            // Adicionar estatísticas fictícias (substituir por dados reais)
            model.addAttribute("totalServers", 0);
            model.addAttribute("onlineServers", 0);
            model.addAttribute("offlineServers", 0);
            model.addAttribute("pendingAlerts", 0);
            
            return "pages/dashboard";
        } catch (Exception e) {
            log.error("Erro ao carregar dashboard", e);
            return "redirect:/login?error=true";
        }
    }

    /**
     * Logout customizado que redireciona para o Keycloak
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, 
                        Authentication authentication) {
        
        // Logout local da aplicação
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        
        // URL de logout do Keycloak
        String keycloakLogoutUrl = keycloakServerUrl + "/realms/" + realm + 
                                  "/protocol/openid-connect/logout?redirect_uri=" + 
                                  getBaseUrl(request) + "/login?logout=true";
        
        return "redirect:" + keycloakLogoutUrl;
    }

    /**
     * Página de acesso negado
     */
    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("title", "Acesso Negado");
        KeycloakUser user = keycloakUserService.getCurrentUser();
        if (user != null) {
            model.addAttribute("user", user);
            try {
                User localUser = userService.getOrCreateUser();
                model.addAttribute("localUser", localUser);
            } catch (Exception e) {
                log.warn("Erro ao obter usuário local", e);
            }
        }
        return "error/access-denied";
    }

    /**
     * Informações do usuário atual
     */
    @GetMapping("/profile")
    public String userProfile(Model model) {
        try {
            User localUser = userService.getOrCreateUser();
            KeycloakUser keycloakUser = keycloakUserService.getCurrentUser();
            
            if (keycloakUser == null) {
                return "redirect:/login";
            }
            
            model.addAttribute("title", "Meu Perfil");
            model.addAttribute("user", keycloakUser); // Para compatibilidade com templates
            model.addAttribute("localUser", localUser); // Dados específicos da aplicação
            
            return "pages/profile";
        } catch (Exception e) {
            log.error("Erro ao carregar perfil do usuário", e);
            return "redirect:/login?error=true";
        }
    }

    /**
     * Obtém a URL base da aplicação
     */
    private String getBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + 
               (request.getServerPort() != 80 && request.getServerPort() != 443 ? 
                ":" + request.getServerPort() : "") + request.getContextPath();
    }
}