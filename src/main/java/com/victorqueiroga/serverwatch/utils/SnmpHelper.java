package com.victorqueiroga.serverwatch.utils;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * Helper class for SNMP operations with corrected and standardized OIDs.
 * 
 * OID Standards Used:
 * - MIB-II (RFC 1213): System and Interface information
 * - Host Resources MIB (RFC 2790): CPU and Memory information 
 * - Net-SNMP Extension MIB: Linux/Unix specific metrics
 * - Interface MIB (RFC 2863): Extended interface information
 * 
 * @author Victor Queiroga
 * @version 2.0 - Corrected OIDs and added utility methods
 */
public class SnmpHelper {

    // Standard MIB-II OIDs - Funcionam na maioria dos dispositivos
    public static final String OID_SYS_DESCR = "1.3.6.1.2.1.1.1.0";          // System Description
    public static final String OID_SYS_UPTIME = "1.3.6.1.2.1.1.3.0";         // System Uptime
    public static final String OID_IF_NUMBER = "1.3.6.1.2.1.2.1.0";          // Number of Interfaces
    public static final String OID_IF_TABLE = "1.3.6.1.2.1.2.2.1";           // Interface Table
    
    // CPU Load OIDs - Net-SNMP (Linux/Unix apenas)
    public static final String OID_CPU_LOAD_1MIN = "1.3.6.1.4.1.2021.10.1.3.1";  // 1 min load average (Linux)
    public static final String OID_CPU_LOAD_5MIN = "1.3.6.1.4.1.2021.10.1.3.2";  // 5 min load average (Linux)
    public static final String OID_CPU_LOAD_15MIN = "1.3.6.1.4.1.2021.10.1.3.3"; // 15 min load average (Linux)
    
    // CPU OIDs - Host Resources MIB (Windows compatível)
    public static final String OID_HR_PROCESSOR_LOAD = "1.3.6.1.2.1.25.3.3.1.2"; // Host Resources CPU Load
    public static final String OID_HR_PROCESSOR_FRWID = "1.3.6.1.2.1.25.3.2.1.3"; // Processor ID
    
    // Memory OIDs - Host Resources MIB (Windows compatível)
    public static final String OID_HR_MEM_SIZE = "1.3.6.1.2.1.25.2.2.0";        // Memory Size
    public static final String OID_HR_STORAGE_INDEX = "1.3.6.1.2.1.25.2.3.1.1";  // Storage Index
    public static final String OID_HR_STORAGE_TYPE = "1.3.6.1.2.1.25.2.3.1.2";   // Storage Type
    public static final String OID_HR_STORAGE_DESCR = "1.3.6.1.2.1.25.2.3.1.3";  // Storage Description
    public static final String OID_HR_STORAGE_UNITS = "1.3.6.1.2.1.25.2.3.1.4";  // Storage Allocation Units
    public static final String OID_HR_STORAGE_SIZE = "1.3.6.1.2.1.25.2.3.1.5";   // Storage Size
    public static final String OID_HR_STORAGE_USED = "1.3.6.1.2.1.25.2.3.1.6";   // Storage Used
    
    // Memory OIDs - Net-SNMP específico (Linux/Unix apenas)
    public static final String OID_MEM_TOTAL_REAL = "1.3.6.1.4.1.2021.4.5.0";    // Total Real Memory (KB) - Linux
    public static final String OID_MEM_AVAIL_REAL = "1.3.6.1.4.1.2021.4.6.0";    // Available Real Memory (KB) - Linux
    public static final String OID_MEM_USED_REAL = "1.3.6.1.4.1.2021.4.14.0";    // Used Real Memory (KB) - Linux
    public static final String OID_MEM_FREE_REAL = "1.3.6.1.4.1.2021.4.11.0";    // Free Real Memory (KB) - Linux
    
    // Disk/Storage OIDs - Net-SNMP (Linux/Unix apenas)
    public static final String OID_DISK_PATH = "1.3.6.1.4.1.2021.9.1.2";        // Disk Path - Linux
    public static final String OID_DISK_TOTAL = "1.3.6.1.4.1.2021.9.1.6";       // Total Disk Size (KB) - Linux
    public static final String OID_DISK_AVAIL = "1.3.6.1.4.1.2021.9.1.7";       // Available Disk (KB) - Linux
    public static final String OID_DISK_USED = "1.3.6.1.4.1.2021.9.1.8";        // Used Disk (KB) - Linux
    public static final String OID_DISK_PERCENT = "1.3.6.1.4.1.2021.9.1.9";     // Disk Usage % - Linux
    
    // Windows Performance Counter OIDs (quando disponíveis)
    public static final String OID_WIN_CPU_UTIL = "1.3.6.1.4.1.311.1.1.3.1.1.2"; // Windows CPU Utilization
    public static final String OID_WIN_MEM_AVAIL = "1.3.6.1.4.1.311.1.1.3.3.1.2"; // Windows Available Memory
    // System Information OIDs
    public static final String OID_HOSTNAME = "1.3.6.1.2.1.1.5.0";             // System Name (Hostname)
    public static final String OID_CONTACT = "1.3.6.1.2.1.1.4.0";              // System Contact
    public static final String OID_LOCATION = "1.3.6.1.2.1.1.6.0";             // System Location
    
    // Interface Table OIDs - MIB-II (RFC 1213)
    public static final String OID_IF_INDEX = "1.3.6.1.2.1.2.2.1.1";           // Interface Index
    public static final String OID_IF_DESC = "1.3.6.1.2.1.2.2.1.2";            // Interface Description
    public static final String OID_IF_TYPE = "1.3.6.1.2.1.2.2.1.3";            // Interface Type
    public static final String OID_IF_MTU = "1.3.6.1.2.1.2.2.1.4";             // Interface MTU
    public static final String OID_IF_SPEED = "1.3.6.1.2.1.2.2.1.5";           // Interface Speed
    public static final String OID_IF_PHYS_ADDRESS = "1.3.6.1.2.1.2.2.1.6";    // Interface MAC Address
    public static final String OID_IF_ADMIN_STATUS = "1.3.6.1.2.1.2.2.1.7";    // Interface Admin Status
    public static final String OID_IF_OPER_STATUS = "1.3.6.1.2.1.2.2.1.8";     // Interface Operational Status
    public static final String OID_IF_LAST_CHANGE = "1.3.6.1.2.1.2.2.1.9";     // Interface Last Change
    public static final String OID_IF_IN_OCTETS = "1.3.6.1.2.1.2.2.1.10";      // Interface Input Octets
    public static final String OID_IF_IN_UCAST_PKTS = "1.3.6.1.2.1.2.2.1.11";  // Interface Input Unicast Packets
    public static final String OID_IF_IN_NUCAST_PKTS = "1.3.6.1.2.1.2.2.1.12"; // Interface Input Non-Unicast Packets
    public static final String OID_IF_IN_DISCARDS = "1.3.6.1.2.1.2.2.1.13";    // Interface Input Discards
    public static final String OID_IF_IN_ERRORS = "1.3.6.1.2.1.2.2.1.14";      // Interface Input Errors
    public static final String OID_IF_IN_UNKNOWN_PROTOS = "1.3.6.1.2.1.2.2.1.15"; // Interface Unknown Protocols
    public static final String OID_IF_OUT_OCTETS = "1.3.6.1.2.1.2.2.1.16";     // Interface Output Octets
    public static final String OID_IF_OUT_UCAST_PKTS = "1.3.6.1.2.1.2.2.1.17"; // Interface Output Unicast Packets
    public static final String OID_IF_OUT_NUCAST_PKTS = "1.3.6.1.2.1.2.2.1.18"; // Interface Output Non-Unicast Packets
    public static final String OID_IF_OUT_DISCARDS = "1.3.6.1.2.1.2.2.1.19";   // Interface Output Discards
    public static final String OID_IF_OUT_ERRORS = "1.3.6.1.2.1.2.2.1.20";     // Interface Output Errors
    public static final String OID_IF_OUT_QLEN = "1.3.6.1.2.1.2.2.1.21";       // Interface Output Queue Length
    
    // Interface Alias (RFC 2863) - Newer Interface MIB
    public static final String OID_IF_ALIAS = "1.3.6.1.2.1.31.1.1.1.18";       // Interface Alias
    public static final String OID_IF_HIGH_SPEED = "1.3.6.1.2.1.31.1.1.1.15";  // Interface High Speed (for > 4Gbps)

    private String community;
    private String address;
    private int snmpVersion = SnmpConstants.version2c;
    private int timeout = 3000;
    private int retries = 2;

    public SnmpHelper(String ip, String community) {
        this.address = "udp:" + ip + "/161"; // Porta SNMP padrão
        this.community = community;
        // Configurações mais tolerantes para melhor compatibilidade
        this.timeout = 5000; // 5 segundos
        this.retries = 3;
    }

    public String getAsString(String oid) throws Exception {
        CommunityTarget<UdpAddress> target = createTarget();

        // Criando transporte SNMP
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        // Criando PDU de solicitação GET
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        // Enviando solicitação
        ResponseEvent<UdpAddress> responseEvent = snmp.send(pdu, target);
        
        try {
            snmp.close();
        } catch (Exception e) {
            // Ignora erros ao fechar - não crítico
        }

        // Verificando resposta
        if (responseEvent != null && responseEvent.getResponse() != null) {
            PDU response = responseEvent.getResponse();
            
            // Verifica se há erro na resposta
            if (response.getErrorStatus() != 0) {
                throw new RuntimeException("Erro SNMP: " + response.getErrorStatusText() + 
                        " (código: " + response.getErrorStatus() + ") para OID: " + oid);
            }
            
            if (response.size() > 0) {
                VariableBinding vb = response.get(0);
                String result = vb.getVariable().toString();
                
                // Verifica se retornou "noSuchObject" ou "noSuchInstance"
                if (result.contains("noSuchObject") || result.contains("noSuchInstance")) {
                    throw new RuntimeException("OID não suportado pelo dispositivo: " + oid);
                }
                
                return result;
            }
        }
        
        throw new RuntimeException("SNMP Timeout ou sem resposta para OID: " + oid + 
                " (target: " + address + ", community: " + community + ")");
    }

    private CommunityTarget<UdpAddress> createTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(community));
        target.setAddress((UdpAddress) targetAddress);
        target.setRetries(retries);
        target.setTimeout(timeout);
        target.setVersion(snmpVersion);
        return target;
    }
    
    // Métodos utilitários para OIDs comumente usadas
    
    /**
     * Obtém a descrição do sistema
     */
    public String getSystemDescription() throws Exception {
        return getAsString(OID_SYS_DESCR);
    }
    
    /**
     * Obtém o hostname do sistema
     */
    public String getHostname() throws Exception {
        return getAsString(OID_HOSTNAME);
    }
    
    /**
     * Obtém o uptime do sistema em centésimos de segundo
     */
    public String getUptime() throws Exception {
        return getAsString(OID_SYS_UPTIME);
    }
    
    /**
     * Detecta se é um sistema Windows baseado na descrição do sistema
     */
    public boolean isWindowsSystem() throws Exception {
        String sysDescr = getAsString(OID_SYS_DESCR);
        return sysDescr != null && sysDescr.toLowerCase().contains("windows");
    }
    
    /**
     * Obtém o load average de CPU (inteligente por SO)
     */
    public String getCpuLoad1Min() throws Exception {
        try {
            if (isWindowsSystem()) {
                // Para Windows, tenta múltiplos OIDs de CPU
                String[] windowsCpuOids = {
                    OID_HR_PROCESSOR_LOAD + ".1",        // hrProcessorLoad.1
                    OID_HR_PROCESSOR_LOAD + ".0",        // hrProcessorLoad.0  
                    "1.3.6.1.2.1.25.3.3.1.2.196608",   // Índice específico Windows
                    "1.3.6.1.2.1.25.3.3.1.2.2",        // hrProcessorLoad.2
                };
                
                for (String oid : windowsCpuOids) {
                    try {
                        String result = getAsString(oid);
                        if (result != null && !result.contains("noSuch") && !result.trim().isEmpty()) {
                            System.out.println("✅ CPU Windows encontrada via OID: " + oid + " = " + result);
                            return result;
                        }
                    } catch (Exception ignored) {}
                }
                
                System.err.println("❌ Nenhum OID de CPU Windows funcionou");
                return null;
                
            } else {
                // Para Linux/Unix, usa Net-SNMP
                return getAsString(OID_CPU_LOAD_1MIN);
            }
        } catch (Exception e) {
            System.err.println("CPU Load não disponível: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtém a memória total (inteligente por SO)
     */
    public String getMemoryTotal() throws Exception {
        try {
            if (isWindowsSystem()) {
                // Para Windows: usa Host Resources MIB
                // Procura por storage do tipo RAM (1.3.6.1.2.1.25.2.1.2)
                return getWindowsMemoryFromHostResources("total");
            } else {
                // Para Linux/Unix: usa Net-SNMP
                return getAsString(OID_MEM_TOTAL_REAL);
            }
        } catch (Exception e) {
            System.err.println("Memória total não disponível: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtém a memória disponível (inteligente por SO)
     */
    public String getMemoryAvailable() throws Exception {
        try {
            if (isWindowsSystem()) {
                return getWindowsMemoryFromHostResources("available");
            } else {
                return getAsString(OID_MEM_AVAIL_REAL);
            }
        } catch (Exception e) {
            System.err.println("Memória disponível não disponível: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtém a memória usada (inteligente por SO)
     */
    public String getMemoryUsed() throws Exception {
        try {
            if (isWindowsSystem()) {
                return getWindowsMemoryFromHostResources("used");
            } else {
                return getAsString(OID_MEM_USED_REAL);
            }
        } catch (Exception e) {
            System.err.println("Memória usada não disponível: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Helper para obter informações de memória do Windows via Host Resources MIB
     */
    private String getWindowsMemoryFromHostResources(String type) throws Exception {
        // Para Host Resources MIB, precisamos iterar pela tabela de storage
        // e encontrar entradas do tipo RAM (OID de tipo físico de memória)
        // Por simplicidade, vamos tentar alguns índices comuns
        for (int i = 1; i <= 10; i++) {
            try {
                String storageType = getAsString(OID_HR_STORAGE_TYPE + "." + i);
                if (storageType != null && storageType.contains("1.3.6.1.2.1.25.2.1.2")) {
                    // Found RAM storage type
                    String units = getAsString(OID_HR_STORAGE_UNITS + "." + i);
                    String size = getAsString(OID_HR_STORAGE_SIZE + "." + i);
                    String used = getAsString(OID_HR_STORAGE_USED + "." + i);
                    
                    if (units != null && size != null) {
                        long unitSize = Long.parseLong(units);
                        long totalSize = Long.parseLong(size);
                        long totalBytes = unitSize * totalSize;
                        long totalKB = totalBytes / 1024;
                        
                        switch (type) {
                            case "total":
                                return String.valueOf(totalKB);
                            case "used":
                                if (used != null) {
                                    long usedSize = Long.parseLong(used);
                                    long usedBytes = unitSize * usedSize;
                                    return String.valueOf(usedBytes / 1024);
                                }
                                break;
                            case "available":
                                if (used != null) {
                                    long usedSize = Long.parseLong(used);
                                    long availableSize = totalSize - usedSize;
                                    long availableBytes = unitSize * availableSize;
                                    return String.valueOf(availableBytes / 1024);
                                }
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                // Continue tentando outros índices
            }
        }
        throw new Exception("Memória " + type + " não encontrada via Host Resources MIB");
    }
    
    /**
     * Obtém o espaço total em disco (inteligente por SO)
     */
    public String getDiskTotal() throws Exception {
        try {
            if (isWindowsSystem()) {
                return getWindowsDiskFromHostResources("total");
            } else {
                // Para Linux, tenta Net-SNMP (índice 1 = primeiro disco)
                return getAsString(OID_DISK_TOTAL + ".1");
            }
        } catch (Exception e) {
            System.err.println("Disco total não disponível: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtém o espaço usado em disco (inteligente por SO)
     */
    public String getDiskUsed() throws Exception {
        try {
            if (isWindowsSystem()) {
                return getWindowsDiskFromHostResources("used");
            } else {
                return getAsString(OID_DISK_USED + ".1");
            }
        } catch (Exception e) {
            System.err.println("Disco usado não disponível: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Obtém o espaço disponível em disco (inteligente por SO)
     */
    public String getDiskAvailable() throws Exception {
        try {
            if (isWindowsSystem()) {
                return getWindowsDiskFromHostResources("available");
            } else {
                return getAsString(OID_DISK_AVAIL + ".1");
            }
        } catch (Exception e) {
            System.err.println("Disco disponível não disponível: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Helper para obter informações de disco do Windows via Host Resources MIB
     */
    private String getWindowsDiskFromHostResources(String type) throws Exception {
        // Procura por storage do tipo disco fixo
        for (int i = 1; i <= 20; i++) {
            try {
                String storageType = getAsString(OID_HR_STORAGE_TYPE + "." + i);
                String description = getAsString(OID_HR_STORAGE_DESCR + "." + i);
                
                // Procura por discos (tipo 1.3.6.1.2.1.25.2.1.4 = disco fixo)
                // ou pela descrição contendo "C:" ou similar
                if ((storageType != null && storageType.contains("1.3.6.1.2.1.25.2.1.4")) ||
                    (description != null && (description.contains("C:") || description.contains("/"))) ) {
                    
                    String units = getAsString(OID_HR_STORAGE_UNITS + "." + i);
                    String size = getAsString(OID_HR_STORAGE_SIZE + "." + i);
                    String used = getAsString(OID_HR_STORAGE_USED + "." + i);
                    
                    if (units != null && size != null) {
                        long unitSize = Long.parseLong(units);
                        long totalSize = Long.parseLong(size);
                        long totalBytes = unitSize * totalSize;
                        long totalKB = totalBytes / 1024;
                        
                        switch (type) {
                            case "total":
                                return String.valueOf(totalKB);
                            case "used":
                                if (used != null) {
                                    long usedSize = Long.parseLong(used);
                                    long usedBytes = unitSize * usedSize;
                                    return String.valueOf(usedBytes / 1024);
                                }
                                break;
                            case "available":
                                if (used != null) {
                                    long usedSize = Long.parseLong(used);
                                    long availableSize = totalSize - usedSize;
                                    long availableBytes = unitSize * availableSize;
                                    return String.valueOf(availableBytes / 1024);
                                }
                                break;
                        }
                    }
                }
            } catch (Exception e) {
                // Continue tentando outros índices
            }
        }
        throw new Exception("Disco " + type + " não encontrado via Host Resources MIB");
    }
    
    /**
     * Obtém o número de interfaces de rede
     */
    public String getInterfaceCount() throws Exception {
        return getAsString(OID_IF_NUMBER);
    }
    
    /**
     * Método para debug - testa todos os OIDs importantes
     */
    public void debugSnmpOids() {
        System.out.println("=== DEBUG SNMP OIDs ===");
        
        String[] testOids = {
            OID_SYS_DESCR, OID_SYS_UPTIME, OID_HOSTNAME,
            OID_HR_PROCESSOR_LOAD + ".1", OID_CPU_LOAD_1MIN,
            OID_MEM_TOTAL_REAL, OID_HR_STORAGE_TYPE + ".1",
            OID_HR_STORAGE_SIZE + ".1", OID_IF_NUMBER
        };
        
        String[] testNames = {
            "System Description", "System Uptime", "Hostname",
            "CPU Load (HR)", "CPU Load (Net-SNMP)",
            "Memory Total (Net-SNMP)", "Storage Type", 
            "Storage Size", "Interface Count"
        };
        
        for (int i = 0; i < testOids.length; i++) {
            try {
                String result = getAsString(testOids[i]);
                System.out.println("[OK] " + testNames[i] + ": " + result);
            } catch (Exception e) {
                System.out.println("[FAIL] " + testNames[i] + ": " + e.getMessage());
            }
        }
        
        System.out.println("=== FIM DEBUG ===");
    }
    
    /**
     * Coleta informações de todos os discos disponíveis
     * @return Lista de DiskInfoDto com todos os discos encontrados
     */
    public java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> getAllDisks() {
        java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> diskList = new java.util.ArrayList<>();
        
        try {
            boolean isWindows = isWindowsSystem();
            
            if (isWindows) {
                // Windows: usa Host Resources MIB para enumerar todos os discos
                diskList = collectWindowsDisks();
            } else {
                // Linux: usa Net-SNMP para enumerar discos
                diskList = collectLinuxDisks();
            }
            
            // Calcula percentuais para todos os discos
            for (com.victorqueiroga.serverwatch.dto.DiskInfoDto disk : diskList) {
                disk.calculateUsagePercent();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao coletar lista de discos: " + e.getMessage());
        }
        
        return diskList;
    }
    
    /**
     * Coleta discos do Windows usando Host Resources MIB
     */
    private java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> collectWindowsDisks() {
        java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> diskList = new java.util.ArrayList<>();
        
        try {
            // Enumera índices de storage do Host Resources MIB
            for (int i = 1; i <= 20; i++) { // Testa até 20 índices
                try {
                    // Verifica o tipo de storage
                    String storageType = getAsString(OID_HR_STORAGE_TYPE + "." + i);
                    if (storageType == null || storageType.contains("noSuch")) {
                        continue;
                    }
                    
                    // Pega descrição do storage
                    String description = getAsString(OID_HR_STORAGE_DESCR + "." + i);
                    if (description == null || description.trim().isEmpty()) {
                        continue;
                    }
                    
                    // Filtra apenas Fixed Disks (tipo .4 geralmente é disco fixo)
                    if (!storageType.endsWith(".4") && !description.toLowerCase().contains("fixed")) {
                        continue; // Pula RAM, CD-ROM, etc.
                    }
                    
                    // Coleta métricas do disco
                    String totalUnits = getAsString(OID_HR_STORAGE_SIZE + "." + i);
                    String usedUnits = getAsString(OID_HR_STORAGE_USED + "." + i);
                    String unitSize = getAsString(OID_HR_STORAGE_UNITS + "." + i);
                    
                    if (totalUnits != null && !totalUnits.contains("noSuch") && 
                        usedUnits != null && !usedUnits.contains("noSuch") &&
                        unitSize != null && !unitSize.contains("noSuch")) {
                        
                        long total = Long.parseLong(totalUnits.trim());
                        long used = Long.parseLong(usedUnits.trim());
                        long unit = Long.parseLong(unitSize.trim());
                        
                        // Converte para GB
                        long totalGB = (total * unit) / (1024 * 1024 * 1024);
                        long usedGB = (used * unit) / (1024 * 1024 * 1024);
                        long availableGB = totalGB - usedGB;
                        
                        if (totalGB > 0) { // Só adiciona se tiver tamanho válido
                            com.victorqueiroga.serverwatch.dto.DiskInfoDto disk = new com.victorqueiroga.serverwatch.dto.DiskInfoDto();
                            disk.setPath(extractDriveLetter(description));
                            disk.setDescription(description);
                            disk.setTotalGB(totalGB);
                            disk.setUsedGB(usedGB);
                            disk.setAvailableGB(availableGB);
                            disk.setType("Fixed Disk");
                            
                            diskList.add(disk);
                        }
                    }
                    
                } catch (Exception e) {
                    // Ignora erros de índices individuais
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao coletar discos Windows: " + e.getMessage());
        }
        
        return diskList;
    }
    
    /**
     * Coleta discos do Linux usando Net-SNMP
     */
    private java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> collectLinuxDisks() {
        java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> diskList = new java.util.ArrayList<>();
        
        try {
            // Net-SNMP enumera discos nos índices 1, 2, 3...
            for (int i = 1; i <= 10; i++) {
                try {
                    String path = getAsString(OID_DISK_PATH + "." + i);
                    if (path == null || path.contains("noSuch") || path.trim().isEmpty()) {
                        continue;
                    }
                    
                    String total = getAsString(OID_DISK_TOTAL + "." + i);
                    String used = getAsString(OID_DISK_USED + "." + i);
                    String avail = getAsString(OID_DISK_AVAIL + "." + i);
                    
                    if (total != null && !total.contains("noSuch") &&
                        used != null && !used.contains("noSuch") &&
                        avail != null && !avail.contains("noSuch")) {
                        
                        long totalKB = Long.parseLong(total.trim());
                        long usedKB = Long.parseLong(used.trim());
                        long availKB = Long.parseLong(avail.trim());
                        
                        // Converte para GB
                        long totalGB = totalKB / (1024 * 1024);
                        long usedGB = usedKB / (1024 * 1024);
                        long availableGB = availKB / (1024 * 1024);
                        
                        if (totalGB > 0) {
                            com.victorqueiroga.serverwatch.dto.DiskInfoDto disk = new com.victorqueiroga.serverwatch.dto.DiskInfoDto();
                            disk.setPath(path);
                            disk.setDescription(path + " filesystem");
                            disk.setTotalGB(totalGB);
                            disk.setUsedGB(usedGB);
                            disk.setAvailableGB(availableGB);
                            disk.setType("Linux Filesystem");
                            
                            diskList.add(disk);
                        }
                    }
                    
                } catch (Exception e) {
                    // Ignora erros de índices individuais
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao coletar discos Linux: " + e.getMessage());
        }
        
        return diskList;
    }
    
    /**
     * Extrai a letra do drive da descrição do Windows (ex: "C:\" de "C:\ Label:System  Serial Number 123456")
     */
    private String extractDriveLetter(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "Unknown";
        }
        
        // Procura por padrão "C:" no início
        if (description.matches("^[A-Z]:\\\\.*")) {
            return description.substring(0, 2); // Retorna "C:"
        }
        
        // Se não encontrar, usa os primeiros caracteres
        String[] parts = description.split("\\s+");
        return parts.length > 0 ? parts[0] : "Unknown";
    }
}
