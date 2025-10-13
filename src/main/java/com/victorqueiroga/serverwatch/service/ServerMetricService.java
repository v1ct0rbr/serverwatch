package com.victorqueiroga.serverwatch.service;

import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.model.ServerMetric;
import com.victorqueiroga.serverwatch.repository.ServerMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciamento de métricas dos servidores
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServerMetricService {

    private final ServerMetricRepository serverMetricRepository;

    /**
     * Lista todas as métricas com paginação
     */
    @Transactional(readOnly = true)
    public Page<ServerMetric> findAll(Pageable pageable) {
        return serverMetricRepository.findAll(pageable);
    }

    /**
     * Busca métrica por ID
     */
    @Transactional(readOnly = true)
    public Optional<ServerMetric> findById(Long id) {
        return serverMetricRepository.findById(id);
    }

    /**
     * Busca métricas por servidor
     */
    @Transactional(readOnly = true)
    public Page<ServerMetric> findByServer(Server server, Pageable pageable) {
        return serverMetricRepository.findByServer(server, pageable);
    }

    /**
     * Busca métricas por servidor ID
     */
    @Transactional(readOnly = true)
    public Page<ServerMetric> findByServerId(Long serverId, Pageable pageable) {
        return serverMetricRepository.findByServerId(serverId, pageable);
    }

    /**
     * Busca métricas por categoria
     */
    @Transactional(readOnly = true)
    public Page<ServerMetric> findByCategory(ServerMetric.MetricCategory category, Pageable pageable) {
        return serverMetricRepository.findByCategory(category, pageable);
    }

    /**
     * Busca última métrica por servidor e nome
     */
    @Transactional(readOnly = true)
    public Optional<ServerMetric> findLatestByServerAndMetricName(Server server, String metricName) {
        return serverMetricRepository.findLatestByServerAndMetricName(server, metricName);
    }

    /**
     * Busca métricas recentes por servidor (última hora)
     */
    @Transactional(readOnly = true)
    public List<ServerMetric> findRecentMetrics(Long serverId) {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        return serverMetricRepository.findRecentMetrics(serverId, since);
    }

    /**
     * Busca métricas por período
     */
    @Transactional(readOnly = true)
    public Page<ServerMetric> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return serverMetricRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * Busca métricas críticas
     */
    @Transactional(readOnly = true)
    public List<ServerMetric> findCriticalMetrics() {
        return serverMetricRepository.findCriticalMetrics();
    }

    /**
     * Busca métricas com warning
     */
    @Transactional(readOnly = true)
    public List<ServerMetric> findWarningMetrics() {
        return serverMetricRepository.findWarningMetrics();
    }

    /**
     * Busca métricas que excedem thresholds
     */
    @Transactional(readOnly = true)
    public List<ServerMetric> findMetricsExceedingThresholds() {
        return serverMetricRepository.findMetricsExceedingThresholds();
    }

    /**
     * Salva ou atualiza uma métrica
     */
    public ServerMetric save(ServerMetric metric) {
        if (metric.getId() == null) {
            // timestamp é automaticamente definido pela anotação @CreationTimestamp
            log.debug("Salvando nova métrica: {} para servidor: {}", 
                     metric.getMetricName(), metric.getServer().getName());
        }
        
        return serverMetricRepository.save(metric);
    }

    /**
     * Cria uma nova métrica
     */
    public ServerMetric createMetric(Server server, String metricName, Double value, String unit,
                                   ServerMetric.MetricCategory category, String snmpOid,
                                   Double warningThreshold, Double criticalThreshold) {
        ServerMetric metric = new ServerMetric();
        metric.setServer(server);
        metric.setMetricName(metricName);
        metric.setValue(BigDecimal.valueOf(value));
        metric.setUnit(unit);
        metric.setCategory(category);
        metric.setSnmpOid(snmpOid);
        if (warningThreshold != null) {
            metric.setWarningThreshold(BigDecimal.valueOf(warningThreshold));
        }
        if (criticalThreshold != null) {
            metric.setCriticalThreshold(BigDecimal.valueOf(criticalThreshold));
        }
        // timestamp é automaticamente definido pela anotação @CreationTimestamp

        return save(metric);
    }

    /**
     * Coleta métricas SNMP para um servidor
     */
    public List<ServerMetric> collectSnmpMetrics(Server server) {
        log.info("Coletando métricas SNMP para servidor: {}", server.getName());
        
        // TODO: Implementar coleta SNMP real
        // Por enquanto, simula algumas métricas
        List<ServerMetric> metrics = List.of(
            createMetric(server, "CPU_USAGE", 75.5, "%", 
                        ServerMetric.MetricCategory.CPU, "1.3.6.1.4.1.2021.11.9.0", 80.0, 90.0),
            createMetric(server, "MEMORY_USAGE", 82.3, "%", 
                        ServerMetric.MetricCategory.MEMORY, "1.3.6.1.4.1.2021.4.5.0", 85.0, 95.0),
            createMetric(server, "DISK_USAGE", 65.1, "%", 
                        ServerMetric.MetricCategory.DISK, "1.3.6.1.4.1.2021.9.1.9.1", 80.0, 90.0)
        );

        log.info("Coletadas {} métricas para servidor: {}", metrics.size(), server.getName());
        return metrics;
    }

    /**
     * Exclui uma métrica
     */
    public void deleteById(Long id) {
        if (serverMetricRepository.existsById(id)) {
            serverMetricRepository.deleteById(id);
            log.info("Métrica ID: {} excluída", id);
        } else {
            throw new RuntimeException("Métrica não encontrada com ID: " + id);
        }
    }

    /**
     * Remove métricas antigas (cleanup)
     */
    public void cleanupOldMetrics(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        serverMetricRepository.deleteOldMetrics(cutoffDate);
        log.info("Métricas antigas (mais de {} dias) removidas", daysOld);
    }

    /**
     * Conta métricas por servidor
     */
    @Transactional(readOnly = true)
    public long countByServer(Server server) {
        return serverMetricRepository.countByServer(server);
    }

    /**
     * Conta métricas por categoria
     */
    @Transactional(readOnly = true)
    public long countByCategory(ServerMetric.MetricCategory category) {
        return serverMetricRepository.countByCategory(category);
    }

    /**
     * Estatísticas de CPU
     */
    @Transactional(readOnly = true)
    public List<Object[]> getCpuUsageStats(Long serverId, LocalDateTime since) {
        return serverMetricRepository.getCpuUsageStats(serverId, since);
    }

    /**
     * Estatísticas de memória
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMemoryUsageStats(Long serverId, LocalDateTime since) {
        return serverMetricRepository.getMemoryUsageStats(serverId, since);
    }

    /**
     * Métricas para dashboard
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDashboardMetrics() {
        return serverMetricRepository.getDashboardMetrics();
    }

    /**
     * Busca servidores com métricas recentes
     */
    @Transactional(readOnly = true)
    public List<Server> findServersWithRecentMetrics() {
        LocalDateTime since = LocalDateTime.now().minusHours(2);
        return serverMetricRepository.findServersWithRecentMetrics(since);
    }

    /**
     * Busca últimas métricas por categoria para um servidor
     */
    @Transactional(readOnly = true)
    public List<ServerMetric> findLatestMetricsByServerAndCategory(Long serverId, 
                                                                  ServerMetric.MetricCategory category) {
        return serverMetricRepository.findLatestMetricsByServerAndCategory(serverId, category);
    }

    /**
     * Valores médios por métrica
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageMetricValues(Long serverId, LocalDateTime since) {
        return serverMetricRepository.getAverageMetricValues(serverId, since);
    }

    /**
     * Valores máximos por métrica
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMaxMetricValues(Long serverId, LocalDateTime since) {
        return serverMetricRepository.getMaxMetricValues(serverId, since);
    }

    /**
     * Valores mínimos por métrica
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMinMetricValues(Long serverId, LocalDateTime since) {
        return serverMetricRepository.getMinMetricValues(serverId, since);
    }

    /**
     * Busca métricas por servidor e período
     */
    @Transactional(readOnly = true)
    public List<ServerMetric> findByServerAndDateRange(Long serverId, LocalDateTime startDate, LocalDateTime endDate) {
        return serverMetricRepository.findByServerAndDateRange(serverId, startDate, endDate);
    }

    /**
     * Busca histórico de uma métrica específica
     */
    @Transactional(readOnly = true)
    public List<ServerMetric> findMetricHistory(Server server, String metricName, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return serverMetricRepository.findByServerAndDateRange(server.getId(), since, LocalDateTime.now())
                .stream()
                .filter(metric -> metric.getMetricName().equals(metricName))
                .toList();
    }

    /**
     * Verifica se métrica excede thresholds e retorna status
     */
    public String checkMetricStatus(ServerMetric metric) {
        if (metric.isCritical()) {
            return "CRITICAL";
        } else if (metric.isWarning()) {
            return "WARNING";
        }
        return "NORMAL";
    }

    /**
     * Busca métricas críticas recentes para dashboard
     */
    @Transactional(readOnly = true)
    public List<ServerMetric> findRecentCriticalMetrics(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return serverMetricRepository.findCriticalMetrics().stream()
                .filter(metric -> metric.getTimestamp().isAfter(since))
                .toList();
    }
}