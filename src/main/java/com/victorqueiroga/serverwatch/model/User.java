package com.victorqueiroga.serverwatch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entidade User que armazena informações complementares aos dados do Keycloak
 * Esta entidade NÃO substitui o Keycloak, mas complementa com dados específicos da aplicação
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_keycloak_id", columnList = "keycloak_id"),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_username", columnList = "username")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID do usuário no Keycloak (Subject do JWT)
     * Este é o campo que faz a ligação com o Keycloak
     */
    @Column(name = "keycloak_id", nullable = false, unique = true, length = 100)
    private String keycloakId;

    /**
     * Nome de usuário (sincronizado com Keycloak)
     */
    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    /**
     * Email do usuário (sincronizado com Keycloak)
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Nome completo (sincronizado com Keycloak)
     */
    @Column(name = "full_name", length = 255)
    private String fullName;

    /**
     * Primeira vez que o usuário acessou a aplicação
     */
    @Column(name = "first_login")
    private LocalDateTime firstLogin;

    /**
     * Último acesso à aplicação
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    /**
     * Se o usuário está ativo na aplicação (independente do Keycloak)
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Preferências do usuário (JSON)
     */
    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences;

    /**
     * Tema preferido da interface
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "theme")
    @Builder.Default
    private Theme theme = Theme.AUTO;

    /**
     * Idioma preferido
     */
    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "pt-BR";

    /**
     * Timezone do usuário
     */
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "America/Sao_Paulo";

    /**
     * Roles locais específicas da aplicação (complementam as do Keycloak)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<ApplicationRole> applicationRoles;

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
     * Enum para temas da interface
     */
    public enum Theme {
        LIGHT, DARK, AUTO
    }

    /**
     * Enum para roles específicas da aplicação
     */
    public enum ApplicationRole {
        // Roles específicas do ServerWatch
        SERVER_MANAGER,     // Pode gerenciar servidores
        ALERT_MANAGER,      // Pode gerenciar alertas
        REPORT_VIEWER,      // Pode visualizar relatórios
        SYSTEM_ADMIN,       // Administração completa do sistema
        MONITORING_VIEWER   // Apenas visualização de monitoramento
    }

    /**
     * Verifica se o usuário tem uma role específica da aplicação
     */
    public boolean hasApplicationRole(ApplicationRole role) {
        return applicationRoles != null && applicationRoles.contains(role);
    }

    /**
     * Registra um novo login
     */
    public void registerLogin() {
        if (this.firstLogin == null) {
            this.firstLogin = LocalDateTime.now();
        }
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * Sincroniza dados básicos com o Keycloak
     */
    public void syncWithKeycloak(String username, String email, String fullName) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
    }
}