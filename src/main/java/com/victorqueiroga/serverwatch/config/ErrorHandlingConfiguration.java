package com.victorqueiroga.serverwatch.config;

import java.util.Map;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

/**
 * Configuração centralizada para tratamento de erros
 * Garante que TODOS os erros sejam processados pelo CustomErrorController
 */
@Configuration
public class ErrorHandlingConfiguration {

    /**
     * Bean customizado para atributos de erro
     * Fornece informações completas sobre o erro
     */
    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest,
                    org.springframework.boot.web.error.ErrorAttributeOptions options) {
                Map<String, Object> attributes = super.getErrorAttributes(webRequest, options);

                // Adicionar informações customizadas se necessário
                // attributes.put("customField", "value");

                return attributes;
            }
        };
    }
}
