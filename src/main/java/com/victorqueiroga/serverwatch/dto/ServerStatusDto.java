package com.victorqueiroga.serverwatch.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.victorqueiroga.serverwatch.model.Server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar o status atual de um servidor monitorado
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerStatusDto {
    
    private Long serverId;
    private String serverName;
    private String ipAddress;
    private String operatingSystem;
    private boolean online;
    private String status; // "ONLINE", "OFFLINE", "WARNING", "ERROR"
    
    // Métricas SNMP
    private String uptime;
    private String systemDescription;
    private String hostname;
    
    // CPU
    private Double cpuLoad1Min;
    private Double cpuLoad5Min;
    private Double cpuLoad15Min;
    
    // Memória (em MB)
    private Long memoryTotal;
    private Long memoryUsed;
    private Long memoryAvailable;
    private Double memoryUsagePercent;
    
    // Disco (em GB)
    private Long diskTotal;
    private Long diskUsed;
    private Long diskAvailable;
    private Double diskUsagePercent;
    
    // Interface de rede
    private Integer interfaceCount;
    private Map<String, Object> networkInterfaces;
    
    // Tempos
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheck;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOnline;
    
    // Erro se houver
    private String errorMessage;
    
    /**
     * Cria um DTO básico a partir de um Server
     */
    public static ServerStatusDto fromServer(Server server) {
        return ServerStatusDto.builder()
                .serverId(server.getId())
                .serverName(server.getName())
                .ipAddress(server.getIpAddress())
                .operatingSystem(server.getOperationSystem() != null ? 
                    server.getOperationSystem().getName() : "N/A")
                .online(false)
                .status("UNKNOWN")
                .lastCheck(LocalDateTime.now())
                .build();
    }
    
    /**
     * Marca o servidor como offline com erro
     */
    public void markAsOffline(String errorMessage) {
        this.online = false;
        this.status = "OFFLINE";
        this.errorMessage = errorMessage;
        this.lastCheck = LocalDateTime.now();
    }
    
    /**
     * Marca o servidor como online
     */
    public void markAsOnline() {
        this.online = true;
        this.status = "ONLINE";
        this.errorMessage = null;
        this.lastCheck = LocalDateTime.now();
        this.lastOnline = LocalDateTime.now();
    }
    
    /**
     * Calcula percentual de uso da memória
     */
    public void calculateMemoryUsage() {
        if (memoryTotal != null && memoryUsed != null && memoryTotal > 0) {
            memoryUsagePercent = (memoryUsed.doubleValue() / memoryTotal.doubleValue()) * 100.0;
        }
    }
    
    /**
     * Calcula percentual de uso do disco
     */
    public void calculateDiskUsage() {
        if (diskTotal != null && diskUsed != null && diskTotal > 0) {
            diskUsagePercent = (diskUsed.doubleValue() / diskTotal.doubleValue()) * 100.0;
        }
    }
    
    /**
     * Determina o status baseado nas métricas
     */
    public void determineStatus() {
        if (!online) {
            status = "OFFLINE";
            return;
        }
        
        boolean hasWarning = false;
        
        // Verifica CPU (warning se > 80%)
        if (cpuLoad1Min != null && cpuLoad1Min > 80.0) {
            hasWarning = true;
        }
        
        // Verifica memória (warning se > 85%)
        if (memoryUsagePercent != null && memoryUsagePercent > 85.0) {
            hasWarning = true;
        }
        
        // Verifica disco (warning se > 90%)
        if (diskUsagePercent != null && diskUsagePercent > 90.0) {
            hasWarning = true;
        }
        
        status = hasWarning ? "WARNING" : "ONLINE";
    }
    
    /**
     * Retorna lastUpdate para compatibilidade (como Date)
     */
    public java.util.Date getLastUpdate() {
        if (lastCheck != null) {
            return java.sql.Timestamp.valueOf(lastCheck);
        }
        return null;
    }
}