package com.victorqueiroga.serverwatch.controller.api;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.victorqueiroga.serverwatch.dto.ServerStatusDto;
import com.victorqueiroga.serverwatch.service.MockServerMonitoringService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock API para desenvolvimento/testes
 */
@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@Profile("dev")
public class MockMonitoringApiController {

    private final MockServerMonitoringService mockMonitoringService;

    @GetMapping("/servers")
    public ResponseEntity<List<ServerStatusDto>> getAllServerStatus() {
        log.info("Mock API: Solicitando status de todos os servidores");
        
        List<ServerStatusDto> serverStatuses = mockMonitoringService.getAllServerStatus();
        
        log.info("Mock API: Retornando status de {} servidores", serverStatuses.size());
        return ResponseEntity.ok(serverStatuses);
    }

    @GetMapping("/servers/{serverId}")
    public ResponseEntity<ServerStatusDto> getServerStatus(@PathVariable Long serverId) {
        log.info("Mock API: Solicitando status do servidor ID: {}", serverId);
        
        ServerStatusDto status = mockMonitoringService.getServerStatus(serverId);
        
        if (status == null) {
            log.warn("Mock API: Servidor não encontrado: {}", serverId);
            return ResponseEntity.notFound().build();
        }
        
        log.info("Mock API: Retornando status do servidor: {} - {}", 
                status.getServerName(), status.getStatus());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/servers/collect-metrics")
    public ResponseEntity<List<ServerStatusDto>> collectAllServerMetrics() {
        log.info("Mock API: Iniciando coleta sob demanda de métricas para todos os servidores");
        
        try {
            // Simula um delay para testar o loading
            Thread.sleep(2000);
            
            List<ServerStatusDto> serverStatuses = mockMonitoringService.collectAllServerMetrics();
            
            log.info("Mock API: Métricas coletadas com sucesso para {} servidores", serverStatuses.size());
            return ResponseEntity.ok(serverStatuses);
            
        } catch (Exception e) {
            log.error("Mock API: Erro ao coletar métricas dos servidores: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}