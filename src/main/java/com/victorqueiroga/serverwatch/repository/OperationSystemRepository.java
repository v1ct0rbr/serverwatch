package com.victorqueiroga.serverwatch.repository;

import com.victorqueiroga.serverwatch.model.OperationSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para gerenciamento de sistemas operacionais
 */
@Repository
public interface OperationSystemRepository extends JpaRepository<OperationSystem, Long> {

    /**
     * Busca sistema operacional por nome
     */
    Optional<OperationSystem> findByName(String name);

    /**
     * Busca sistemas operacionais ordenados por nome
     */
    List<OperationSystem> findAllByOrderByNameAsc();

    /**
     * Verifica se existe sistema operacional com o nome
     */
    boolean existsByName(String name);
}