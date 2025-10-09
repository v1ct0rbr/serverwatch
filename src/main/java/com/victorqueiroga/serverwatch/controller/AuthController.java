package com.victorqueiroga.serverwatch.controller;

import com.victorqueiroga.serverwatch.security.KeycloakUser;
import com.victorqueiroga.serverwatch.security.KeycloakUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    
    private final KeycloakUserService userService;

    /**
     * Página de login
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        
        // Se o usuário já está autenticado, redireciona para o dashboard
        if (userService.isUserAuthenticated()) {
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
    public String homePage() {
        if (userService.isUserAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    /**
     * Dashboard principal
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        KeycloakUser user = userService.getCurrentUser();
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("title", "Dashboard");
        model.addAttribute("user", user);
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userEmail", user.getEmail());
        
        // Adicionar estatísticas fictícias (substituir por dados reais)
        model.addAttribute("totalServers", 0);
        model.addAttribute("onlineServers", 0);
        model.addAttribute("offlineServers", 0);
        model.addAttribute("pendingAlerts", 0);
        
        return "pages/dashboard";
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
        KeycloakUser user = userService.getCurrentUser();
        if (user != null) {
            model.addAttribute("user", user);
        }
        return "error/access-denied";
    }

    /**
     * Informações do usuário atual
     */
    @GetMapping("/profile")
    public String userProfile(Model model) {
        KeycloakUser user = userService.getCurrentUser();
        
        if (user == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("title", "Meu Perfil");
        model.addAttribute("user", user);
        
        return "pages/profile";
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