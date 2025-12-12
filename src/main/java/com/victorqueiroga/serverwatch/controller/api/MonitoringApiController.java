package com.victorqueiroga.serverwatch.controller.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
public class MonitoringApiController {

    private final ServerMonitoringService monitoringService;
    private final com.victorqueiroga.serverwatch.service.ServerService serverService;

    /**
     * GET /api/monitoring/servers Obtém o status atual de todos os servidores
     */
    @GetMapping("/servers")
    public ResponseEntity<List<ServerStatusDto>> getAllServerStatus() {
        log.info("API: Solicitando status de todos os servidores");

        List<ServerStatusDto> serverStatuses = monitoringService.getAllServerStatus();

        log.info("API: Retornando status de {} servidores", serverStatuses.size());
        return ResponseEntity.ok(serverStatuses);
    }

    /**
     * GET /api/monitoring/servers/{serverId} Obtém o status de um servidor
     * específico
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
     * POST /api/monitoring/servers/{serverId}/refresh Força a atualização do
     * status de um servidor específico
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
     * POST /api/monitoring/servers/collect-metrics Coleta métricas de todos os
     * servidores sob demanda (para otimizar carregamento da página)
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
     * POST /api/monitoring/refresh Força atualização completa via SNMP limpando
     * cache
     */
    @PostMapping("/refresh")
    public ResponseEntity<List<ServerStatusDto>> forceRefreshServers() {
        log.info("API: FORÇANDO refresh completo de todos os servidores");

        try {
            List<ServerStatusDto> serverStatuses = monitoringService.forceRefreshAllServers();

            log.info("API: Refresh forçado concluído - {} servidores atualizados", serverStatuses.size());
            return ResponseEntity.ok(serverStatuses);

        } catch (Exception e) {
            log.error("API: Erro no refresh forçado: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * DELETE /api/monitoring/cache Limpa o cache de monitoramento
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<String> clearCache() {
        log.info("API: Limpando cache de monitoramento");

        try {
            monitoringService.clearCache();
            return ResponseEntity.ok("Cache limpo com sucesso");

        } catch (Exception e) {
            log.error("API: Erro ao limpar cache: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/monitoring/test/{serverIp} Testa conectividade SNMP e mostra
     * quais OIDs funcionam
     */
    @GetMapping("/test/{serverIp}")
    public ResponseEntity<String> testServerSnmp(@PathVariable String serverIp) {
        log.info("API: Testando conectividade SNMP para: {}", serverIp);

        try {
            String testResult = monitoringService.testServerSnmp(serverIp);
            return ResponseEntity.ok(testResult);

        } catch (Exception e) {
            log.error("API: Erro no teste SNMP: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Erro no teste SNMP: " + e.getMessage());
        }
    }

    /**
     * GET /api/monitoring/test-snmp/{serverId} Testa SNMP de um servidor
     * específico para debug - Retorna JSON
     */
    @GetMapping("/test-snmp/{serverId}")
    public ResponseEntity<?> testServerSnmp(@PathVariable Long serverId) {
        log.info("API: Testando SNMP do servidor ID: {}", serverId);

        try {
            // Busca o servidor
            java.util.Optional<com.victorqueiroga.serverwatch.model.Server> serverOpt = serverService.findById(serverId);
            if (serverOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            com.victorqueiroga.serverwatch.model.Server server = serverOpt.get();
            log.info("Testando SNMP para: {} [{}]", server.getName(), server.getIpAddress());

            // Testa SNMP
            com.victorqueiroga.serverwatch.utils.SnmpHelper snmpHelper
                    = new com.victorqueiroga.serverwatch.utils.SnmpHelper(server.getIpAddress(), "public");

            // Cria objeto JSON com resultados dos testes
            java.util.Map<String, Object> testResult = new java.util.HashMap<>();
            testResult.put("serverName", server.getName());
            testResult.put("ipAddress", server.getIpAddress());
            testResult.put("community", "public");

            // Testa OIDs básicos
            java.util.Map<String, String> basicInfo = new java.util.HashMap<>();
            try {
                basicInfo.put("systemDescription", snmpHelper.getAsString("1.3.6.1.2.1.1.1.0"));
            } catch (Exception e) {
                basicInfo.put("systemDescription", "ERROR: " + e.getMessage());
            }

            try {
                basicInfo.put("hostname", snmpHelper.getAsString("1.3.6.1.2.1.1.5.0"));
            } catch (Exception e) {
                basicInfo.put("hostname", "ERROR: " + e.getMessage());
            }

            try {
                basicInfo.put("uptime", snmpHelper.getAsString("1.3.6.1.2.1.1.3.0"));
            } catch (Exception e) {
                basicInfo.put("uptime", "ERROR: " + e.getMessage());
            }

            testResult.put("basicInfo", basicInfo);

            // Testa CPU
            java.util.Map<String, String> cpuInfo = new java.util.HashMap<>();
            try {
                cpuInfo.put("cpuLoad", snmpHelper.getCpuLoad1Min());
                cpuInfo.put("status", "OK");
            } catch (Exception e) {
                cpuInfo.put("cpuLoad", null);
                cpuInfo.put("status", "ERROR");
                cpuInfo.put("error", e.getMessage());
            }
            testResult.put("cpu", cpuInfo);

            // Testa Memory
            java.util.Map<String, String> memoryInfo = new java.util.HashMap<>();
            try {
                memoryInfo.put("total", snmpHelper.getMemoryTotal() + " KB");
                memoryInfo.put("used", snmpHelper.getMemoryUsed() + " KB");
                memoryInfo.put("available", snmpHelper.getMemoryAvailable() + " KB");
                memoryInfo.put("status", "OK");
            } catch (Exception e) {
                memoryInfo.put("status", "ERROR");
                memoryInfo.put("error", e.getMessage());
            }
            testResult.put("memory", memoryInfo);

            // Testa Disk (todos os discos)
            java.util.Map<String, Object> diskInfo = new java.util.HashMap<>();
            try {
                java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> disks = snmpHelper.getAllDisks();
                diskInfo.put("count", disks.size());
                diskInfo.put("disks", disks);
                diskInfo.put("status", "OK");
            } catch (Exception e) {
                diskInfo.put("status", "ERROR");
                diskInfo.put("error", e.getMessage());
            }
            testResult.put("disk", diskInfo);

            testResult.put("timestamp", java.time.LocalDateTime.now().toString());

            log.info("Teste SNMP concluído para {}", server.getName());
            return ResponseEntity.ok(testResult);

        } catch (Exception e) {
            log.error("Erro no teste SNMP: {}", e.getMessage(), e);
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("error", "Erro no teste SNMP: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * GET /api/monitoring/summary Obtém um resumo do status geral dos
     * servidores
     */
    @GetMapping("/summary")
    public ResponseEntity<MonitoringSummaryDto> getMonitoringSummary() {
        log.debug("API: Solicitando resumo de monitoramento");

        List<ServerStatusDto> allStatuses = monitoringService.getAllServerStatus();

        MonitoringSummaryDto summary = MonitoringSummaryDto.builder()
                .totalServers(allStatuses.size())
                .onlineServers((int) allStatuses.stream().filter(s -> ("ONLINE".equals(s.getStatus()) || "WARNING".equals(s.getStatus()))).count())
                .offlineServers((int) allStatuses.stream().filter(s -> "OFFLINE".equals(s.getStatus())).count())
                .warningServers((int) allStatuses.stream().filter(s -> "WARNING".equals(s.getStatus())).count())
                .unknownServers((int) allStatuses.stream().filter(s -> "UNKNOWN".equals(s.getStatus())).count())
                .build();

        log.debug("API: Resumo - Total: {}, Online: {}, Offline: {}, Warning: {}",
                summary.getTotalServers(), summary.getOnlineServers(),
                summary.getOfflineServers(), summary.getWarningServers());

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/ping")
    public ResponseEntity<Boolean> pingServer(@RequestParam String ipAddress) {
        //teste de rede simples
        try {
            InetAddress address = InetAddress.getByName(ipAddress);
            log.debug("API: Pinging server: {}", address.getHostName());
            try {
                return ResponseEntity.ok(address.isReachable(2000));
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body(false);
            }
        } catch (UnknownHostException e) {
            log.error("API: Erro ao pingar servidor: {}", e.getMessage(), e);
        }
        return ResponseEntity.internalServerError().body(false);
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
        public MonitoringSummaryDto() {
        }

        // Builder
        public static MonitoringSummaryDtoBuilder builder() {
            return new MonitoringSummaryDtoBuilder();
        }

        // Getters e Setters
        public int getTotalServers() {
            return totalServers;
        }

        public void setTotalServers(int totalServers) {
            this.totalServers = totalServers;
        }

        public int getOnlineServers() {
            return onlineServers;
        }

        public void setOnlineServers(int onlineServers) {
            this.onlineServers = onlineServers;
        }

        public int getOfflineServers() {
            return offlineServers;
        }

        public void setOfflineServers(int offlineServers) {
            this.offlineServers = offlineServers;
        }

        public int getWarningServers() {
            return warningServers;
        }

        public void setWarningServers(int warningServers) {
            this.warningServers = warningServers;
        }

        public int getUnknownServers() {
            return unknownServers;
        }

        public void setUnknownServers(int unknownServers) {
            this.unknownServers = unknownServers;
        }

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
