package com.victorqueiroga.serverwatch.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.victorqueiroga.serverwatch.dto.ServerStatusDto;
import com.victorqueiroga.serverwatch.model.Server;
import com.victorqueiroga.serverwatch.utils.SnmpHelper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço responsável pelo monitoramento SNMP dos servidores
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServerMonitoringService {

    private final ServerService serverService;
    
    // Cache dos status dos servidores
    private final ConcurrentHashMap<Long, ServerStatusDto> serverStatusCache = new ConcurrentHashMap<>();
    
    // Executor para operações SNMP assíncronas
    private final Executor snmpExecutor = Executors.newFixedThreadPool(10);
    
    // Configurações SNMP padrão
    private static final String DEFAULT_COMMUNITY = "public";
    private static final int SNMP_TIMEOUT = 5000; // 5 segundos
    

    
    /**
     * Obtém o status atual de todos os servidores monitorados
     */
    public List<ServerStatusDto> getAllServerStatus() {
        log.debug("Obtendo status de todos os servidores do cache");
        
        // Se o cache estiver vazio, popula com dados dos servidores cadastrados
        if (serverStatusCache.isEmpty()) {
            log.info("Cache vazio, populando com servidores cadastrados");
            List<Server> servers = serverService.findAll();
            
            for (Server server : servers) {
                try {
                    ServerStatusDto status = collectServerMetrics(server);
                    serverStatusCache.put(server.getId(), status);
                } catch (Exception e) {
                    log.error("Erro ao coletar métricas do servidor {}: {}", server.getName(), e.getMessage());
                    ServerStatusDto errorStatus = ServerStatusDto.fromServer(server);
                    errorStatus.markAsOffline("Erro na coleta: " + e.getMessage());
                    serverStatusCache.put(server.getId(), errorStatus);
                }
            }
        }
        
        return serverStatusCache.values().stream()
                .sorted((s1, s2) -> s1.getServerName().compareToIgnoreCase(s2.getServerName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtém o status de um servidor específico
     */
    public ServerStatusDto getServerStatus(Long serverId) {
        log.debug("Obtendo status do servidor ID: {}", serverId);
        return serverStatusCache.get(serverId);
    }
    
    /**
     * Força a atualização do status de um servidor específico
     */
    @Async
    public CompletableFuture<ServerStatusDto> refreshServerStatus(Long serverId) {
        log.debug("Forçando atualização do status do servidor ID: {}", serverId);
        
        Server server = serverService.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor não encontrado: " + serverId));
        
        ServerStatusDto status = collectServerMetrics(server);
        serverStatusCache.put(serverId, status);
        
        return CompletableFuture.completedFuture(status);
    }
    
    /**
     * Atualização programada dos status dos servidores (a cada 2 minutos)
     */
    @Scheduled(fixedRate = 120000) // 2 minutos
    public void scheduledMonitoring() {
        log.info("Iniciando monitoramento programado dos servidores");
        
        List<Server> servers = serverService.findAll();
        log.info("Monitorando {} servidores", servers.size());
        
        // Processa servidores em paralelo
        List<CompletableFuture<Void>> futures = servers.stream()
                .map(server -> CompletableFuture.runAsync(() -> {
                    try {
                        ServerStatusDto status = collectServerMetrics(server);
                        serverStatusCache.put(server.getId(), status);
                        log.debug("Status atualizado para servidor: {} - {}", 
                                server.getName(), status.getStatus());
                    } catch (Exception e) {
                        log.error("Erro ao monitorar servidor {}: {}", 
                                server.getName(), e.getMessage());
                        
                        ServerStatusDto errorStatus = ServerStatusDto.fromServer(server);
                        errorStatus.markAsOffline("Erro no monitoramento: " + e.getMessage());
                        serverStatusCache.put(server.getId(), errorStatus);
                    }
                }, snmpExecutor))
                .collect(Collectors.toList());
        
        // Aguarda todas as operações terminarem
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Erro durante monitoramento programado", throwable);
                    } else {
                        log.info("Monitoramento programado concluído com sucesso");
                    }
                });
    }
    
    /**
     * Coleta métricas SNMP de um servidor
     */
    private ServerStatusDto collectServerMetrics(Server server) {
        log.debug("Coletando métricas SNMP do servidor: {}", server.getName());
        
        ServerStatusDto status = ServerStatusDto.fromServer(server);
        SnmpHelper snmp = new SnmpHelper(server.getIpAddress(), DEFAULT_COMMUNITY);
        
        try {
            // Testa conectividade básica primeiro
            String sysDescr = snmp.getSystemDescription();
            if (sysDescr == null || sysDescr.trim().isEmpty()) {
                status.markAsOffline("Sem resposta SNMP");
                return status;
            }
            
            status.markAsOnline();
            
            // Coleta informações básicas do sistema
            collectSystemInfo(snmp, status);
            
            // Coleta métricas de CPU
            collectCpuMetrics(snmp, status);
            
            // Coleta métricas de memória
            collectMemoryMetrics(snmp, status);
            
            // Coleta métricas de disco
            collectDiskMetrics(snmp, status);
            
            // Coleta informações de rede
            collectNetworkMetrics(snmp, status);
            
            // Calcula percentuais
            status.calculateMemoryUsage();
            status.calculateDiskUsage();
            
            // Determina status final
            status.determineStatus();
            
            log.debug("Métricas coletadas com sucesso para {}: {}", 
                    server.getName(), status.getStatus());
            
        } catch (Exception e) {
            log.warn("Erro ao coletar métricas SNMP de {}: {}", 
                    server.getName(), e.getMessage());
            status.markAsOffline("Erro SNMP: " + e.getMessage());
        }
        
        return status;
    }
    
    /**
     * Coleta informações básicas do sistema
     */
    private void collectSystemInfo(SnmpHelper snmp, ServerStatusDto status) {
        try {
            status.setSystemDescription(snmp.getSystemDescription());
            status.setHostname(snmp.getHostname());
            
            // Converte uptime de centésimos de segundo para formato legível
            String uptimeStr = snmp.getUptime();
            if (uptimeStr != null && !uptimeStr.isEmpty()) {
                long uptimeCentiseconds = Long.parseLong(uptimeStr);
                long uptimeSeconds = uptimeCentiseconds / 100;
                status.setUptime(formatUptime(uptimeSeconds));
            }
        } catch (Exception e) {
            log.debug("Erro ao coletar info do sistema: {}", e.getMessage());
        }
    }
    
    /**
     * Coleta métricas de CPU
     */
    private void collectCpuMetrics(SnmpHelper snmp, ServerStatusDto status) {
        try {
            String load1 = snmp.getCpuLoad1Min();
            if (load1 != null && !load1.isEmpty()) {
                // Load average pode vir como string "0.50" ou como inteiro
                status.setCpuLoad1Min(parseDoubleValue(load1));
            }
        } catch (Exception e) {
            log.debug("Erro ao coletar métricas de CPU: {}", e.getMessage());
        }
    }
    
    /**
     * Coleta métricas de memória
     */
    private void collectMemoryMetrics(SnmpHelper snmp, ServerStatusDto status) {
        try {
            String memTotal = snmp.getMemoryTotal();
            String memUsed = snmp.getMemoryUsed();
            String memAvailable = snmp.getMemoryAvailable();
            
            if (memTotal != null && !memTotal.isEmpty()) {
                // Converte de KB para MB
                status.setMemoryTotal(Long.parseLong(memTotal) / 1024);
            }
            
            if (memUsed != null && !memUsed.isEmpty()) {
                status.setMemoryUsed(Long.parseLong(memUsed) / 1024);
            }
            
            if (memAvailable != null && !memAvailable.isEmpty()) {
                status.setMemoryAvailable(Long.parseLong(memAvailable) / 1024);
            }
        } catch (Exception e) {
            log.debug("Erro ao coletar métricas de memória: {}", e.getMessage());
        }
    }
    
    /**
     * Coleta métricas de disco (primeira partição)
     */
    private void collectDiskMetrics(SnmpHelper snmp, ServerStatusDto status) {
        try {
            // Para o primeiro disco (índice 1)
            String diskTotal = snmp.getAsString(SnmpHelper.OID_DISK_TOTAL + ".1");
            String diskUsed = snmp.getAsString(SnmpHelper.OID_DISK_USED + ".1");
            String diskAvail = snmp.getAsString(SnmpHelper.OID_DISK_AVAIL + ".1");
            
            if (diskTotal != null && !diskTotal.isEmpty()) {
                // Converte de KB para GB
                status.setDiskTotal(Long.parseLong(diskTotal) / (1024 * 1024));
            }
            
            if (diskUsed != null && !diskUsed.isEmpty()) {
                status.setDiskUsed(Long.parseLong(diskUsed) / (1024 * 1024));
            }
            
            if (diskAvail != null && !diskAvail.isEmpty()) {
                status.setDiskAvailable(Long.parseLong(diskAvail) / (1024 * 1024));
            }
        } catch (Exception e) {
            log.debug("Erro ao coletar métricas de disco: {}", e.getMessage());
        }
    }
    
    /**
     * Coleta informações de rede
     */
    private void collectNetworkMetrics(SnmpHelper snmp, ServerStatusDto status) {
        try {
            String ifCount = snmp.getInterfaceCount();
            if (ifCount != null && !ifCount.isEmpty()) {
                status.setInterfaceCount(Integer.parseInt(ifCount));
            }
        } catch (Exception e) {
            log.debug("Erro ao coletar métricas de rede: {}", e.getMessage());
        }
    }
    
    /**
     * Converte string para double, tratando diferentes formatos
     */
    private Double parseDoubleValue(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // Tenta remover caracteres não numéricos
            String cleanValue = value.replaceAll("[^0-9.]", "");
            try {
                return Double.parseDouble(cleanValue);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }
    
    /**
     * Formata uptime em formato legível
     */
    private String formatUptime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        
        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        } else {
            return String.format("%dm", minutes);
        }
    }
    
    /**
     * Inicialização do cache com servidores existentes
     */
    @PostConstruct
    public void initializeCache() {
        log.info("Inicializando cache de status dos servidores na inicialização da aplicação");
        
        List<Server> servers = serverService.findAll();
        for (Server server : servers) {
            try {
                // Coleta as métricas reais na inicialização
                ServerStatusDto status = collectServerMetrics(server);
                serverStatusCache.put(server.getId(), status);
                log.debug("Servidor {} inicializado com status: {}", server.getName(), status.getStatus());
            } catch (Exception e) {
                log.error("Erro ao inicializar servidor {}: {}", server.getName(), e.getMessage());
                // Em caso de erro, adiciona com status offline
                ServerStatusDto errorStatus = ServerStatusDto.fromServer(server);
                errorStatus.markAsOffline("Erro na inicialização: " + e.getMessage());
                serverStatusCache.put(server.getId(), errorStatus);
            }
        }
        
        log.info("Cache inicializado com {} servidores", servers.size());
    }
}