package com.victorqueiroga.serverwatch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
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
@Profile("!dev")  // Exclui do profile dev
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
     * Limpa o cache forçando nova coleta SNMP na próxima consulta
     */
    public void clearCache() {
        log.info("Limpando cache de servidores - próximas consultas farão coleta SNMP fresca");
        serverStatusCache.clear();
    }
    
    /**
     * Força atualização de todos os servidores limpando cache primeiro
     */
    public List<ServerStatusDto> forceRefreshAllServers() {
        log.info("FORÇANDO refresh completo - limpando cache e coletando SNMP");
        clearCache();
        return getAllServerStatus();
    }
    
    /**
     * Obtém a lista de todos os servidores com dados atuais
     * FORÇA coleta via SNMP para garantir dados reais (não usa cache antigo)
     */
    public List<ServerStatusDto> getAllServerStatus() {
        log.info("FORÇANDO coleta SNMP para todos os servidores (dados reais)");
        
        List<Server> servers = serverService.findAll();
        List<ServerStatusDto> results = new ArrayList<>();
        
        // Coleta SNMP em paralelo para TODOS os servidores
        List<CompletableFuture<ServerStatusDto>> futures = servers.stream()
                .map(server -> CompletableFuture.supplyAsync(() -> {
                    try {
                        log.info("=== Coletando SNMP REAL para: {} [{}] ===", 
                                server.getName(), server.getIpAddress());
                        
                        // SEMPRE coleta dados frescos via SNMP
                        ServerStatusDto freshStatus = collectServerMetrics(server);
                        
                        // Atualiza cache com dados reais
                        serverStatusCache.put(server.getId(), freshStatus);
                        
                        log.info("SNMP coletado com sucesso para {}: CPU={}, Mem={}MB, Disk={}GB", 
                                server.getName(), 
                                freshStatus.getCpuLoad1Min(),
                                freshStatus.getMemoryTotal(),
                                freshStatus.getDiskTotal());
                        
                        return freshStatus;
                        
                    } catch (Exception e) {
                        log.error("ERRO na coleta SNMP REAL de {} [{}]: {}", 
                                server.getName(), server.getIpAddress(), e.getMessage());
                        
                        // Cria status de erro com detalhes
                        ServerStatusDto errorStatus = ServerStatusDto.fromServer(server);
                        errorStatus.markAsOffline("Erro SNMP: " + e.getMessage());
                        
                        // Salva erro no cache
                        serverStatusCache.put(server.getId(), errorStatus);
                        
                        return errorStatus;
                    }
                }, snmpExecutor))
                .collect(Collectors.toList());
        
        // Aguarda TODAS as coletas terminarem
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();
        
        // Coleta os resultados
        for (CompletableFuture<ServerStatusDto> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                log.error("Erro ao obter resultado da coleta SNMP", e);
            }
        }
        
        log.info("=== COLETA SNMP CONCLUÍDA: {} servidores processados ===", results.size());
        
        return results.stream()
                .sorted((s1, s2) -> s1.getServerName().compareToIgnoreCase(s2.getServerName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtém o status de um servidor específico
     * Se não há dados no cache ou estão desatualizados, coleta via SNMP
     */
    public ServerStatusDto getServerStatus(Long serverId) {
        log.debug("Obtendo status do servidor ID: {}", serverId);
        
        // Verifica se há dados no cache
        ServerStatusDto cachedStatus = serverStatusCache.get(serverId);
        
        // Se não há cache ou dados estão muito antigos, força nova coleta
        if (cachedStatus == null || isStatusExpired(cachedStatus)) {
            log.debug("Cache expirado ou inexistente para servidor {}, coletando via SNMP", serverId);
            
            // Busca servidor no banco
            Server server = serverService.findById(serverId)
                    .orElseThrow(() -> new RuntimeException("Servidor não encontrado: " + serverId));
            
            // Coleta métricas SNMP em tempo real
            ServerStatusDto freshStatus = collectServerMetrics(server);
            
            // Atualiza cache
            serverStatusCache.put(serverId, freshStatus);
            
            return freshStatus;
        }
        
        return cachedStatus;
    }
    
    /**
     * Verifica se o status no cache está expirado (mais de 5 minutos)
     */
    private boolean isStatusExpired(ServerStatusDto status) {
        if (status.getLastUpdate() == null) {
            return true;
        }
        
        long ageInMinutes = (System.currentTimeMillis() - status.getLastUpdate().getTime()) / (60 * 1000);
        return ageInMinutes > 5; // Considera expirado após 5 minutos (mais tolerante)
    }
    
    /**
     * Força a atualização do status de um servidor específico (ASYNC)
     */
    @Async
    public CompletableFuture<ServerStatusDto> refreshServerStatus(Long serverId) {
        log.debug("Forçando atualização ASYNC do status do servidor ID: {}", serverId);
        
        Server server = serverService.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor não encontrado: " + serverId));
        
        ServerStatusDto status = collectServerMetrics(server);
        serverStatusCache.put(serverId, status);
        
        return CompletableFuture.completedFuture(status);
    }
    
    /**
     * Força a atualização IMEDIATA do status de um servidor específico (SINCRONO)
     * Método para uso direto quando necessário dados em tempo real
     */
    public ServerStatusDto forceRefreshServerStatus(Long serverId) {
        log.info("Forçando atualização IMEDIATA do status do servidor ID: {}", serverId);
        
        Server server = serverService.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Servidor não encontrado: " + serverId));
        
        ServerStatusDto status = collectServerMetrics(server);
        serverStatusCache.put(serverId, status);
        
        log.info("Atualização imediata concluída para servidor {}: {}", 
                server.getName(), status.getStatus());
        
        return status;
    }
    
    /**
     * Coleta métricas sob demanda para todos os servidores (usado pela API)
     */
    public List<ServerStatusDto> collectAllServerMetrics() {
        log.info("Coletando métricas sob demanda para todos os servidores");
        
        List<Server> servers = serverService.findAll();
        List<ServerStatusDto> results = new ArrayList<>();
        
        // Executa em paralelo para melhor performance
        List<CompletableFuture<ServerStatusDto>> futures = servers.stream()
                .map(server -> CompletableFuture.supplyAsync(() -> {
                    try {
                        ServerStatusDto status = collectServerMetrics(server);
                        serverStatusCache.put(server.getId(), status);
                        return status;
                    } catch (Exception e) {
                        log.error("Erro ao coletar métricas do servidor {}: {}", server.getName(), e.getMessage());
                        ServerStatusDto errorStatus = ServerStatusDto.fromServer(server);
                        errorStatus.markAsOffline("Erro na coleta: " + e.getMessage());
                        serverStatusCache.put(server.getId(), errorStatus);
                        return errorStatus;
                    }
                }, snmpExecutor))
                .collect(Collectors.toList());
        
        // Aguarda todas as coletas terminarem
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .join();
        
        // Coleta os resultados
        for (CompletableFuture<ServerStatusDto> future : futures) {
            try {
                results.add(future.get());
            } catch (Exception e) {
                log.error("Erro ao obter resultado da coleta", e);
            }
        }
        
        return results.stream()
                .sorted((s1, s2) -> s1.getServerName().compareToIgnoreCase(s2.getServerName()))
                .collect(Collectors.toList());
    }
    
    /**
     * Atualização programada dos status dos servidores (a cada 2 minutos)
     * Mantém funcionando para gerar alertas automáticos
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
        log.info("=== Iniciando coleta SNMP para servidor: {} [{}] ===", 
                server.getName(), server.getIpAddress());
        
        ServerStatusDto status = ServerStatusDto.fromServer(server);
        SnmpHelper snmp = new SnmpHelper(server.getIpAddress(), DEFAULT_COMMUNITY);
        
        try {
            // Testa conectividade básica primeiro
            log.debug("Testando conectividade SNMP básica...");
            String sysDescr = snmp.getSystemDescription();
            
            if (sysDescr == null || sysDescr.trim().isEmpty()) {
                log.warn("Servidor {} não respondeu ao SNMP ou resposta vazia", server.getName());
                status.markAsOffline("Sem resposta SNMP");
                return status;
            }
            
            log.info("Conectividade SNMP OK para {}: {}", server.getName(), sysDescr);
            status.markAsOnline();
            
            // Coleta informações básicas do sistema
            collectSystemInfo(snmp, status);
            
            // Coleta métricas de CPU
            collectCpuMetrics(snmp, status);
            
            // Coleta métricas de memória
            collectMemoryMetrics(snmp, status);
            
            // Coleta métricas de disco
            collectDiskMetrics(snmp, status);
            
            // Calcula percentuais
            status.calculateMemoryUsage();
            status.calculateDiskUsage();
            
            // Determina status final
            status.determineStatus();
            
            log.info("=== Coleta SNMP concluída para {}: Status={}, CPU={}, Mem={}%, Disk={}% ===", 
                    server.getName(), status.getStatus(), status.getCpuLoad1Min(),
                    status.getMemoryUsagePercent(), status.getDiskUsagePercent());
            
        } catch (Exception e) {
            log.error("ERRO na coleta SNMP de {} [{}]: {} - Tipo: {}", 
                    server.getName(), server.getIpAddress(), e.getMessage(), e.getClass().getSimpleName());
            log.debug("Stack trace completo do erro SNMP:", e);
            status.markAsOffline("Erro SNMP: " + e.getMessage());
        }
        
        return status;
    }
    
    /**
     * Coleta informações básicas do sistema
     */
    private void collectSystemInfo(SnmpHelper snmp, ServerStatusDto status) {
        try {
            log.debug("Coletando informações básicas do sistema...");
            
            String sysDesc = snmp.getSystemDescription();
            status.setSystemDescription(sysDesc);
            log.debug("System Description: {}", sysDesc);
            
            String hostname = snmp.getHostname();
            status.setHostname(hostname);
            log.debug("Hostname: {}", hostname);
            
            // Converte uptime de centésimos de segundo para formato legível
            String uptimeStr = snmp.getUptime();
            if (uptimeStr != null && !uptimeStr.isEmpty()) {
                long uptimeCentiseconds = Long.parseLong(uptimeStr);
                long uptimeSeconds = uptimeCentiseconds / 100;
                String formattedUptime = formatUptime(uptimeSeconds);
                status.setUptime(formattedUptime);
                log.debug("Uptime: {} segundos = {}", uptimeSeconds, formattedUptime);
            }
        } catch (Exception e) {
            log.warn("Erro ao coletar info do sistema: {}", e.getMessage());
        }
    }
    
    /**
     * Coleta métricas de CPU (usando método inteligente do SnmpHelper)
     */
    private void collectCpuMetrics(SnmpHelper snmp, ServerStatusDto status) {
        log.debug("=== Coletando métricas de CPU (método inteligente por SO) ===");
        
        try {
            String cpuValue = snmp.getCpuLoad1Min();
            if (cpuValue != null && !cpuValue.trim().isEmpty()) {
                Double cpuLoad = parseDoubleValue(cpuValue);
                if (cpuLoad != null) {
                    status.setCpuLoad1Min(cpuLoad);
                    log.info("✅ CPU coletada com método inteligente! Valor: {} -> {}", cpuValue, cpuLoad);
                } else {
                    log.warn("⚠️ CPU valor não numérico: {}", cpuValue);
                }
            } else {
                log.warn("⚠️ CPU Load não disponível via método inteligente");
            }
        } catch (Exception e) {
            log.warn("❌ Erro ao coletar CPU via método inteligente: {}", e.getMessage());
            
            // Fallback para método manual
            log.debug("Tentando fallback manual para CPU...");
            String[] cpuOids = {
                SnmpHelper.OID_HR_PROCESSOR_LOAD + ".1", // Host Resources
                SnmpHelper.OID_CPU_LOAD_1MIN,            // Net-SNMP Linux
                "1.3.6.1.4.1.2021.11.10.0"             // Net-SNMP CPU user
            };
            
            for (String oid : cpuOids) {
                try {
                    String value = snmp.getAsString(oid);
                    if (value != null && !value.contains("noSuch")) {
                        Double cpuLoad = parseDoubleValue(value);
                        if (cpuLoad != null) {
                            status.setCpuLoad1Min(cpuLoad);
                            log.info("✅ CPU coletada com fallback! OID: {} = {}", oid, cpuLoad);
                            return;
                        }
                    }
                } catch (Exception ex) {
                    log.debug("❌ Fallback OID {} falhou: {}", oid, ex.getMessage());
                }
            }
            
            log.warn("⚠️ CPU: Todos os métodos falharam");
        }
    }
    
    /**
     * Coleta métricas de memória (usando métodos inteligentes do SnmpHelper)
     */
    private void collectMemoryMetrics(SnmpHelper snmp, ServerStatusDto status) {
        log.debug("=== Coletando métricas de MEMÓRIA (métodos inteligentes por SO) ===");
        
        try {
            // Memória Total
            String memTotalValue = snmp.getMemoryTotal();
            if (memTotalValue != null && !memTotalValue.trim().isEmpty()) {
                Long totalKB = Long.parseLong(memTotalValue.trim());
                Long totalMB = totalKB / 1024;
                status.setMemoryTotal(totalMB);
                log.info("✅ Memória TOTAL coletada via método inteligente! {} KB = {} MB", totalKB, totalMB);
            } else {
                log.warn("⚠️ Memória TOTAL não disponível via método inteligente");
            }
            
            // Memória Usada
            String memUsedValue = snmp.getMemoryUsed();
            if (memUsedValue != null && !memUsedValue.trim().isEmpty()) {
                Long usedKB = Long.parseLong(memUsedValue.trim());
                Long usedMB = usedKB / 1024;
                status.setMemoryUsed(usedMB);
                log.info("✅ Memória USADA coletada via método inteligente! {} KB = {} MB", usedKB, usedMB);
            } else {
                log.warn("⚠️ Memória USADA não disponível via método inteligente");
            }
            
            // Memória Disponível
            String memAvailValue = snmp.getMemoryAvailable();
            if (memAvailValue != null && !memAvailValue.trim().isEmpty()) {
                Long availKB = Long.parseLong(memAvailValue.trim());
                Long availMB = availKB / 1024;
                status.setMemoryAvailable(availMB);
                log.info("✅ Memória DISPONÍVEL coletada via método inteligente! {} KB = {} MB", availKB, availMB);
            } else {
                log.warn("⚠️ Memória DISPONÍVEL não disponível via método inteligente");
            }
            
        } catch (Exception e) {
            log.error("❌ Erro ao coletar memória via métodos inteligentes: {}", e.getMessage());
            
            // Fallback para coleta manual
            log.debug("Tentando fallback manual para memória...");
            try {
                String totalValue = snmp.getAsString(SnmpHelper.OID_MEM_TOTAL_REAL);
                if (totalValue != null && !totalValue.contains("noSuch")) {
                    Long totalKB = Long.parseLong(totalValue.trim());
                    status.setMemoryTotal(totalKB / 1024);
                    log.info("✅ Memória TOTAL via fallback: {} KB", totalKB);
                }
            } catch (Exception ex) {
                log.debug("❌ Fallback memória total falhou: {}", ex.getMessage());
            }
        }
        
        if (status.getMemoryTotal() == null || status.getMemoryTotal() == 0) {
            log.warn("⚠️ MEMÓRIA: Todos os métodos falharam");
        }
    }
    
    /**
     * Coleta métricas de disco (múltiplos discos usando métodos inteligentes)
     */
    private void collectDiskMetrics(SnmpHelper snmp, ServerStatusDto status) {
        log.debug("=== Coletando múltiplos DISCOS (métodos inteligentes por SO) ===");
        
        try {
            // Coleta TODOS os discos disponíveis
            List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> diskList = snmp.getAllDisks();
            
            if (diskList != null && !diskList.isEmpty()) {
                status.setDiskList(diskList);
                log.info("✅ {} discos coletados com sucesso!", diskList.size());
                
                // Log de cada disco encontrado
                for (com.victorqueiroga.serverwatch.dto.DiskInfoDto disk : diskList) {
                    log.info("  💾 Disco {}: {} GB total, {} GB usado, {} GB disponível ({}%)", 
                            disk.getPath(), disk.getTotalGB(), disk.getUsedGB(), 
                            disk.getAvailableGB(), String.format("%.1f", disk.getUsagePercent()));
                }
                
                // Mantém compatibilidade: define o primeiro disco como disco principal
                if (!diskList.isEmpty()) {
                    com.victorqueiroga.serverwatch.dto.DiskInfoDto primaryDisk = diskList.get(0);
                    status.setDiskTotal(primaryDisk.getTotalGB());
                    status.setDiskUsed(primaryDisk.getUsedGB());
                    status.setDiskAvailable(primaryDisk.getAvailableGB());
                    log.debug("Disco principal definido: {} com {} GB total", 
                            primaryDisk.getPath(), primaryDisk.getTotalGB());
                }
                
            } else {
                log.warn("⚠️ Nenhum disco encontrado via método inteligente");
                
                // Fallback para método antigo (disco único)
                collectSingleDisk(snmp, status);
            }
            
        } catch (Exception e) {
            log.error("❌ Erro ao coletar discos via método inteligente: {}", e.getMessage());
            
            // Fallback para método antigo
            collectSingleDisk(snmp, status);
        }
    }
    
    /**
     * Fallback para coleta de disco único (método antigo)
     */
    private void collectSingleDisk(SnmpHelper snmp, ServerStatusDto status) {
        log.debug("Usando fallback para disco único...");
        
        try {
            // Disco Total
            String diskTotalValue = snmp.getDiskTotal();
            if (diskTotalValue != null && !diskTotalValue.trim().isEmpty()) {
                Long totalKB = Long.parseLong(diskTotalValue.trim());
                Long totalGB = totalKB / (1024 * 1024);
                status.setDiskTotal(totalGB);
                log.info("✅ Disco TOTAL (fallback): {} GB", totalGB);
            }
        } catch (Exception e) {
            log.debug("❌ Fallback disco único falhou: {}", e.getMessage());
        }
        
        if (status.getDiskTotal() == null || status.getDiskTotal() == 0) {
            log.warn("⚠️ DISCO: Todos os métodos falharam");
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
     * Testa conectividade SNMP e quais OIDs funcionam em um servidor específico
     */
    public String testServerSnmp(String serverIp) {
        StringBuilder result = new StringBuilder();
        result.append("=== TESTE SNMP DETALHADO PARA: ").append(serverIp).append(" ===\n\n");
        
        SnmpHelper snmp = new SnmpHelper(serverIp, DEFAULT_COMMUNITY);
        
        // Testa OIDs básicos
        String[] basicOids = {
            SnmpHelper.OID_SYS_DESCR + "|System Description",
            SnmpHelper.OID_SYS_UPTIME + "|System Uptime", 
            SnmpHelper.OID_HOSTNAME + "|Hostname"
        };
        
        result.append("📋 INFORMAÇÕES BÁSICAS:\n");
        for (String oidInfo : basicOids) {
            String[] parts = oidInfo.split("\\|");
            String oid = parts[0];
            String name = parts[1];
            
            try {
                String value = snmp.getAsString(oid);
                if (value != null && !value.contains("noSuch")) {
                    result.append("✅ ").append(name).append(": ").append(value).append("\n");
                } else {
                    result.append("❌ ").append(name).append(": OID não suportado\n");
                }
            } catch (Exception e) {
                result.append("❌ ").append(name).append(": ERRO - ").append(e.getMessage()).append("\n");
            }
        }
        
        // Testa OIDs de CPU
        result.append("\n🖥️ MÉTRICAS DE CPU:\n");
        String[] cpuOids = {
            SnmpHelper.OID_CPU_LOAD_1MIN + "|CPU Load 1min (Net-SNMP)",
            SnmpHelper.OID_HR_PROCESSOR_LOAD + ".1|CPU Load (Host Resources)",
            "1.3.6.1.4.1.2021.11.9.0|CPU Idle %",
            "1.3.6.1.4.1.2021.11.10.0|CPU User %", 
            "1.3.6.1.4.1.2021.11.11.0|CPU System %"
        };
        
        for (String oidInfo : cpuOids) {
            String[] parts = oidInfo.split("\\|");
            String oid = parts[0];
            String name = parts[1];
            
            try {
                String value = snmp.getAsString(oid);
                if (value != null && !value.contains("noSuch")) {
                    result.append("✅ ").append(name).append(": ").append(value).append("\n");
                } else {
                    result.append("❌ ").append(name).append(": OID não suportado\n");
                }
            } catch (Exception e) {
                result.append("❌ ").append(name).append(": ERRO - ").append(e.getMessage()).append("\n");
            }
        }
        
        // Testa OIDs de Memória
        result.append("\n💾 MÉTRICAS DE MEMÓRIA:\n");
        String[] memOids = {
            SnmpHelper.OID_MEM_TOTAL_REAL + "|Memória Total (KB)",
            SnmpHelper.OID_MEM_USED_REAL + "|Memória Usada (KB)",
            SnmpHelper.OID_MEM_FREE_REAL + "|Memória Livre (KB)",
            SnmpHelper.OID_MEM_AVAIL_REAL + "|Memória Disponível (KB)"
        };
        
        for (String oidInfo : memOids) {
            String[] parts = oidInfo.split("\\|");
            String oid = parts[0];
            String name = parts[1];
            
            try {
                String value = snmp.getAsString(oid);
                if (value != null && !value.contains("noSuch")) {
                    result.append("✅ ").append(name).append(": ").append(value).append(" KB\n");
                } else {
                    result.append("❌ ").append(name).append(": OID não suportado\n");
                }
            } catch (Exception e) {
                result.append("❌ ").append(name).append(": ERRO - ").append(e.getMessage()).append("\n");
            }
        }
        
        // Testa OIDs de Disco
        result.append("\n💿 MÉTRICAS DE DISCO:\n");
        for (int i = 0; i <= 3; i++) {
            result.append("  --- Disco Índice ").append(i).append(" ---\n");
            
            try {
                String path = snmp.getAsString(SnmpHelper.OID_DISK_PATH + "." + i);
                String total = snmp.getAsString(SnmpHelper.OID_DISK_TOTAL + "." + i);
                String used = snmp.getAsString(SnmpHelper.OID_DISK_USED + "." + i);
                String avail = snmp.getAsString(SnmpHelper.OID_DISK_AVAIL + "." + i);
                
                if (path != null && !path.contains("noSuch")) {
                    result.append("  ✅ Path: ").append(path).append("\n");
                    
                    if (total != null && !total.contains("noSuch")) {
                        result.append("  ✅ Total: ").append(total).append(" KB\n");
                    }
                    if (used != null && !used.contains("noSuch")) {
                        result.append("  ✅ Usado: ").append(used).append(" KB\n");
                    }
                    if (avail != null && !avail.contains("noSuch")) {
                        result.append("  ✅ Disponível: ").append(avail).append(" KB\n");
                    }
                } else {
                    result.append("  ❌ Índice ").append(i).append(" não existe\n");
                }
            } catch (Exception e) {
                result.append("  ❌ Erro no índice ").append(i).append(": ").append(e.getMessage()).append("\n");
            }
        }
        
        return result.toString();
    }

    /**
     * Inicialização do cache com servidores existentes (desabilitado para coleta sob demanda)
     */
    // @PostConstruct - Removido para melhorar performance de inicialização
    public void initializeCache() {
        log.info("Inicialização automática de métricas desabilitada - coleta sob demanda ativada");
        // Métricas agora são coletadas apenas quando solicitadas via API
    }
}