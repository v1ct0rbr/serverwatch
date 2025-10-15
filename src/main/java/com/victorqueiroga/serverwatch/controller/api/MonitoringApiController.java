package com.victorqueiroga.serverwatch.controller.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.victorqueiroga.serverwatch.dto.ServerStatusDto;
import com.victorqueiroga.serverwatch.service.ServerMonitoringService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * API REST para monitoramento de servidores
 */
@Slf4j
@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Profile("!dev")  // Exclui do profile dev
public class MonitoringApiController {

    private final ServerMonitoringService monitoringService;

    /**
     * GET /api/monitoring/servers
     * Obtém o status atual de todos os servidores
     */
    @GetMapping("/servers")
    public ResponseEntity<List<ServerStatusDto>> getAllServerStatus() {
        log.info("API: Solicitando status de todos os servidores");
        
        List<ServerStatusDto> serverStatuses = monitoringService.getAllServerStatus();
        
        log.info("API: Retornando status de {} servidores", serverStatuses.size());
        return ResponseEntity.ok(serverStatuses);
    }

    /**
     * GET /api/monitoring/servers/{serverId}
     * Obtém o status de um servidor específico
     */
    @GetMapping("/servers/{serverId}")
    public ResponseEntity<ServerStatusDto> getServerStatus(@PathVariable Long serverId) {
        log.debug("API: Solicitando status do servidor ID: {}", serverId);
        
        ServerStatusDto status = monitoringService.getServerStatus(serverId);
        
        if (status == null) {
            log.warn("API: Servidor não encontrado: {}", serverId);
            return ResponseEntity.notFound().build();
        }
        
        log.debug("API: Retornando status do servidor: {} - {}", 
                status.getServerName(), status.getStatus());
        return ResponseEntity.ok(status);
    }

    /**
     * POST /api/monitoring/servers/{serverId}/refresh
     * Força a atualização do status de um servidor específico
     */
    @PostMapping("/servers/{serverId}/refresh")
    public ResponseEntity<ServerStatusDto> refreshServerStatus(@PathVariable Long serverId) {
        log.info("API: Forçando atualização do servidor ID: {}", serverId);
        
        try {
            CompletableFuture<ServerStatusDto> future = monitoringService.refreshServerStatus(serverId);
            ServerStatusDto status = future.get(); // Aguarda a conclusão
            
            log.info("API: Status atualizado para servidor: {} - {}", 
                    status.getServerName(), status.getStatus());
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("API: Erro ao atualizar status do servidor {}: {}", serverId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/monitoring/servers/collect-metrics
     * Coleta métricas de todos os servidores sob demanda (para otimizar carregamento da página)
     */
    @PostMapping("/servers/collect-metrics")
    public ResponseEntity<List<ServerStatusDto>> collectAllServerMetrics() {
        log.info("API: Iniciando coleta sob demanda de métricas para todos os servidores");
        
        try {
            List<ServerStatusDto> serverStatuses = monitoringService.collectAllServerMetrics();
            
            log.info("API: Métricas coletadas com sucesso para {} servidores", serverStatuses.size());
            return ResponseEntity.ok(serverStatuses);
            
        } catch (Exception e) {
            log.error("API: Erro ao coletar métricas dos servidores: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/monitoring/summary
     * Obtém um resumo do status geral dos servidores
     */
    @GetMapping("/summary")
    public ResponseEntity<MonitoringSummaryDto> getMonitoringSummary() {
        log.debug("API: Solicitando resumo de monitoramento");
        
        List<ServerStatusDto> allStatuses = monitoringService.getAllServerStatus();
        
        MonitoringSummaryDto summary = MonitoringSummaryDto.builder()
                .totalServers(allStatuses.size())
                .onlineServers((int) allStatuses.stream().filter(s -> "ONLINE".equals(s.getStatus())).count())
                .offlineServers((int) allStatuses.stream().filter(s -> "OFFLINE".equals(s.getStatus())).count())
                .warningServers((int) allStatuses.stream().filter(s -> "WARNING".equals(s.getStatus())).count())
                .unknownServers((int) allStatuses.stream().filter(s -> "UNKNOWN".equals(s.getStatus())).count())
                .build();
        
        log.debug("API: Resumo - Total: {}, Online: {}, Offline: {}, Warning: {}", 
                summary.getTotalServers(), summary.getOnlineServers(), 
                summary.getOfflineServers(), summary.getWarningServers());
        
        return ResponseEntity.ok(summary);
    }

    /**
     * DTO para resumo de monitoramento
     */
    public static class MonitoringSummaryDto {
        private int totalServers;
        private int onlineServers;
        private int offlineServers;
        private int warningServers;
        private int unknownServers;

        // Construtor padrão
        public MonitoringSummaryDto() {}

        // Builder
        public static MonitoringSummaryDtoBuilder builder() {
            return new MonitoringSummaryDtoBuilder();
        }

        // Getters e Setters
        public int getTotalServers() { return totalServers; }
        public void setTotalServers(int totalServers) { this.totalServers = totalServers; }

        public int getOnlineServers() { return onlineServers; }
        public void setOnlineServers(int onlineServers) { this.onlineServers = onlineServers; }

        public int getOfflineServers() { return offlineServers; }
        public void setOfflineServers(int offlineServers) { this.offlineServers = offlineServers; }

        public int getWarningServers() { return warningServers; }
        public void setWarningServers(int warningServers) { this.warningServers = warningServers; }

        public int getUnknownServers() { return unknownServers; }
        public void setUnknownServers(int unknownServers) { this.unknownServers = unknownServers; }

        // Builder class
        public static class MonitoringSummaryDtoBuilder {
            private int totalServers;
            private int onlineServers;
            private int offlineServers;
            private int warningServers;
            private int unknownServers;

            public MonitoringSummaryDtoBuilder totalServers(int totalServers) {
                this.totalServers = totalServers;
                return this;
            }

            public MonitoringSummaryDtoBuilder onlineServers(int onlineServers) {
                this.onlineServers = onlineServers;
                return this;
            }

            public MonitoringSummaryDtoBuilder offlineServers(int offlineServers) {
                this.offlineServers = offlineServers;
                return this;
            }

            public MonitoringSummaryDtoBuilder warningServers(int warningServers) {
                this.warningServers = warningServers;
                return this;
            }

            public MonitoringSummaryDtoBuilder unknownServers(int unknownServers) {
                this.unknownServers = unknownServers;
                return this;
            }

            public MonitoringSummaryDto build() {
                MonitoringSummaryDto dto = new MonitoringSummaryDto();
                dto.totalServers = this.totalServers;
                dto.onlineServers = this.onlineServers;
                dto.offlineServers = this.offlineServers;
                dto.warningServers = this.warningServers;
                dto.unknownServers = this.unknownServers;
                return dto;
            }
        }
    }
}