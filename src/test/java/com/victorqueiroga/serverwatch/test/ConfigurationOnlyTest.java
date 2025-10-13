package com.victorqueiroga.serverwatch.test;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import com.victorqueiroga.serverwatch.config.KeycloakProperties;
import com.victorqueiroga.serverwatch.config.ServerWatchProperties;

/**
 * Teste unitário para validar apenas as configurações customizadas
 */
@SpringBootTest(classes = {ConfigurationOnlyTest.TestConfig.class})
@ActiveProfiles("test")
public class ConfigurationOnlyTest {

    @Autowired(required = false)
    private KeycloakProperties keycloakProperties;

    @Autowired(required = false) 
    private ServerWatchProperties serverWatchProperties;

    @Configuration
    @EnableConfigurationProperties({KeycloakProperties.class, ServerWatchProperties.class})
    static class TestConfig {
        // Configuration class apenas para habilitar as propriedades
    }

    @Test
    public void testKeycloakPropertiesLoaded() {
        assertThat(keycloakProperties).isNotNull();
        assertThat(keycloakProperties.getRealm()).isEqualTo("test-realm");
        assertThat(keycloakProperties.getAuthServerUrl()).isEqualTo("https://keycloak.test.com");
        assertThat(keycloakProperties.getResource()).isEqualTo("test-client");
        
        System.out.println("✅ Keycloak Properties carregadas:");
        System.out.println("   - Realm: " + keycloakProperties.getRealm());
        System.out.println("   - Auth Server URL: " + keycloakProperties.getAuthServerUrl());
        System.out.println("   - Resource: " + keycloakProperties.getResource());
    }

    @Test
    public void testServerWatchPropertiesLoaded() {
        assertThat(serverWatchProperties).isNotNull();
        assertThat(serverWatchProperties.getApplication().getName()).isEqualTo("ServerWatch Test");
        assertThat(serverWatchProperties.getApplication().getVersion()).isEqualTo("1.0.0-TEST");
        assertThat(serverWatchProperties.getApplication().isDevelopmentMode()).isTrue();
        
        System.out.println("✅ ServerWatch Properties carregadas:");
        System.out.println("   - Nome: " + serverWatchProperties.getApplication().getName());
        System.out.println("   - Versão: " + serverWatchProperties.getApplication().getVersion());
        System.out.println("   - Desenvolvimento: " + serverWatchProperties.getApplication().isDevelopmentMode());
        System.out.println("   - Tema UI: " + serverWatchProperties.getUi().getTheme());
        System.out.println("   - Itens por Página: " + serverWatchProperties.getUi().getItemsPerPage());
    }

    @Test
    public void testKeycloakUrlsCalculated() {
        assertThat(keycloakProperties).isNotNull();
        
        // Chamar postConstruct manualmente para calcular URLs
        keycloakProperties.postConstruct();
        
        assertThat(keycloakProperties.getUrls().getIssuer()).isNotNull();
        assertThat(keycloakProperties.getUrls().getJwks()).isNotNull();
        
        System.out.println("✅ URLs do Keycloak calculadas:");
        System.out.println("   - Issuer: " + keycloakProperties.getUrls().getIssuer());
        System.out.println("   - JWKS: " + keycloakProperties.getUrls().getJwks());
        System.out.println("   - Auth: " + keycloakProperties.getUrls().getAuth());
        System.out.println("   - Token: " + keycloakProperties.getUrls().getToken());
    }
}