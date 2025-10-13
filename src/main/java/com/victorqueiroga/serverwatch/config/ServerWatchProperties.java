package com.victorqueiroga.serverwatch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configurações customizadas da aplicação ServerWatch
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "serverwatch")
public class ServerWatchProperties {

    /**
     * Configurações da aplicação
     */
    private Application application = new Application();

    /**
     * Configurações de interface
     */
    private Interface ui = new Interface();

    /**
     * Configurações de monitoramento
     */
    private Monitoring monitoring = new Monitoring();

    /**
     * Configurações de notificações
     */
    private Notifications notifications = new Notifications();

    @Data
    public static class Application {
        /**
         * Nome da aplicação
         */
        private String name = "ServerWatch";

        /**
         * Versão da aplicação
         */
        private String version = "1.0.0";

        /**
         * Descrição da aplicação
         */
        private String description = "Sistema de Monitoramento de Servidores";

        /**
         * Autor da aplicação
         */
        private String author = "Victor Queiroga";

        /**
         * Organização
         */
        private String organization = "DERPB";

        /**
         * Timezone padrão
         */
        private String defaultTimezone = "America/Fortaleza";

        /**
         * Modo de desenvolvimento
         */
        private boolean developmentMode = true;

        /**
         * Configurações de debug
         */
        private Debug debug = new Debug();

        @Data
        public static class Debug {
            private boolean enabled = false;
            private boolean logSqlQueries = false;
            private boolean logKeycloakEvents = false;
        }
    }

    @Data
    public static class Interface {
        /**
         * Tema da interface
         */
        private String theme = "bootstrap";

        /**
         * Idioma padrão
         */
        private String defaultLanguage = "pt-BR";

        /**
         * Número de itens por página
         */
        private int itemsPerPage = 25;

        /**
         * Timeout de sessão em minutos
         */
        private int sessionTimeoutMinutes = 30;

        /**
         * Auto-refresh das páginas em segundos
         */
        private int autoRefreshSeconds = 30;

        /**
         * Configurações do dashboard
         */
        private Dashboard dashboard = new Dashboard();

        @Data
        public static class Dashboard {
            private boolean showServerStatus = true;
            private boolean showRecentAlerts = true;
            private boolean showUserActivity = true;
            private boolean showSystemMetrics = true;
            private int maxRecentItems = 10;
        }
    }

    @Data
    public static class Monitoring {
        /**
         * Intervalo de verificação de servidores em segundos
         */
        private int serverCheckIntervalSeconds = 60;

        /**
         * Timeout para ping de servidores em segundos
         */
        private int serverPingTimeoutSeconds = 5;

        /**
         * Número de tentativas antes de considerar servidor offline
         */
        private int maxRetryAttempts = 3;

        /**
         * Intervalo entre tentativas em segundos
         */
        private int retryIntervalSeconds = 10;

        /**
         * Configurações de alertas
         */
        private Alerts alerts = new Alerts();

        @Data
        public static class Alerts {
            private boolean enableEmailAlerts = true;
            private boolean enableSmsAlerts = false;
            private boolean enableWebhookAlerts = false;
            private int cooldownMinutes = 15;
        }
    }

    @Data
    public static class Notifications {
        /**
         * Configurações de e-mail
         */
        private Email email = new Email();

        /**
         * Configurações de webhook
         */
        private Webhook webhook = new Webhook();

        @Data
        public static class Email {
            private boolean enabled = false;
            private String smtpHost = "";
            private int smtpPort = 587;
            private String username = "";
            private String password = "";
            private boolean useSSL = true;
            private String fromAddress = "noreply@serverwatch.com";
            private String fromName = "ServerWatch";
        }

        @Data
        public static class Webhook {
            private boolean enabled = false;
            private String url = "";
            private String secret = "";
            private int timeoutSeconds = 10;
        }
    }
}