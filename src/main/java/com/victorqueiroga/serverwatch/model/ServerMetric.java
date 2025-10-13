package com.victorqueiroga.serverwatch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que armazena métricas coletadas via SNMP dos servidores
 */
@Entity
@Table(name = "server_metrics", 
       indexes = {
           @Index(name = "idx_metric_server", columnList = "server_id"),
           @Index(name = "idx_metric_name", columnList = "metric_name"),
           @Index(name = "idx_metric_timestamp", columnList = "timestamp"),
           @Index(name = "idx_metric_server_name", columnList = "server_id,metric_name")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Servidor relacionado à métrica
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    /**
     * Nome da métrica (CPU_USAGE, MEMORY_USAGE, DISK_USAGE, etc.)
     */
    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    /**
     * Valor da métrica
     */
    @Column(name = "metric_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal value;

    /**
     * Unidade da métrica (%, GB, MB/s, etc.)
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * Valor como string para dados textuais
     */
    @Column(name = "string_value", length = 500)
    private String stringValue;

    /**
     * Categoria da métrica
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private MetricCategory category;

    /**
     * Subcategoria ou descrição adicional
     */
    @Column(name = "subcategory", length = 100)
    private String subcategory;

    /**
     * OID SNMP usado para coletar a métrica
     */
    @Column(name = "snmp_oid", length = 200)
    private String snmpOid;

    /**
     * Timestamp da coleta
     */
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Se a métrica indica um estado crítico
     */
    @Column(name = "is_critical")
    @Builder.Default
    private Boolean critical = false;

    /**
     * Threshold de warning para a métrica
     */
    @Column(name = "warning_threshold", precision = 15, scale = 4)
    private BigDecimal warningThreshold;

    /**
     * Threshold crítico para a métrica
     */
    @Column(name = "critical_threshold", precision = 15, scale = 4)
    private BigDecimal criticalThreshold;

    /**
     * Enum para categorias de métricas
     */
    public enum MetricCategory {
        SYSTEM("Sistema"),
        CPU("CPU"),
        MEMORY("Memória"),
        DISK("Disco"),
        NETWORK("Rede"),
        PROCESS("Processo"),
        SERVICE("Serviço"),
        CUSTOM("Personalizado");

        private final String displayName;

        MetricCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Verifica se a métrica está em estado de warning
     */
    public boolean isWarning() {
        return warningThreshold != null && value != null && 
               value.compareTo(warningThreshold) >= 0 &&
               (criticalThreshold == null || value.compareTo(criticalThreshold) < 0);
    }

    /**
     * Verifica se a métrica está em estado crítico
     */
    public boolean isCritical() {
        return Boolean.TRUE.equals(critical) || 
               (criticalThreshold != null && value != null && 
                value.compareTo(criticalThreshold) >= 0);
    }

    /**
     * Obtém o valor formatado com unidade
     */
    public String getFormattedValue() {
        if (stringValue != null && !stringValue.trim().isEmpty()) {
            return stringValue;
        }
        if (value != null) {
            return String.format("%.2f%s", value, unit != null ? " " + unit : "");
        }
        return "N/A";
    }

    /**
     * Obtém o status da métrica baseado nos thresholds
     */
    public String getStatus() {
        if (isCritical()) {
            return "CRITICAL";
        } else if (isWarning()) {
            return "WARNING";
        }
        return "OK";
    }
}