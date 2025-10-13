package com.victorqueiroga.serverwatch.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade que representa os níveis de severidade dos alertas
 */
@Entity
@Table(name = "severities", 
       uniqueConstraints = @UniqueConstraint(columnNames = "name"),
       indexes = @Index(name = "idx_severity_level", columnList = "severity_level"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Severity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nome da severidade
     */
    @NotBlank(message = "Nome da severidade é obrigatório")
    @Size(max = 50, message = "Nome deve ter no máximo 50 caracteres")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Descrição da severidade
     */
    @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
    @Column(name = "description", length = 200)
    private String description;

    /**
     * Nível numérico da severidade (1 = mais alta, 5 = mais baixa)
     */
    @Column(name = "severity_level", nullable = false)
    private Integer level;

    /**
     * Cor associada à severidade (para UI)
     */
    @Column(name = "color", length = 7)
    private String color;

    /**
     * Classe CSS Bootstrap para estilização
     */
    @Column(name = "bootstrap_class", length = 20)
    private String bootstrapClass;

    /**
     * Ícone associado à severidade
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * Se a severidade está ativa
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Data de criação
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Verifica se é uma severidade crítica
     */
    public boolean isCritical() {
        return this.level != null && this.level <= 2;
    }

    /**
     * Verifica se é uma severidade alta
     */
    public boolean isHigh() {
        return this.level != null && this.level == 3;
    }

    /**
     * Verifica se é uma severidade baixa
     */
    public boolean isLow() {
        return this.level != null && this.level >= 4;
    }

    /**
     * Obtém o nome de exibição com ícone
     */
    public String getDisplayNameWithIcon() {
        return (icon != null ? icon + " " : "") + name;
    }
}
