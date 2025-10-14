package com.victorqueiroga.serverwatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.victorqueiroga.serverwatch.model.OperationSystem;
import com.victorqueiroga.serverwatch.model.Server;

@Repository
public interface ServerRepository extends JpaRepository<Server, Long> {

    /**
     * Busca servidor por nome exato
     */
    Optional<Server> findByName(String name);

    /**
     * Busca servidor por endereço IP
     */
    Optional<Server> findByIpAddress(String ipAddress);

    /**
     * Busca servidores por sistema operacional
     */
    List<Server> findByOperationSystem(OperationSystem operationSystem);

    /**
     * Busca servidores por nome contendo o termo (case insensitive)
     */
    List<Server> findByNameContainingIgnoreCase(String name);

    /**
     * Busca servidores por endereço IP contendo o termo
     */
    List<Server> findByIpAddressContaining(String ipAddress);

    /**
     * Verifica se existe servidor com o nome
     */
    boolean existsByName(String name);

    /**
     * Verifica se existe servidor com o endereço IP
     */
    boolean existsByIpAddress(String ipAddress);

    /**
     * Busca servidores por sistema operacional ID
     */
    List<Server> findByOperationSystemId(Long operationSystemId);

    /**
     * Busca servidores ordenados por nome
     */
    List<Server> findAllByOrderByNameAsc();

    /**
     * Busca servidores com query customizada para filtros complexos
     */
    @Query("SELECT s FROM Server s WHERE " +
            "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:ipAddress IS NULL OR s.ipAddress LIKE CONCAT('%', :ipAddress, '%')) AND " +
            "(:operationSystemId IS NULL OR s.operationSystem.id = :operationSystemId)")
    List<Server> findByFilters(@Param("name") String name,
            @Param("ipAddress") String ipAddress,
            @Param("operationSystemId") Long operationSystemId);

    /**
     * Conta servidores por sistema operacional
     */
    @Query("SELECT COUNT(s) FROM Server s WHERE s.operationSystem.id = :operationSystemId")
    long countByOperationSystemId(@Param("operationSystemId") Long operationSystemId);

    /**
     * Busca servidor com sistema operacional
     */
    @Query("SELECT s FROM Server s LEFT JOIN FETCH s.operationSystem WHERE s.id = :id")
    Optional<Server> findByIdWithOperationSystem(@Param("id") Long id);
}