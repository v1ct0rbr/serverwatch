package com.victorqueiroga.serverwatch.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.victorqueiroga.serverwatch.model.Alert;
import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.model.Severity;
import com.victorqueiroga.serverwatch.repository.AlertRepository;
import com.victorqueiroga.serverwatch.repository.SeverityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para gerenciamento de alertas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@Profile("!dev")  // Exclui este service do profile dev
public class AlertService {

    private final AlertRepository alertRepository;
    private final SeverityRepository severityRepository;

    /**
     * Lista todos os alertas com paginação
     */
    @Transactional(readOnly = true)
    public Page<Alert> findAll(Pageable pageable) {
        return alertRepository.findAll(pageable);
    }

    /**
     * Busca alerta por ID
     */
    @Transactional(readOnly = true)
    public Optional<Alert> findById(Long id) {
        return alertRepository.findById(id);
    }

    /**
     * Busca alertas ativos com paginação
     */
    @Transactional(readOnly = true)
    public Page<Alert> findActiveAlerts(Pageable pageable) {
        return alertRepository.findActiveAlerts(pageable);
    }

    /**
     * Busca alertas por servidor
     */
    @Transactional(readOnly = true)
    public Page<Alert> findByServer(Server server, Pageable pageable) {
        return alertRepository.findByServer(server, pageable);
    }

    /**
     * Busca alertas por severidade
     */
    @Transactional(readOnly = true)
    public Page<Alert> findBySeverity(Severity severity, Pageable pageable) {
        return alertRepository.findBySeverity(severity, pageable);
    }

    /**
     * Busca alertas com filtros
     */
    @Transactional(readOnly = true)
    public Page<Alert> findByFilters(Long serverId, Long severityId, Boolean resolved, 
                                    Alert.AlertStatus status, Alert.AlertType alertType, 
                                    String title, Pageable pageable) {
        return alertRepository.findByFilters(serverId, severityId, resolved, status, alertType, title, pageable);
    }

    /**
     * Busca alertas recentes (últimas 24 horas)
     */
    @Transactional(readOnly = true)
    public List<Alert> findRecentAlerts() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        return alertRepository.findRecentAlerts(since);
    }

    /**
     * Busca alertas críticos ativos
     */
    @Transactional(readOnly = true)
    public List<Alert> findActiveCriticalAlerts() {
        return alertRepository.findActiveCriticalAlerts();
    }

    /**
     * Salva ou atualiza um alerta
     */
    public Alert save(Alert alert) {
        if (alert.getId() == null) {
            alert.setCreatedAt(LocalDateTime.now());
            log.info("Criando novo alerta: {} para servidor: {}", 
                    alert.getTitle(), alert.getServer().getName());
        } else {
            alert.setUpdatedAt(LocalDateTime.now());
            log.info("Atualizando alerta ID: {}", alert.getId());
        }
        
        return alertRepository.save(alert);
    }

    /**
     * Cria um novo alerta
     */
    public Alert createAlert(String title, String description, Server server, 
                            Severity severity, Alert.AlertType alertType, String metricName, 
                            String currentValue, String thresholdValue) {
        Alert alert = new Alert();
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setServer(server);
        alert.setSeverity(severity);
        alert.setAlertType(alertType);
        alert.setMetricName(metricName);
        alert.setCurrentValue(currentValue);
        alert.setThresholdValue(thresholdValue);
        alert.setStatus(Alert.AlertStatus.OPEN);
        alert.setResolved(false);
        alert.setCreatedAt(LocalDateTime.now());

        return save(alert);
    }

    /**
     * Resolve um alerta
     */
    public Alert resolveAlert(Long alertId, String resolvedBy, String resolutionNotes) {
        Optional<Alert> alertOpt = findById(alertId);
        if (alertOpt.isPresent()) {
            Alert alert = alertOpt.get();
            alert.resolve(resolvedBy, resolutionNotes);
            log.info("Alerta ID: {} resolvido por: {}", alertId, resolvedBy);
            return save(alert);
        }
        throw new RuntimeException("Alerta não encontrado com ID: " + alertId);
    }

    /**
     * Reconhece um alerta
     */
    public Alert acknowledgeAlert(Long alertId) {
        Optional<Alert> alertOpt = findById(alertId);
        if (alertOpt.isPresent()) {
            Alert alert = alertOpt.get();
            alert.acknowledge();
            log.info("Alerta ID: {} reconhecido", alertId);
            return save(alert);
        }
        throw new RuntimeException("Alerta não encontrado com ID: " + alertId);
    }

    /**
     * Atualiza status do alerta
     */
    public Alert updateAlertStatus(Long alertId, Alert.AlertStatus newStatus) {
        Optional<Alert> alertOpt = findById(alertId);
        if (alertOpt.isPresent()) {
            Alert alert = alertOpt.get();
            alert.setStatus(newStatus);
            log.info("Status do alerta ID: {} atualizado para: {}", alertId, newStatus);
            return save(alert);
        }
        throw new RuntimeException("Alerta não encontrado com ID: " + alertId);
    }

    /**
     * Exclui um alerta
     */
    public void deleteById(Long id) {
        if (alertRepository.existsById(id)) {
            alertRepository.deleteById(id);
            log.info("Alerta ID: {} excluído", id);
        } else {
            throw new RuntimeException("Alerta não encontrado com ID: " + id);
        }
    }

    /**
     * Conta alertas ativos
     */
    @Transactional(readOnly = true)
    public long countActiveAlerts() {
        return alertRepository.countActiveAlerts();
    }

    /**
     * Conta alertas críticos ativos
     */
    @Transactional(readOnly = true)
    public long countActiveCriticalAlerts() {
        return alertRepository.countActiveCriticalAlerts();
    }

    /**
     * Estatísticas de alertas por status
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAlertStatsByStatus() {
        return alertRepository.getAlertStatsByStatus();
    }

    /**
     * Estatísticas de alertas por tipo
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAlertStatsByType() {
        return alertRepository.getAlertStatsByType();
    }

    /**
     * Estatísticas de alertas por severidade
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAlertStatsBySeverity() {
        return alertRepository.getAlertStatsBySeverity();
    }

    /**
     * Top servidores com mais alertas
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTopServersWithAlerts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return alertRepository.getTopServersWithAlerts(pageable);
    }

    /**
     * Busca alertas por período
     */
    @Transactional(readOnly = true)
    public Page<Alert> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return alertRepository.findByDateRange(startDate, endDate, pageable);
    }

    /**
     * Busca últimos alertas por servidor
     */
    @Transactional(readOnly = true)
    public Page<Alert> findLatestByServerId(Long serverId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return alertRepository.findLatestByServerId(serverId, pageable);
    }

    /**
     * Cria alerta de monitoramento automático
     */
    public Alert createMonitoringAlert(Server server, String metricName, Double value, 
                                     String unit, Double threshold, boolean isCritical) {
        // Busca severidade apropriada
        Severity severity;
        if (isCritical) {
            severity = severityRepository.findCriticalSeverities().stream()
                    .findFirst()
                    .orElseGet(() -> getDefaultSeverity());
        } else {
            severity = severityRepository.findHighSeverities().stream()
                    .findFirst()
                    .orElseGet(() -> getDefaultSeverity());
        }

        String title = String.format("%s excedeu o limite em %s", metricName, server.getName());
        String description = String.format("Métrica %s atingiu valor %.2f %s (limite: %.2f %s)", 
                metricName, value, unit, threshold, unit);

        String currentValue = String.format("%.2f %s", value, unit);
        String thresholdValue = String.format("%.2f %s", threshold, unit);

        return createAlert(title, description, server, severity, 
                Alert.AlertType.MONITORING, metricName, currentValue, thresholdValue);
    }

    /**
     * Obtém severidade padrão
     */
    private Severity getDefaultSeverity() {
        return severityRepository.findDefaultSeverity()
                .orElseGet(() -> {
                    // Cria severidade padrão se não existir
                    Severity defaultSeverity = new Severity();
                    defaultSeverity.setName("Medium");
                    defaultSeverity.setLevel(5);
                    defaultSeverity.setColor("#ffc107");
                    defaultSeverity.setBootstrapClass("warning");
                    defaultSeverity.setIcon("fas fa-exclamation-triangle");
                    return severityRepository.save(defaultSeverity);
                });
    }

    /**
     * Resolve alertas em lote por servidor
     */
    public int resolveAlertsByServer(Long serverId, String resolvedBy, String resolutionNotes) {
        Page<Alert> alerts = alertRepository.findByServerId(serverId, Pageable.unpaged());
        int resolved = 0;
        
        for (Alert alert : alerts) {
            if (!alert.getResolved()) {
                alert.resolve(resolvedBy, resolutionNotes);
                save(alert);
                resolved++;
            }
        }
        
        log.info("Resolvidos {} alertas para servidor ID: {}", resolved, serverId);
        return resolved;
    }

    /**
     * Limpa alertas antigos resolvidos
     */
    public int cleanupOldResolvedAlerts(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        
        // Busca alertas resolvidos antigos
        Page<Alert> oldAlerts = alertRepository.findByDateRange(
                LocalDateTime.of(2000, 1, 1, 0, 0), 
                cutoffDate, 
                Pageable.unpaged()
        );
        
        int deleted = 0;
        for (Alert alert : oldAlerts) {
            if (alert.getResolved()) {
                alertRepository.delete(alert);
                deleted++;
            }
        }
        
        log.info("Removidos {} alertas antigos resolvidos", deleted);
        return deleted;
    }
}