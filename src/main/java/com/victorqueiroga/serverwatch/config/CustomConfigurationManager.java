package com.victorqueiroga.serverwatch.config;

import java.util.Properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.victorqueiroga.serverwatch.mail.EmailConfig;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuração principal das propriedades customizadas da aplicação
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({KeycloakProperties.class, ServerWatchProperties.class})
public class CustomConfigurationManager {

    private final KeycloakProperties keycloakProperties;
    private final ServerWatchProperties serverWatchProperties;
    private final Environment environment;
    private final EmailConfig emailConfig;

    /**
     * Inicialização pós-construção para calcular URLs e configurações dinâmicas
     */
    @PostConstruct
    public void init() {
        // Calcula as URLs do Keycloak automaticamente
        keycloakProperties.postConstruct();

        // Log das configurações carregadas
        log.info("=== Configurações da Aplicação ===");
        log.info("Aplicação: {}", serverWatchProperties.getApplication().getName());
        log.info("Versão: {}", serverWatchProperties.getApplication().getVersion());
        log.info("Organização: {}", serverWatchProperties.getApplication().getOrganization());
        log.info("Modo Desenvolvimento: {}", serverWatchProperties.getApplication().isDevelopmentMode());

        log.info("=== Configurações do Keycloak ===");
        log.info("Auth Server URL: {}", keycloakProperties.getAuthServerUrl());
        log.info("Realm: {}", keycloakProperties.getRealm());
        log.info("Resource: {}", keycloakProperties.getResource());
        log.info("Issuer URL: {}", keycloakProperties.getUrls().getIssuer());
        log.info("JWKS URL: {}", keycloakProperties.getUrls().getJwks());

        // Log das configurações de monitoramento
        log.info("=== Configurações de Monitoramento ===");
        log.info("Intervalo de Verificação: {}s", serverWatchProperties.getMonitoring().getServerCheckIntervalSeconds());
        log.info("Timeout de Ping: {}s", serverWatchProperties.getMonitoring().getServerPingTimeoutSeconds());
        log.info("Max Tentativas: {}", serverWatchProperties.getMonitoring().getMaxRetryAttempts());

        // Validações básicas
        validateConfiguration();
    }

    /**
     * Valida as configurações essenciais
     */
    private void validateConfiguration() {
        // Validar Keycloak
        if (keycloakProperties.getAuthServerUrl() == null || keycloakProperties.getAuthServerUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("keycloak.auth-server-url é obrigatório");
        }

        if (keycloakProperties.getRealm() == null || keycloakProperties.getRealm().trim().isEmpty()) {
            throw new IllegalArgumentException("keycloak.realm é obrigatório");
        }

        if (keycloakProperties.getResource() == null || keycloakProperties.getResource().trim().isEmpty()) {
            throw new IllegalArgumentException("keycloak.resource é obrigatório");
        }

        // Validar ServerWatch
        if (serverWatchProperties.getApplication().getName() == null || serverWatchProperties.getApplication().getName().trim().isEmpty()) {
            throw new IllegalArgumentException("serverwatch.application.name é obrigatório");
        }

        // Validar intervalos de monitoramento
        if (serverWatchProperties.getMonitoring().getServerCheckIntervalSeconds() <= 0) {
            throw new IllegalArgumentException("serverwatch.monitoring.server-check-interval-seconds deve ser maior que 0");
        }

        if (serverWatchProperties.getMonitoring().getServerPingTimeoutSeconds() <= 0) {
            throw new IllegalArgumentException("serverwatch.monitoring.server-ping-timeout-seconds deve ser maior que 0");
        }

        log.info("✅ Todas as configurações foram validadas com sucesso!");
    }

    /**
     * Bean para acessar as propriedades do Keycloak em outros componentes
     */
    @Bean
    public KeycloakProperties keycloakProperties() {
        return keycloakProperties;
    }

    /**
     * Bean para acessar as propriedades do ServerWatch em outros componentes
     */
    @Bean
    public ServerWatchProperties serverWatchProperties() {
        return serverWatchProperties;
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getHost());
        mailSender.setPort(emailConfig.getPort());

        mailSender.setUsername(emailConfig.getUsername());
        mailSender.setPassword(emailConfig.getPassword());
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
        return mailSender;
    }

    /**
     * Helper method para verificar se está em modo desenvolvimento
     */
    public boolean isDevelopmentMode() {
        return serverWatchProperties.getApplication().isDevelopmentMode()
                || environment.getActiveProfiles().length > 0
                && (java.util.Arrays.asList(environment.getActiveProfiles()).contains("dev")
                || java.util.Arrays.asList(environment.getActiveProfiles()).contains("development")
                || java.util.Arrays.asList(environment.getActiveProfiles()).contains("local"));
    }

    /**
     * Helper method para obter a URL completa de um endpoint do Keycloak
     */
    public String getKeycloakUrl(String endpoint) {
        return keycloakProperties.getAuthServerUrl() + "/realms/" + keycloakProperties.getRealm() + endpoint;
    }
}
