package com.victorqueiroga.serverwatch.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade que representa um alerta de monitoramento do sistema
 */
@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_server", columnList = "server_id"),
    @Index(name = "idx_alert_severity", columnList = "severity_id"),
    @Index(name = "idx_alert_resolved", columnList = "resolved"),
    @Index(name = "idx_alert_created", columnList = "created_at"),
    @Index(name = "idx_alert_type", columnList = "alert_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título do alerta
     */
    @NotBlank(message = "Título do alerta é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Descrição detalhada do alerta
     */
    @NotBlank(message = "Descrição do alerta é obrigatória")
    @Size(max = 2000, message = "Descrição deve ter no máximo 2000 caracteres")
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    /**
     * Servidor relacionado ao alerta
     */
    @NotNull(message = "Servidor é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    /**
     * Severidade do alerta
     */
    @NotNull(message = "Severidade é obrigatória")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "severity_id", nullable = false)
    private Severity severity;

    /**
     * Tipo do alerta
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    @Builder.Default
    private AlertType alertType = AlertType.MONITORING;

    /**
     * Status do alerta
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AlertStatus status = AlertStatus.OPEN;

    /**
     * Se o alerta foi resolvido
     */
    @Column(name = "resolved", nullable = false)
    @Builder.Default
    private Boolean resolved = false;

    /**
     * Valor atual que disparou o alerta
     */
    @Column(name = "current_value")
    private String currentValue;

    /**
     * Valor de threshold que foi ultrapassado
     */
    @Column(name = "threshold_value")
    private String thresholdValue;

    /**
     * Métrica relacionada ao alerta (CPU, Memory, Disk, etc.)
     */
    @Column(name = "metric_name", length = 100)
    private String metricName;

    /**
     * Dados adicionais em formato JSON
     */
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;

    /**
     * Usuário que resolveu o alerta
     */
    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    /**
     * Comentário de resolução
     */
    @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
    @Column(name = "resolution_comment", length = 1000)
    private String resolutionComment;

    /**
     * Data de criação do alerta
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Data de última atualização
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Data de resolução do alerta
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /**
     * Enum para tipos de alerta
     */
    public enum AlertType {
        MONITORING("Monitoramento", "primary"),
        PERFORMANCE("Performance", "warning"),
        SECURITY("Segurança", "danger"),
        SYSTEM("Sistema", "info"),
        NETWORK("Rede", "secondary"),
        CUSTOM("Personalizado", "dark");

        private final String displayName;
        private final String bootstrapClass;

        AlertType(String displayName, String bootstrapClass) {
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
     * Enum para status do alerta
     */
    public enum AlertStatus {
        OPEN("Aberto", "danger"),
        IN_PROGRESS("Em Progresso", "warning"),
        RESOLVED("Resolvido", "success"),
        CLOSED("Fechado", "secondary"),
        ACKNOWLEDGED("Reconhecido", "info");

        private final String displayName;
        private final String bootstrapClass;

        AlertStatus(String displayName, String bootstrapClass) {
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
     * Marca o alerta como resolvido
     */
    public void resolve(String resolvedBy, String comment) {
        this.resolved = true;
        this.status = AlertStatus.RESOLVED;
        this.resolvedBy = resolvedBy;
        this.resolutionComment = comment;
        this.resolvedAt = LocalDateTime.now();
    }

    /**
     * Reconhece o alerta
     */
    public void acknowledge() {
        this.status = AlertStatus.ACKNOWLEDGED;
    }

    /**
     * Verifica se o alerta está ativo (não resolvido)
     */
    public boolean isActive() {
        return !Boolean.TRUE.equals(this.resolved) && 
               !AlertStatus.RESOLVED.equals(this.status) && 
               !AlertStatus.CLOSED.equals(this.status);
    }

    /**
     * Obtém uma descrição resumida do alerta
     */
    public String getSummary() {
        return String.format("[%s] %s - %s (%s)", 
                           this.severity != null ? this.severity.getName() : "N/A",
                           this.title,
                           this.server != null ? this.server.getName() : "N/A",
                           this.status != null ? this.status.getDisplayName() : "N/A");
    }
}
