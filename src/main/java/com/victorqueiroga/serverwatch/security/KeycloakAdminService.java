package com.victorqueiroga.serverwatch.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para interação com o Keycloak Admin API
 */
@Slf4j
@Service
public class KeycloakAdminService {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String adminClientId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String adminClientSecret;

    private final WebClient webClient;

    public KeycloakAdminService() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }

    /**
     * Obtém um token de acesso admin do Keycloak
     */
    public String getAdminToken() {
        try {
            String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
            
            Map<String, Object> response = webClient.post()
                .uri(tokenUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials&client_id=" + adminClientId + 
                          "&client_secret=" + adminClientSecret)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            return (String) response.get("access_token");
        } catch (Exception e) {
            log.error("Erro ao obter token admin do Keycloak", e);
            throw new RuntimeException("Falha na autenticação admin com Keycloak", e);
        }
    }

    /**
     * Busca informações de um usuário pelo ID
     */
    public Map<String, Object> getUserById(String userId) {
        try {
            String adminToken = getAdminToken();
            String userUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;
            
            return webClient.get()
                .uri(userUrl)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        } catch (Exception e) {
            log.error("Erro ao buscar usuário no Keycloak: {}", userId, e);
            return null;
        }
    }

    /**
     * Busca usuário pelo email
     */
    public Map<String, Object> getUserByEmail(String email) {
        try {
            String adminToken = getAdminToken();
            String usersUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users?email=" + email;
            
            Map[] users = webClient.get()
                .uri(usersUrl)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(Map[].class)
                .block();

            return (users != null && users.length > 0) ? users[0] : null;
        } catch (Exception e) {
            log.error("Erro ao buscar usuário por email no Keycloak: {}", email, e);
            return null;
        }
    }

    /**
     * Valida se o token JWT é válido
     */
    public boolean validateToken(String token) {
        try {
            String introspectUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token/introspect";
            
            Map<String, Object> response = webClient.post()
                .uri(introspectUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Bearer " + getAdminToken())
                .bodyValue("token=" + token)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            return response != null && Boolean.TRUE.equals(response.get("active"));
        } catch (Exception e) {
            log.error("Erro ao validar token no Keycloak", e);
            return false;
        }
    }

    /**
     * Obtém as informações de configuração do realm
     */
    public Map<String, Object> getRealmInfo() {
        try {
            String realmUrl = keycloakServerUrl + "/realms/" + realm;
            
            return webClient.get()
                .uri(realmUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        } catch (Exception e) {
            log.error("Erro ao obter informações do realm", e);
            return null;
        }
    }
}