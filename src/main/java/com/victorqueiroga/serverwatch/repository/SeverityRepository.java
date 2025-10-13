package com.victorqueiroga.serverwatch.repository;

import com.victorqueiroga.serverwatch.model.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para gerenciamento de severidades dos alertas
 */
@Repository
public interface SeverityRepository extends JpaRepository<Severity, Long> {

    /**
     * Busca severidade por nome
     */
    Optional<Severity> findByName(String name);

    /**
     * Busca severidade por nome (case insensitive)
     */
    Optional<Severity> findByNameIgnoreCase(String name);

    /**
     * Busca severidade por nível
     */
    Optional<Severity> findByLevel(Integer level);

    /**
     * Busca severidades por nível menor ou igual
     */
    List<Severity> findByLevelLessThanEqual(Integer level);

    /**
     * Busca severidades por nível maior ou igual
     */
    List<Severity> findByLevelGreaterThanEqual(Integer level);

    /**
     * Busca severidades por range de nível
     */
    @Query("SELECT s FROM Severity s WHERE s.level BETWEEN :minLevel AND :maxLevel ORDER BY s.level")
    List<Severity> findByLevelRange(@Param("minLevel") Integer minLevel, @Param("maxLevel") Integer maxLevel);

    /**
     * Busca todas as severidades ordenadas por nível
     */
    List<Severity> findAllByOrderByLevel();

    /**
     * Busca todas as severidades ordenadas por nível descendente
     */
    List<Severity> findAllByOrderByLevelDesc();

    /**
     * Busca severidades críticas (nível 1-2)
     */
    @Query("SELECT s FROM Severity s WHERE s.level <= 2 ORDER BY s.level")
    List<Severity> findCriticalSeverities();

    /**
     * Busca severidades altas (nível 3-4)
     */
    @Query("SELECT s FROM Severity s WHERE s.level BETWEEN 3 AND 4 ORDER BY s.level")
    List<Severity> findHighSeverities();

    /**
     * Busca severidades baixas (nível 5+)
     */
    @Query("SELECT s FROM Severity s WHERE s.level >= 5 ORDER BY s.level")
    List<Severity> findLowSeverities();

    /**
     * Verifica se existe severidade com o nome
     */
    boolean existsByName(String name);

    /**
     * Verifica se existe severidade com o nome (case insensitive)
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Verifica se existe severidade com o nível
     */
    boolean existsByLevel(Integer level);

    /**
     * Busca severidade padrão (nível médio)
     */
    @Query("SELECT s FROM Severity s WHERE s.name = 'Medium' OR s.level = 5")
    Optional<Severity> findDefaultSeverity();

    /**
     * Busca severidade mais crítica
     */
    @Query("SELECT s FROM Severity s WHERE s.level = (SELECT MIN(s2.level) FROM Severity s2)")
    Optional<Severity> findMostCritical();

    /**
     * Busca severidade menos crítica
     */
    @Query("SELECT s FROM Severity s WHERE s.level = (SELECT MAX(s2.level) FROM Severity s2)")
    Optional<Severity> findLeastCritical();

    /**
     * Conta alertas por severidade
     */
    @Query("SELECT s.name, COUNT(a) FROM Severity s LEFT JOIN Alert a ON a.severity.id = s.id GROUP BY s.id, s.name ORDER BY s.level")
    List<Object[]> countAlertsBySeverity();

    /**
     * Conta alertas ativos por severidade
     */
    @Query("SELECT s.name, COUNT(a) FROM Severity s LEFT JOIN Alert a ON a.severity.id = s.id " +
           "WHERE a.resolved = false OR a.resolved IS NULL " +
           "GROUP BY s.id, s.name ORDER BY s.level")
    List<Object[]> countActiveAlertsBySeverity();

    /**
     * Busca severidades com alertas ativos
     */
    @Query("SELECT DISTINCT s FROM Severity s WHERE s.id IN " +
           "(SELECT DISTINCT a.severity.id FROM Alert a WHERE a.resolved = false)")
    List<Severity> findSeveritiesWithActiveAlerts();

    /**
     * Busca severidades sem alertas
     */
    @Query("SELECT s FROM Severity s WHERE s.id NOT IN " +
           "(SELECT DISTINCT a.severity.id FROM Alert a)")
    List<Severity> findSeveritiesWithoutAlerts();

    /**
     * Busca próximo nível disponível
     */
    @Query("SELECT COALESCE(MAX(s.level), 0) + 1 FROM Severity s")
    Integer getNextAvailableLevel();
}