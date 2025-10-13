package com.victorqueiroga.serverwatch.repository;

import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.model.ServerMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para gerenciamento de métricas dos servidores
 */
@Repository
public interface ServerMetricRepository extends JpaRepository<ServerMetric, Long> {

    /**
     * Busca métricas por servidor
     */
    Page<ServerMetric> findByServer(Server server, Pageable pageable);

    /**
     * Busca métricas por servidor ID
     */
    Page<ServerMetric> findByServerId(Long serverId, Pageable pageable);

    /**
     * Busca métricas por categoria
     */
    Page<ServerMetric> findByCategory(ServerMetric.MetricCategory category, Pageable pageable);

    /**
     * Busca métricas por nome
     */
    Page<ServerMetric> findByMetricName(String metricName, Pageable pageable);

    /**
     * Busca métricas por servidor e categoria
     */
    Page<ServerMetric> findByServerAndCategory(Server server, ServerMetric.MetricCategory category, Pageable pageable);

    /**
     * Busca métricas por servidor e nome da métrica
     */
    List<ServerMetric> findByServerAndMetricNameOrderByTimestampDesc(Server server, String metricName);

    /**
     * Busca última métrica por servidor e nome
     */
    @Query("SELECT m FROM ServerMetric m WHERE m.server = :server AND m.metricName = :metricName " +
           "ORDER BY m.timestamp DESC")
    Optional<ServerMetric> findLatestByServerAndMetricName(@Param("server") Server server, 
                                                          @Param("metricName") String metricName);

    /**
     * Busca métricas por período
     */
    @Query("SELECT m FROM ServerMetric m WHERE m.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY m.timestamp DESC")
    Page<ServerMetric> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);

    /**
     * Busca métricas por servidor e período
     */
    @Query("SELECT m FROM ServerMetric m WHERE m.server.id = :serverId " +
           "AND m.timestamp BETWEEN :startDate AND :endDate " +
           "ORDER BY m.timestamp DESC")
    List<ServerMetric> findByServerAndDateRange(@Param("serverId") Long serverId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Busca métricas recentes por servidor (última hora)
     */
    @Query("SELECT m FROM ServerMetric m WHERE m.server.id = :serverId " +
           "AND m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<ServerMetric> findRecentMetrics(@Param("serverId") Long serverId, 
                                        @Param("since") LocalDateTime since);

    /**
     * Busca métricas que excedem threshold
     */
    @Query("SELECT m FROM ServerMetric m WHERE " +
           "(m.warningThreshold IS NOT NULL AND m.value >= m.warningThreshold) OR " +
           "(m.criticalThreshold IS NOT NULL AND m.value >= m.criticalThreshold)")
    List<ServerMetric> findMetricsExceedingThresholds();

    /**
     * Busca métricas críticas
     */
    @Query("SELECT m FROM ServerMetric m WHERE m.criticalThreshold IS NOT NULL " +
           "AND m.value >= m.criticalThreshold")
    List<ServerMetric> findCriticalMetrics();

    /**
     * Busca métricas com warning
     */
    @Query("SELECT m FROM ServerMetric m WHERE m.warningThreshold IS NOT NULL " +
           "AND m.value >= m.warningThreshold " +
           "AND (m.criticalThreshold IS NULL OR m.value < m.criticalThreshold)")
    List<ServerMetric> findWarningMetrics();

    /**
     * Busca últimas métricas por categoria para um servidor
     */
    @Query("SELECT m FROM ServerMetric m WHERE m.server.id = :serverId " +
           "AND m.category = :category " +
           "AND m.timestamp = (SELECT MAX(m2.timestamp) FROM ServerMetric m2 " +
           "WHERE m2.server.id = :serverId AND m2.category = :category AND m2.metricName = m.metricName)")
    List<ServerMetric> findLatestMetricsByServerAndCategory(@Param("serverId") Long serverId,
                                                           @Param("category") ServerMetric.MetricCategory category);

    /**
     * Busca métricas por OID SNMP
     */
    List<ServerMetric> findBySnmpOid(String snmpOid);

    /**
     * Remove métricas antigas (cleanup)
     */
    @Query("DELETE FROM ServerMetric m WHERE m.timestamp < :cutoffDate")
    void deleteOldMetrics(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Conta métricas por servidor
     */
    long countByServer(Server server);

    /**
     * Conta métricas por categoria
     */
    long countByCategory(ServerMetric.MetricCategory category);

    /**
     * Média de valores por métrica
     */
    @Query("SELECT m.metricName, AVG(m.value) FROM ServerMetric m " +
           "WHERE m.server.id = :serverId AND m.timestamp >= :since " +
           "GROUP BY m.metricName")
    List<Object[]> getAverageMetricValues(@Param("serverId") Long serverId,
                                         @Param("since") LocalDateTime since);

    /**
     * Valores máximos por métrica
     */
    @Query("SELECT m.metricName, MAX(m.value) FROM ServerMetric m " +
           "WHERE m.server.id = :serverId AND m.timestamp >= :since " +
           "GROUP BY m.metricName")
    List<Object[]> getMaxMetricValues(@Param("serverId") Long serverId,
                                     @Param("since") LocalDateTime since);

    /**
     * Valores mínimos por métrica
     */
    @Query("SELECT m.metricName, MIN(m.value) FROM ServerMetric m " +
           "WHERE m.server.id = :serverId AND m.timestamp >= :since " +
           "GROUP BY m.metricName")
    List<Object[]> getMinMetricValues(@Param("serverId") Long serverId,
                                     @Param("since") LocalDateTime since);

    /**
     * Estatísticas de uso de CPU
     */
    @Query("SELECT AVG(m.value), MIN(m.value), MAX(m.value), COUNT(m) FROM ServerMetric m " +
           "WHERE m.server.id = :serverId AND m.metricName = 'CPU_USAGE' " +
           "AND m.timestamp >= :since")
    List<Object[]> getCpuUsageStats(@Param("serverId") Long serverId,
                                   @Param("since") LocalDateTime since);

    /**
     * Estatísticas de uso de memória
     */
    @Query("SELECT AVG(m.value), MIN(m.value), MAX(m.value), COUNT(m) FROM ServerMetric m " +
           "WHERE m.server.id = :serverId AND m.metricName = 'MEMORY_USAGE' " +
           "AND m.timestamp >= :since")
    List<Object[]> getMemoryUsageStats(@Param("serverId") Long serverId,
                                      @Param("since") LocalDateTime since);

    /**
     * Busca servidores com métricas disponíveis
     */
    @Query("SELECT DISTINCT m.server FROM ServerMetric m WHERE m.timestamp >= :since")
    List<Server> findServersWithRecentMetrics(@Param("since") LocalDateTime since);

    /**
     * Busca métricas para dashboard (resumo)
     */
    @Query("SELECT m.server.id, m.server.name, m.metricName, m.value, m.unit, " +
           "m.warningThreshold, m.criticalThreshold, m.timestamp FROM ServerMetric m " +
           "WHERE m.timestamp = (SELECT MAX(m2.timestamp) FROM ServerMetric m2 " +
           "WHERE m2.server.id = m.server.id AND m2.metricName = m.metricName) " +
           "ORDER BY m.server.name, m.metricName")
    List<Object[]> getDashboardMetrics();
}