package com.victorqueiroga.serverwatch.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.victorqueiroga.serverwatch.model.User;
import com.victorqueiroga.serverwatch.security.KeycloakUser;
import com.victorqueiroga.serverwatch.security.KeycloakUserService;
import com.victorqueiroga.serverwatch.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;

/**
 * Controller responsável por gerenciar autenticação e autorização
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@Profile("!dev") // Exclui do profile dev
public class AuthController extends AbstractController {

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

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

        // IMPORTANTE: Não redirecionar se há erro ou logout, mesmo que esteja
        // autenticado
        if (error == null && logout == null &&
                authentication != null && authentication.isAuthenticated() &&
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
     * Página inicial - redireciona para dashboard se autenticado ou login se não
     * autenticado
     */
    @GetMapping("/")
    public String homePage(HttpServletRequest request, Authentication authentication) {
        log.info("Acesso à página inicial - Authentication: {}, Principal: {}",
                authentication != null ? authentication.getClass().getSimpleName() : "null",
                authentication != null ? authentication.getName() : "null");

        // Verificar se está autenticado com OAuth2 - versão mais segura
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {

            try {
                // Verificar se é OAuth2AuthenticationToken (login via Keycloak)
                if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    log.info("Usuário OAuth2 autenticado: {} - redirecionando para dashboard",
                            authentication.getName());
                    return "redirect:/dashboard";
                }

                log.info("Usuário autenticado (não OAuth2): {} - redirecionando para dashboard",
                        authentication.getName());
                return "redirect:/dashboard";
            } catch (Exception e) {
                log.error("Erro ao processar autenticação na página inicial: {}", e.getMessage(), e);
                // Em caso de erro, redirecionar para login sem erro para evitar loop
            }
        }

        log.info("Usuário não autenticado ou erro - redirecionando para login");
        return "redirect:/login";
    }

    /**
     * Dashboard principal
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model, HttpServletRequest request) {
        try {
            // Adicionar URI atual ao modelo para uso nos templates
            model.addAttribute("currentURI", request.getRequestURI());

            log.info("Tentativa de acesso ao dashboard - Authentication: {}, Principal: {}",
                    authentication != null ? authentication.getClass().getSimpleName() : "null",
                    authentication != null ? authentication.getName() : "null");

            // Verificar autenticação primeiro
            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication.getName().equals("anonymousUser")) {
                log.warn("Usuário não autenticado tentando acessar dashboard - redirecionando para login");
                return "redirect:/login?error=authentication_required";
            }

            // Log das authorities para debug
            log.info("Authorities do usuário {}: {}",
                    authentication.getName(),
                    authentication.getAuthorities().stream()
                            .map(auth -> auth.getAuthority())
                            .collect(Collectors.toList()));

            // Verificar se é OAuth2AuthenticationToken
            if (!(authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken)) {
                log.warn("Authentication não é OAuth2AuthenticationToken: {} - redirecionando para login",
                        authentication.getClass().getSimpleName());
                return "redirect:/login?error=invalid_authentication";
            }

            log.info("Usuário OAuth2 autenticado acessando dashboard: {}", authentication.getName());

            // Obter ou criar usuário local integrado com Keycloak
            User localUser;
            KeycloakUser keycloakUser;

            try {
                localUser = userService.getOrCreateUser();
                keycloakUser = keycloakUserService.getCurrentUser();

                if (keycloakUser == null) {
                    log.warn("KeycloakUser é null para usuário autenticado: {}", authentication.getName());
                    // Em vez de redirecionar, criar um keycloakUser básico ou mostrar erro
                    model.addAttribute("error", "Não foi possível carregar informações do usuário do Keycloak");
                    model.addAttribute("userName", authentication.getName());
                    return "error/500";
                }
            } catch (Exception ex) {
                log.error("Erro ao obter dados do usuário: {}", ex.getMessage(), ex);
                model.addAttribute("error", "Erro ao carregar dados do usuário: " + ex.getMessage());
                model.addAttribute("userName", authentication.getName());
                return "error/500";
            }

            model.addAttribute("title", "Dashboard");
            model.addAttribute("user", keycloakUser); // Para compatibilidade com templates
            model.addAttribute("localUser", localUser); // Usuário local com dados específicos
            model.addAttribute("userName",
                    localUser.getFullName() != null ? localUser.getFullName() : keycloakUser.getFullName());
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
            log.error("Erro ao carregar dashboard para usuário: {} - {}",
                    authentication != null ? authentication.getName() : "unknown", e.getMessage(), e);

            // Em caso de erro, mostrar página de erro em vez de redirecionar
            model.addAttribute("error", "Erro interno ao carregar dashboard: " + e.getMessage());
            model.addAttribute("userName", authentication != null ? authentication.getName() : "Usuário");
            return "error/500";
        }
    }

    /**
     * Endpoint de debug para verificar informações do usuário
     */
    @GetMapping("/debug/user")
    public String debugUser(Authentication authentication, Model model) {
        if (authentication == null) {
            model.addAttribute("error", "Usuário não autenticado");
            return "debug/user-info";
        }

        model.addAttribute("authentication", authentication);
        model.addAttribute("principal", authentication.getPrincipal());
        model.addAttribute("authorities", authentication.getAuthorities());
        model.addAttribute("name", authentication.getName());
        model.addAttribute("authenticated", authentication.isAuthenticated());

        // Se for OAuth2AuthenticationToken, extrair mais informações
        if (authentication instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            var oauth2Token = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) authentication;
            model.addAttribute("oauth2Principal", oauth2Token.getPrincipal());

            if (oauth2Token.getPrincipal() instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser) {
                var oidcUser = (org.springframework.security.oauth2.core.oidc.user.OidcUser) oauth2Token.getPrincipal();
                model.addAttribute("idToken", oidcUser.getIdToken());
                model.addAttribute("claims", oidcUser.getClaims());
                model.addAttribute("userInfo", oidcUser.getUserInfo());
            }
        }

        return "debug/user-info";
    }

    /**
     * Endpoint de logout que redireciona para o Keycloak
     * O Spring Security já processará o logout local via CustomLogoutHandler
     */
    @GetMapping("/keycloak-logout")
    public String keycloakLogout(HttpServletRequest request, Authentication authentication) {

        log.info("Iniciando logout do Keycloak para usuário: {}",
                authentication != null ? authentication.getName() : "Anônimo");

        // URL de logout do Keycloak com parâmetros para finalizar sessão SSO
        String baseUrl = getBaseUrl(request);
        String keycloakLogoutUrl = keycloakServerUrl + "/realms/" + realm +
                "/protocol/openid-connect/logout" +
                "?post_logout_redirect_uri=" + baseUrl + "/login?logout=true" +
                "&client_id=" + clientId;

        log.info("Redirecionando para logout do Keycloak: {}", keycloakLogoutUrl);
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
                (request.getServerPort() != 80 && request.getServerPort() != 443 ? ":" + request.getServerPort() : "")
                + request.getContextPath();
    }
}