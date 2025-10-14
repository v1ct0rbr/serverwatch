package com.victorqueiroga.serverwatch.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um servidor monitorado pelo sistema
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "servers", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "name"),
           @UniqueConstraint(columnNames = "ip_address")
       },
       indexes = {
           @Index(name = "idx_server_name", columnList = "name"),
           @Index(name = "idx_server_ip", columnList = "ip_address"),
           @Index(name = "idx_server_os", columnList = "operation_system_id")
       })
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome único do servidor
     */
    @NotBlank(message = "Nome do servidor é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Endereço IP do servidor
     */
    @NotBlank(message = "Endereço IP é obrigatório")
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", 
             message = "Endereço IP deve estar em formato válido (ex: 192.168.1.100)")
    @Column(name = "ip_address", nullable = false, unique = true, length = 15)
    private String ipAddress;

    /**
     * Sistema operacional do servidor
     */
    @NotNull(message = "Sistema operacional é obrigatório")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operation_system_id", nullable = false)
    private OperationSystem operationSystem;

    /**
     * Status atual do servidor
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServerStatus status = ServerStatus.UNKNOWN;

    /**
     * Porta principal de monitoramento
     */
    @Column(name = "port")
    private Integer port = 80;

    /**
     * Descrição adicional do servidor
     */
    @Size(max = 500, message = "Descrição não pode exceder 500 caracteres")
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Se o servidor está ativo para monitoramento
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Última vez que o servidor foi verificado
     */
    @Column(name = "last_check")
    private LocalDateTime lastCheck;

    /**
     * Tempo de resposta da última verificação (em ms)
     */
    @Column(name = "last_response_time")
    private Long lastResponseTime;

    /**
     * Data de criação do registro
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data da última atualização
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum para status do servidor
     */
    public enum ServerStatus {
        ONLINE("Online", "success"),
        OFFLINE("Offline", "danger"),
        WARNING("Com Alertas", "warning"),
        MAINTENANCE("Manutenção", "info"),
        UNKNOWN("Desconhecido", "secondary");

        private final String displayName;
        private final String bootstrapClass;

        ServerStatus(String displayName, String bootstrapClass) {
            this.displayName = displayName;
            this.bootstrapClass = bootstrapClass;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getBootstrapClass() {
            return bootstrapClass;
        }
    }

    /**
     * Verifica se o servidor está online
     */
    public boolean isOnline() {
        return ServerStatus.ONLINE.equals(this.status);
    }

    /**
     * Verifica se o servidor está ativo para monitoramento
     */
    public boolean isActiveForMonitoring() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Atualiza o status da última verificação
     */
    public void updateLastCheck(ServerStatus status, Long responseTime) {
        this.status = status;
        this.lastCheck = LocalDateTime.now();
        this.lastResponseTime = responseTime;
    }

    /**
     * Obtém uma descrição resumida do servidor
     */
    public String getSummary() {
        return String.format("%s (%s) - %s", 
                           this.name, 
                           this.ipAddress, 
                           this.status != null ? this.status.getDisplayName() : "Status desconhecido");
    }
}
