package com.victorqueiroga.serverwatch.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.victorqueiroga.serverwatch.dto.ServerStatusDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Mock do ServerMonitoringService para desenvolvimento/testes
 */
@Slf4j
@Service
@Profile("dev")
public class MockServerMonitoringService {

    private List<ServerStatusDto> mockServers = new ArrayList<>();

    public void initializeCache() {
        log.info("Inicializando cache mock de servidores");
        
        // Cria alguns servidores mock
        mockServers.clear();
        
        ServerStatusDto server1 = ServerStatusDto.builder()
            .serverId(1L)
            .serverName("Server Web 01")
            .ipAddress("192.168.1.10")
            .operatingSystem("Ubuntu Server 20.04")
            .online(false)
            .status("PENDING")
            .lastCheck(LocalDateTime.now().minusMinutes(2))
            .build();
        
        ServerStatusDto server2 = ServerStatusDto.builder()
            .serverId(2L)
            .serverName("Server DB 01")
            .ipAddress("192.168.1.20")
            .operatingSystem("CentOS 8")
            .online(false)
            .status("PENDING")
            .lastCheck(LocalDateTime.now().minusMinutes(5))
            .build();
        
        ServerStatusDto server3 = ServerStatusDto.builder()
            .serverId(3L)
            .serverName("Server App 01")
            .ipAddress("192.168.1.30")
            .operatingSystem("Windows Server 2019")
            .online(false)
            .status("PENDING")
            .lastCheck(LocalDateTime.now().minusMinutes(1))
            .build();
        
        mockServers.add(server1);
        mockServers.add(server2);
        mockServers.add(server3);
        
        log.info("Cache mock inicializado com {} servidores", mockServers.size());
    }

    public List<ServerStatusDto> getAllServerStatus() {
        log.debug("Mock: Retornando lista de {} servidores com status básico", mockServers.size());
        
        if (mockServers.isEmpty()) {
            initializeCache();
        }
        
        return new ArrayList<>(mockServers);
    }

    public ServerStatusDto getServerStatus(Long serverId) {
        log.debug("Mock: Solicitando status do servidor ID: {}", serverId);
        
        return mockServers.stream()
            .filter(s -> s.getServerId().equals(serverId))
            .findFirst()
            .orElse(null);
    }

    public CompletableFuture<ServerStatusDto> refreshServerStatus(Long serverId) {
        log.info("Mock: Forçando atualização do servidor ID: {}", serverId);
        
        ServerStatusDto server = getServerStatus(serverId);
        if (server != null) {
            // Simula coleta de métricas
            server.setOnline(true);
            server.setStatus("ONLINE");
            server.setUptime("15 days, 3 hours");
            server.setCpuLoad1Min(12.5);
            server.setCpuLoad5Min(15.8);
            server.setCpuLoad15Min(18.2);
            server.setMemoryTotal(8192L);
            server.setMemoryUsed(3072L);
            server.setMemoryUsagePercent(37.5);
            server.setDiskTotal(500L);
            server.setDiskUsed(125L);
            server.setDiskUsagePercent(25.0);
            server.setLastCheck(LocalDateTime.now());
        }
        
        return CompletableFuture.completedFuture(server);
    }

    public List<ServerStatusDto> collectAllServerMetrics() {
        log.info("Mock: Coletando métricas para todos os servidores");
        
        List<ServerStatusDto> results = new ArrayList<>();
        
        for (ServerStatusDto server : mockServers) {
            // Simula diferentes status
            if (server.getServerId() == 1L) {
                // Server Web - Online
                server.setOnline(true);
                server.setStatus("ONLINE");
                server.setUptime("25 days, 14 hours");
                server.setCpuLoad1Min(8.2);
                server.setCpuLoad5Min(9.5);
                server.setCpuLoad15Min(11.3);
                server.setMemoryTotal(16384L);
                server.setMemoryUsed(4096L);
                server.setMemoryUsagePercent(25.0);
                server.setDiskTotal(1000L);
                server.setDiskUsed(350L);
                server.setDiskUsagePercent(35.0);                
            } else if (server.getServerId() == 2L) {
                // Server DB - Warning
                server.setOnline(true);
                server.setStatus("WARNING");
                server.setUptime("8 days, 6 hours");
                server.setCpuLoad1Min(45.8);
                server.setCpuLoad5Min(48.2);
                server.setCpuLoad15Min(52.1);
                server.setMemoryTotal(32768L);
                server.setMemoryUsed(26214L);
                server.setMemoryUsagePercent(80.0);
                server.setDiskTotal(2000L);
                server.setDiskUsed(1600L);
                server.setDiskUsagePercent(80.0);                
            } else if (server.getServerId() == 3L) {
                // Server App - Offline
                server.setOnline(false);
                server.setStatus("OFFLINE");
                server.setErrorMessage("Timeout na conexão SNMP");
            }
            
            server.setLastCheck(LocalDateTime.now());
            results.add(server);
        }
        
        log.info("Mock: Métricas coletadas para {} servidores", results.size());
        return results;
    }
}