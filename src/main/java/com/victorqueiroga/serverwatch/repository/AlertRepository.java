package com.victorqueiroga.serverwatch.repository;

import com.victorqueiroga.serverwatch.model.Alert;
import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.model.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório para gerenciamento de alertas do sistema
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Busca alertas por servidor
     */
    Page<Alert> findByServer(Server server, Pageable pageable);

    /**
     * Busca alertas por severidade
     */
    Page<Alert> findBySeverity(Severity severity, Pageable pageable);

    /**
     * Busca alertas resolvidos
     */
    Page<Alert> findByResolved(Boolean resolved, Pageable pageable);

    /**
     * Busca alertas por status
     */
    Page<Alert> findByStatus(Alert.AlertStatus status, Pageable pageable);

    /**
     * Busca alertas por tipo
     */
    Page<Alert> findByAlertType(Alert.AlertType alertType, Pageable pageable);

    /**
     * Busca alertas ativos (não resolvidos)
     */
    @Query("SELECT a FROM Alert a WHERE a.resolved = false ORDER BY a.createdAt DESC")
    List<Alert> findActiveAlerts();

    /**
     * Busca alertas ativos com paginação
     */
    @Query("SELECT a FROM Alert a WHERE a.resolved = false ORDER BY a.createdAt DESC")
    Page<Alert> findActiveAlerts(Pageable pageable);

    /**
     * Busca alertas críticos ativos
     */
    @Query("SELECT a FROM Alert a WHERE a.resolved = false AND a.severity.level <= 2 ORDER BY a.createdAt DESC")
    List<Alert> findActiveCriticalAlerts();

    /**
     * Busca alertas por servidor ID
     */
    Page<Alert> findByServerId(Long serverId, Pageable pageable);

    /**
     * Busca alertas por título contendo texto
     */
    Page<Alert> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Busca alertas por período
     */
    @Query("SELECT a FROM Alert a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    Page<Alert> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate, 
                               Pageable pageable);

    /**
     * Busca alertas com filtros complexos
     */
    @Query("SELECT a FROM Alert a WHERE " +
           "(:serverId IS NULL OR a.server.id = :serverId) AND " +
           "(:severityId IS NULL OR a.severity.id = :severityId) AND " +
           "(:resolved IS NULL OR a.resolved = :resolved) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:alertType IS NULL OR a.alertType = :alertType) AND " +
           "(:title IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
           "ORDER BY a.createdAt DESC")
    Page<Alert> findByFilters(@Param("serverId") Long serverId,
                             @Param("severityId") Long severityId,
                             @Param("resolved") Boolean resolved,
                             @Param("status") Alert.AlertStatus status,
                             @Param("alertType") Alert.AlertType alertType,
                             @Param("title") String title,
                             Pageable pageable);

    /**
     * Conta alertas ativos
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.resolved = false")
    long countActiveAlerts();

    /**
     * Conta alertas críticos ativos
     */
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.resolved = false AND a.severity.level <= 2")
    long countActiveCriticalAlerts();

    /**
     * Conta alertas por servidor
     */
    long countByServerAndResolved(Server server, Boolean resolved);

    /**
     * Conta alertas por severidade
     */
    long countBySeverityAndResolved(Severity severity, Boolean resolved);

    /**
     * Busca alertas recentes (últimas 24 horas)
     */
    @Query("SELECT a FROM Alert a WHERE a.createdAt >= :since ORDER BY a.createdAt DESC")
    List<Alert> findRecentAlerts(@Param("since") LocalDateTime since);

    /**
     * Busca últimos alertas por servidor
     */
    @Query("SELECT a FROM Alert a WHERE a.server.id = :serverId ORDER BY a.createdAt DESC")
    Page<Alert> findLatestByServerId(@Param("serverId") Long serverId, Pageable pageable);

    /**
     * Busca alertas por métrica
     */
    @Query("SELECT a FROM Alert a WHERE a.metricName = :metricName ORDER BY a.createdAt DESC")
    Page<Alert> findByMetricName(@Param("metricName") String metricName, Pageable pageable);

    /**
     * Estatísticas de alertas por status
     */
    @Query("SELECT a.status, COUNT(a) FROM Alert a GROUP BY a.status")
    List<Object[]> getAlertStatsByStatus();

    /**
     * Estatísticas de alertas por tipo
     */
    @Query("SELECT a.alertType, COUNT(a) FROM Alert a GROUP BY a.alertType")
    List<Object[]> getAlertStatsByType();

    /**
     * Estatísticas de alertas por severidade
     */
    @Query("SELECT s.name, COUNT(a) FROM Alert a JOIN a.severity s GROUP BY s.name ORDER BY s.level")
    List<Object[]> getAlertStatsBySeverity();

    /**
     * Top servidores com mais alertas
     */
    @Query("SELECT s.name, COUNT(a) FROM Alert a JOIN a.server s WHERE a.resolved = false " +
           "GROUP BY s.id, s.name ORDER BY COUNT(a) DESC")
    List<Object[]> getTopServersWithAlerts(Pageable pageable);
}