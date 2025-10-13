package com.victorqueiroga.serverwatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para operações de criação e edição de servidores
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerDTO {

    private Long id;

    @NotBlank(message = "Nome do servidor é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String name;

    @NotBlank(message = "Endereço IP é obrigatório")
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", 
             message = "Endereço IP deve estar em formato válido (ex: 192.168.1.100)")
    private String ipAddress;

    @NotNull(message = "Sistema operacional é obrigatório")
    private Long operationSystemId;

    private String operationSystemName;

    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    private String description;

    @Builder.Default
    private Integer port = 80;

    @Builder.Default
    private Boolean active = true;

    private String status;

    /**
     * DTO simplificado para listagens
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String name;
        private String ipAddress;
        private String operationSystemName;
        private String status;
        private Boolean active;
        private Long lastResponseTime;
    }

    /**
     * DTO para filtros de busca
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Filter {
        private String name;
        private String ipAddress;
        private Long operationSystemId;
        private String status;
        private Boolean active;
    }
}