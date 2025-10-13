package com.victorqueiroga.serverwatch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configurações customizadas do Keycloak
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    /**
     * URL base do servidor Keycloak
     */
    private String authServerUrl = "https://keycloak.derpb.com.br";

    /**
     * Nome do realm
     */
    private String realm = "DERPB";

    /**
     * Client ID da aplicação (resource)
     */
    private String resource = "serverwatch-client";

    /**
     * Configurações administrativas
     */
    private Admin admin = new Admin();

    /**
     * Configurações avançadas
     */
    private Configuration configuration = new Configuration();

    /**
     * URLs calculadas
     */
    private Urls urls = new Urls();

    @Data
    public static class Admin {
        /**
         * Client ID para operações administrativas
         */
        private String clientId = "serverwatch-admin";

        /**
         * Client Secret para operações administrativas
         */
        private String clientSecret = "change-me-in-production";
    }

    @Data
    public static class Configuration {
        /**
         * Usa resource roles ao invés de realm roles
         */
        private boolean useResourceRoleMappings = false;

        /**
         * Modo bearer-only para APIs
         */
        private boolean bearerOnly = false;

        /**
         * SSL obrigatório
         */
        private String sslRequired = "external";

        /**
         * Verificar audience do token
         */
        private boolean verifyTokenAudience = true;

        /**
         * Timeout de conexão em milissegundos
         */
        private int connectionTimeoutMillis = 5000;

        /**
         * Timeout de socket em milissegundos
         */
        private int socketTimeoutMillis = 5000;

        /**
         * Tamanho do pool de conexões
         */
        private int connectionPoolSize = 10;

        /**
         * Configurações CORS específicas do Keycloak
         */
        private Cors cors = new Cors();

        @Data
        public static class Cors {
            private String allowedOrigins = "*";
            private String allowedHeaders = "*";
            private String allowedMethods = "GET,POST,PUT,DELETE,OPTIONS";
        }
    }

    @Data
    public static class Urls {
        private String auth;
        private String token;
        private String userinfo;
        private String jwks;
        private String logout;
        private String issuer;
        private String admin;

        /**
         * Calcula as URLs baseadas nas configurações
         */
        public void calculateUrls(String authServerUrl, String realm) {
            String baseUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect";
            this.auth = baseUrl + "/auth";
            this.token = baseUrl + "/token";
            this.userinfo = baseUrl + "/userinfo";
            this.jwks = baseUrl + "/certs";
            this.logout = baseUrl + "/logout";
            this.issuer = authServerUrl + "/realms/" + realm;
            this.admin = authServerUrl + "/admin/realms/" + realm;
        }
    }

    /**
     * Método para calcular URLs automaticamente após a inicialização
     */
    public void postConstruct() {
        urls.calculateUrls(authServerUrl, realm);
    }
}