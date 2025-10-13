package com.victorqueiroga.serverwatch.service;

import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.model.OperationSystem;
import com.victorqueiroga.serverwatch.repository.ServerRepository;
import com.victorqueiroga.serverwatch.repository.OperationSystemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciamento de servidores monitorados
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServerService {

    private final ServerRepository serverRepository;
    private final OperationSystemRepository operationSystemRepository;

    /**
     * Busca todos os servidores com paginação
     */
    public Page<Server> findAll(Pageable pageable) {
        log.debug("Buscando servidores com paginação: {}", pageable);
        return serverRepository.findAll(pageable);
    }

    /**
     * Busca todos os servidores sem paginação
     */
    public List<Server> findAll() {
        log.debug("Buscando todos os servidores");
        return serverRepository.findAll();
    }

    /**
     * Busca servidor por ID
     */
    public Optional<Server> findById(Long id) {
        log.debug("Buscando servidor por ID: {}", id);
        return serverRepository.findById(id);
    }

    /**
     * Busca servidor por nome
     */
    public Optional<Server> findByName(String name) {
        log.debug("Buscando servidor por nome: {}", name);
        return serverRepository.findByName(name);
    }

    /**
     * Busca servidor por endereço IP
     */
    public Optional<Server> findByIpAddress(String ipAddress) {
        log.debug("Buscando servidor por IP: {}", ipAddress);
        return serverRepository.findByIpAddress(ipAddress);
    }

    /**
     * Busca servidores por sistema operacional
     */
    public List<Server> findByOperationSystem(OperationSystem operationSystem) {
        log.debug("Buscando servidores por sistema operacional: {}", operationSystem.getName());
        return serverRepository.findByOperationSystem(operationSystem);
    }

    /**
     * Busca servidores por nome contendo o termo
     */
    public List<Server> findByNameContaining(String name) {
        log.debug("Buscando servidores por nome contendo: {}", name);
        return serverRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Salva um servidor (criar ou atualizar)
     */
    @Transactional
    public Server save(Server server) {
        log.info("Salvando servidor: {}", server.getName());
        
        // Validações básicas
        validateServer(server);
        
        // Verifica se já existe um servidor com o mesmo nome (exceto ele mesmo)
        Optional<Server> existingByName = serverRepository.findByName(server.getName());
        if (existingByName.isPresent() && !existingByName.get().getId().equals(server.getId())) {
            throw new IllegalArgumentException("Já existe um servidor com o nome: " + server.getName());
        }

        // Verifica se já existe um servidor com o mesmo IP (exceto ele mesmo)
        Optional<Server> existingByIp = serverRepository.findByIpAddress(server.getIpAddress());
        if (existingByIp.isPresent() && !existingByIp.get().getId().equals(server.getId())) {
            throw new IllegalArgumentException("Já existe um servidor com o IP: " + server.getIpAddress());
        }

        Server savedServer = serverRepository.save(server);
        log.info("Servidor salvo com sucesso - ID: {}, Nome: {}", savedServer.getId(), savedServer.getName());
        
        return savedServer;
    }

    /**
     * Deleta um servidor por ID
     */
    @Transactional
    public void deleteById(Long id) {
        log.info("Deletando servidor com ID: {}", id);
        
        if (!serverRepository.existsById(id)) {
            throw new IllegalArgumentException("Servidor não encontrado com ID: " + id);
        }
        
        serverRepository.deleteById(id);
        log.info("Servidor deletado com sucesso - ID: {}", id);
    }

    /**
     * Verifica se existe servidor com o nome
     */
    public boolean existsByName(String name) {
        return serverRepository.existsByName(name);
    }

    /**
     * Verifica se existe servidor com o IP
     */
    public boolean existsByIpAddress(String ipAddress) {
        return serverRepository.existsByIpAddress(ipAddress);
    }

    /**
     * Conta total de servidores
     */
    public long count() {
        return serverRepository.count();
    }

    /**
     * Busca todos os sistemas operacionais disponíveis
     */
    public List<OperationSystem> findAllOperationSystems() {
        log.debug("Buscando todos os sistemas operacionais");
        return operationSystemRepository.findAll();
    }

    /**
     * Busca sistema operacional por ID
     */
    public Optional<OperationSystem> findOperationSystemById(Long id) {
        log.debug("Buscando sistema operacional por ID: {}", id);
        return operationSystemRepository.findById(id);
    }

    /**
     * Valida os dados do servidor
     */
    private void validateServer(Server server) {
        if (server == null) {
            throw new IllegalArgumentException("Servidor não pode ser nulo");
        }
        
        if (server.getName() == null || server.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do servidor é obrigatório");
        }
        
        if (server.getIpAddress() == null || server.getIpAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Endereço IP é obrigatório");
        }
        
        if (server.getOperationSystem() == null || server.getOperationSystem().getId() == null) {
            throw new IllegalArgumentException("Sistema operacional é obrigatório");
        }
        
        // Validação básica de formato IP (IPv4)
        if (!isValidIpAddress(server.getIpAddress())) {
            throw new IllegalArgumentException("Endereço IP inválido: " + server.getIpAddress());
        }
    }

    /**
     * Validação básica de endereço IP IPv4
     */
    private boolean isValidIpAddress(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return false;
        }
        
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        
        try {
            for (String part : parts) {
                int num = Integer.parseInt(part);
                if (num < 0 || num > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}