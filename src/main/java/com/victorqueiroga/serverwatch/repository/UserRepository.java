package com.victorqueiroga.serverwatch.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.victorqueiroga.serverwatch.model.User;

/**
 * Repositório para gerenciar usuários locais da aplicação
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuário pelo ID do Keycloak
     */
    Optional<User> findByKeycloakId(String keycloakId);

    /**
     * Busca usuário pelo username
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca usuário pelo email
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se existe usuário com o Keycloak ID
     */
    boolean existsByKeycloakId(String keycloakId);

    /**
     * Verifica se existe usuário com o username
     */
    boolean existsByUsername(String username);

    /**
     * Verifica se existe usuário com o email
     */
    boolean existsByEmail(String email);

    /**
     * Busca todos os usuários ativos
     */
    List<User> findByActiveTrue();

    /**
     * Busca usuários por role da aplicação
     */
    @Query("SELECT u FROM User u JOIN u.applicationRoles r WHERE r = :role AND u.active = true")
    List<User> findByApplicationRole(@Param("role") User.ApplicationRole role);

    /**
     * Busca usuários que fizeram login em um período específico
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :startDate AND u.lastLogin <= :endDate")
    List<User> findByLastLoginBetween(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Conta usuários ativos
     */
    long countByActiveTrue();

    /**
     * Conta usuários que fizeram primeiro login hoje
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.firstLogin >= :startOfDay AND u.firstLogin < :endOfDay")
    long countNewUsersToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * Conta usuários que fizeram login nas últimas 24 horas
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :since")
    long countActiveUsersSince(@Param("since") LocalDateTime since);

    /**
     * Busca usuários por idioma
     */
    List<User> findByLanguage(String language);

    /**
     * Busca usuários por tema
     */
    List<User> findByTheme(User.Theme theme);
}