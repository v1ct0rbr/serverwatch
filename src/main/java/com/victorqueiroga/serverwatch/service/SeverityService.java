package com.victorqueiroga.serverwatch.service;

import com.victorqueiroga.serverwatch.model.Severity;
import com.victorqueiroga.serverwatch.repository.SeverityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciamento de severidades
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeverityService {

    private final SeverityRepository severityRepository;

    /**
     * Lista todas as severidades ordenadas por nível
     */
    @Transactional(readOnly = true)
    public List<Severity> findAll() {
        return severityRepository.findAllByOrderByLevel();
    }

    /**
     * Lista todas as severidades ordenadas por nível (descendente)
     */
    @Transactional(readOnly = true)
    public List<Severity> findAllDesc() {
        return severityRepository.findAllByOrderByLevelDesc();
    }

    /**
     * Busca severidade por ID
     */
    @Transactional(readOnly = true)
    public Optional<Severity> findById(Long id) {
        return severityRepository.findById(id);
    }

    /**
     * Busca severidade por nome
     */
    @Transactional(readOnly = true)
    public Optional<Severity> findByName(String name) {
        return severityRepository.findByNameIgnoreCase(name);
    }

    /**
     * Busca severidade por nível
     */
    @Transactional(readOnly = true)
    public Optional<Severity> findByLevel(Integer level) {
        return severityRepository.findByLevel(level);
    }

    /**
     * Busca severidades críticas
     */
    @Transactional(readOnly = true)
    public List<Severity> findCriticalSeverities() {
        return severityRepository.findCriticalSeverities();
    }

    /**
     * Busca severidades altas
     */
    @Transactional(readOnly = true)
    public List<Severity> findHighSeverities() {
        return severityRepository.findHighSeverities();
    }

    /**
     * Busca severidades baixas
     */
    @Transactional(readOnly = true)
    public List<Severity> findLowSeverities() {
        return severityRepository.findLowSeverities();
    }

    /**
     * Busca severidade padrão
     */
    @Transactional(readOnly = true)
    public Optional<Severity> findDefaultSeverity() {
        return severityRepository.findDefaultSeverity();
    }

    /**
     * Busca severidade mais crítica
     */
    @Transactional(readOnly = true)
    public Optional<Severity> findMostCritical() {
        return severityRepository.findMostCritical();
    }

    /**
     * Busca severidade menos crítica
     */
    @Transactional(readOnly = true)
    public Optional<Severity> findLeastCritical() {
        return severityRepository.findLeastCritical();
    }

    /**
     * Salva ou atualiza uma severidade
     */
    public Severity save(Severity severity) {
        if (severity.getId() == null) {
            log.info("Criando nova severidade: {}", severity.getName());
        } else {
            log.info("Atualizando severidade ID: {}", severity.getId());
        }
        
        return severityRepository.save(severity);
    }

    /**
     * Cria uma nova severidade
     */
    public Severity createSeverity(String name, Integer level, String color, 
                                  String bootstrapClass, String icon) {
        // Verifica se já existe severidade com o mesmo nome ou nível
        if (severityRepository.existsByNameIgnoreCase(name)) {
            throw new RuntimeException("Já existe uma severidade com o nome: " + name);
        }
        
        if (severityRepository.existsByLevel(level)) {
            throw new RuntimeException("Já existe uma severidade com o nível: " + level);
        }

        Severity severity = new Severity();
        severity.setName(name);
        severity.setLevel(level);
        severity.setColor(color);
        severity.setBootstrapClass(bootstrapClass);
        severity.setIcon(icon);

        return save(severity);
    }

    /**
     * Exclui uma severidade
     */
    public void deleteById(Long id) {
        if (severityRepository.existsById(id)) {
            // Verifica se existem alertas usando esta severidade
            List<Object[]> alertCount = severityRepository.countAlertsBySeverity();
            boolean hasAlerts = alertCount.stream()
                    .anyMatch(row -> {
                        Severity sev = severityRepository.findById(id).orElse(null);
                        return sev != null && sev.getName().equals(row[0]) && ((Long) row[1]) > 0;
                    });
            
            if (hasAlerts) {
                throw new RuntimeException("Não é possível excluir severidade pois existem alertas associados");
            }
            
            severityRepository.deleteById(id);
            log.info("Severidade ID: {} excluída", id);
        } else {
            throw new RuntimeException("Severidade não encontrada com ID: " + id);
        }
    }

    /**
     * Verifica se existe severidade com nome
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return severityRepository.existsByNameIgnoreCase(name);
    }

    /**
     * Verifica se existe severidade com nível
     */
    @Transactional(readOnly = true)
    public boolean existsByLevel(Integer level) {
        return severityRepository.existsByLevel(level);
    }

    /**
     * Obtém próximo nível disponível
     */
    @Transactional(readOnly = true)
    public Integer getNextAvailableLevel() {
        return severityRepository.getNextAvailableLevel();
    }

    /**
     * Estatísticas de alertas por severidade
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAlertStatsBySeverity() {
        return severityRepository.countAlertsBySeverity();
    }

    /**
     * Estatísticas de alertas ativos por severidade
     */
    @Transactional(readOnly = true)
    public List<Object[]> getActiveAlertStatsBySeverity() {
        return severityRepository.countActiveAlertsBySeverity();
    }

    /**
     * Busca severidades com alertas ativos
     */
    @Transactional(readOnly = true)
    public List<Severity> findSeveritiesWithActiveAlerts() {
        return severityRepository.findSeveritiesWithActiveAlerts();
    }

    /**
     * Busca severidades sem alertas
     */
    @Transactional(readOnly = true)
    public List<Severity> findSeveritiesWithoutAlerts() {
        return severityRepository.findSeveritiesWithoutAlerts();
    }

    /**
     * Inicializa severidades padrão se não existirem
     */
    public void initializeDefaultSeverities() {
        if (severityRepository.count() == 0) {
            log.info("Inicializando severidades padrão...");
            
            // Critical
            createSeverity("Critical", 1, "#dc3545", "danger", "fas fa-exclamation-circle");
            
            // High
            createSeverity("High", 2, "#fd7e14", "warning", "fas fa-exclamation-triangle");
            
            // Medium
            createSeverity("Medium", 3, "#ffc107", "warning", "fas fa-exclamation");
            
            // Low
            createSeverity("Low", 4, "#20c997", "info", "fas fa-info-circle");
            
            // Info
            createSeverity("Info", 5, "#0dcaf0", "info", "fas fa-info");
            
            log.info("Severidades padrão criadas com sucesso");
        }
    }

    /**
     * Busca severidade por range de nível
     */
    @Transactional(readOnly = true)
    public List<Severity> findByLevelRange(Integer minLevel, Integer maxLevel) {
        return severityRepository.findByLevelRange(minLevel, maxLevel);
    }

    /**
     * Busca severidades por nível menor ou igual
     */
    @Transactional(readOnly = true)
    public List<Severity> findByLevelLessThanEqual(Integer level) {
        return severityRepository.findByLevelLessThanEqual(level);
    }

    /**
     * Busca severidades por nível maior ou igual
     */
    @Transactional(readOnly = true)
    public List<Severity> findByLevelGreaterThanEqual(Integer level) {
        return severityRepository.findByLevelGreaterThanEqual(level);
    }
}