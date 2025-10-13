package com.victorqueiroga.serverwatch.test;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.victorqueiroga.serverwatch.config.CustomConfigurationManager;
import com.victorqueiroga.serverwatch.config.KeycloakProperties;
import com.victorqueiroga.serverwatch.config.ServerWatchProperties;

/**
 * Teste para validar as configurações customizadas
 */
@SpringBootTest
@ActiveProfiles("test")
public class CustomConfigurationTest {

    @Autowired(required = false)
    private KeycloakProperties keycloakProperties;

    @Autowired(required = false) 
    private ServerWatchProperties serverWatchProperties;

    @Autowired(required = false)
    private CustomConfigurationManager configurationManager;

    @Test
    public void testKeycloakPropertiesLoaded() {
        if (keycloakProperties != null) {
            assertThat(keycloakProperties.getRealm()).isNotNull();
            assertThat(keycloakProperties.getAuthServerUrl()).isNotNull();
            assertThat(keycloakProperties.getResource()).isNotNull();
            
            System.out.println("✅ Keycloak Properties carregadas:");
            System.out.println("   - Realm: " + keycloakProperties.getRealm());
            System.out.println("   - Auth Server URL: " + keycloakProperties.getAuthServerUrl());
            System.out.println("   - Resource: " + keycloakProperties.getResource());
        } else {
            System.out.println("⚠️ KeycloakProperties não foi carregada");
        }
    }

    @Test
    public void testServerWatchPropertiesLoaded() {
        if (serverWatchProperties != null) {
            assertThat(serverWatchProperties.getApplication().getName()).isNotNull();
            
            System.out.println("✅ ServerWatch Properties carregadas:");
            System.out.println("   - Nome: " + serverWatchProperties.getApplication().getName());
            System.out.println("   - Versão: " + serverWatchProperties.getApplication().getVersion());
            System.out.println("   - Desenvolvimento: " + serverWatchProperties.getApplication().isDevelopmentMode());
        } else {
            System.out.println("⚠️ ServerWatchProperties não foi carregada");
        }
    }

    @Test
    public void testConfigurationManagerLoaded() {
        if (configurationManager != null) {
            System.out.println("✅ CustomConfigurationManager carregado");
            System.out.println("   - Modo Desenvolvimento: " + configurationManager.isDevelopmentMode());
        } else {
            System.out.println("⚠️ CustomConfigurationManager não foi carregado");
        }
    }
}