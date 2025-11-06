package com.victorqueiroga.serverwatch.utils;

import java.util.ArrayList;
import java.util.List;

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
 * OID Standards Used: - MIB-II (RFC 1213): System and Interface information -
 * Host Resources MIB (RFC 2790): CPU and Memory information - Net-SNMP
 * Extension MIB: Linux/Unix specific metrics - Interface MIB (RFC 2863):
 * Extended interface information
 *
 * @author Victor Queiroga
 * @version 2.0 - Corrected OIDs and added utility methods
 */
public class SnmpHelper {

    // Standard MIB-II OIDs
    public static final String OID_SYS_DESCR = "1.3.6.1.2.1.1.1.0";
    public static final String OID_SYS_UPTIME = "1.3.6.1.2.1.1.3.0";
    public static final String OID_IF_NUMBER = "1.3.6.1.2.1.2.1.0";
    public static final String OID_IF_TABLE = "1.3.6.1.2.1.2.2.1";
    public static final String OID_HOSTNAME = "1.3.6.1.2.1.1.5.0";
    public static final String OID_CONTACT = "1.3.6.1.2.1.1.4.0";
    public static final String OID_LOCATION = "1.3.6.1.2.1.1.6.0";

    // CPU OIDs - Net-SNMP (Linux/Unix)
    public static final String OID_CPU_LOAD_1MIN = "1.3.6.1.4.1.2021.10.1.3.1";
    public static final String OID_CPU_LOAD_5MIN = "1.3.6.1.4.1.2021.10.1.3.2";
    public static final String OID_CPU_LOAD_15MIN = "1.3.6.1.4.1.2021.10.1.3.3";

    // CPU Detailed Stats - Net-SNMP (Linux)
    public static final String OID_SS_CPU_RAW_USER = "1.3.6.1.4.1.2021.11.50.0";      // User CPU time
    public static final String OID_SS_CPU_RAW_SYSTEM = "1.3.6.1.4.1.2021.11.52.0";    // System CPU time
    public static final String OID_SS_CPU_RAW_IDLE = "1.3.6.1.4.1.2021.11.53.0";      // Idle CPU time
    public static final String OID_SS_CPU_RAW_NICE = "1.3.6.1.4.1.2021.11.51.0";      // Nice CPU time

    // CPU Percentage - Net-SNMP (Pre-calculated)
    public static final String OID_SS_CPU_USER_PERCENT = "1.3.6.1.4.1.2021.11.9.0";   // User %
    public static final String OID_SS_CPU_SYSTEM_PERCENT = "1.3.6.1.4.1.2021.11.10.0"; // System %
    public static final String OID_SS_CPU_IDLE_PERCENT = "1.3.6.1.4.1.2021.11.11.0";   // Idle %

    // Host Resources MIB - CPU (Multi-platform)
    public static final String OID_HR_PROCESSOR_LOAD = "1.3.6.1.2.1.25.3.3.1.2";
    public static final String OID_HR_PROCESSOR_FRWID = "1.3.6.1.2.1.25.3.2.1.3";

    // Windows specific
    public static final String OID_WIN_CPU_UTIL = "1.3.6.1.4.1.311.1.1.3.1.1.2";

    // Memory OIDs - Host Resources MIB
    public static final String OID_HR_MEM_SIZE = "1.3.6.1.2.1.25.2.2.0";
    public static final String OID_HR_STORAGE_INDEX = "1.3.6.1.2.1.25.2.3.1.1";
    public static final String OID_HR_STORAGE_TYPE = "1.3.6.1.2.1.25.2.3.1.2";
    public static final String OID_HR_STORAGE_DESCR = "1.3.6.1.2.1.25.2.3.1.3";
    public static final String OID_HR_STORAGE_UNITS = "1.3.6.1.2.1.25.2.3.1.4";
    public static final String OID_HR_STORAGE_SIZE = "1.3.6.1.2.1.25.2.3.1.5";
    public static final String OID_HR_STORAGE_USED = "1.3.6.1.2.1.25.2.3.1.6";

    // Memory OIDs - Net-SNMP
    public static final String OID_MEM_TOTAL_REAL = "1.3.6.1.4.1.2021.4.5.0";
    public static final String OID_MEM_AVAIL_REAL = "1.3.6.1.4.1.2021.4.6.0";
    public static final String OID_MEM_USED_REAL = "1.3.6.1.4.1.2021.4.14.0";
    public static final String OID_MEM_FREE_REAL = "1.3.6.1.4.1.2021.4.11.0";

    // Disk OIDs - Net-SNMP
    public static final String OID_DISK_PATH = "1.3.6.1.4.1.2021.9.1.2";
    public static final String OID_DISK_TOTAL = "1.3.6.1.4.1.2021.9.1.6";
    public static final String OID_DISK_AVAIL = "1.3.6.1.4.1.2021.9.1.7";
    public static final String OID_DISK_USED = "1.3.6.1.4.1.2021.9.1.8";
    public static final String OID_DISK_PERCENT = "1.3.6.1.4.1.2021.9.1.9";

    // Interface Table OIDs
    public static final String OID_IF_INDEX = "1.3.6.1.2.1.2.2.1.1";
    public static final String OID_IF_DESC = "1.3.6.1.2.1.2.2.1.2";
    public static final String OID_IF_TYPE = "1.3.6.1.2.1.2.2.1.3";
    public static final String OID_IF_MTU = "1.3.6.1.2.1.2.2.1.4";
    public static final String OID_IF_SPEED = "1.3.6.1.2.1.2.2.1.5";
    public static final String OID_IF_PHYS_ADDRESS = "1.3.6.1.2.1.2.2.1.6";
    public static final String OID_IF_ADMIN_STATUS = "1.3.6.1.2.1.2.2.1.7";
    public static final String OID_IF_OPER_STATUS = "1.3.6.1.2.1.2.2.1.8";
    public static final String OID_IF_LAST_CHANGE = "1.3.6.1.2.1.2.2.1.9";
    public static final String OID_IF_IN_OCTETS = "1.3.6.1.2.1.2.2.1.10";
    public static final String OID_IF_IN_UCAST_PKTS = "1.3.6.1.2.1.2.2.1.11";
    public static final String OID_IF_IN_NUCAST_PKTS = "1.3.6.1.2.1.2.2.1.12";
    public static final String OID_IF_IN_DISCARDS = "1.3.6.1.2.1.2.2.1.13";
    public static final String OID_IF_IN_ERRORS = "1.3.6.1.2.1.2.2.1.14";
    public static final String OID_IF_IN_UNKNOWN_PROTOS = "1.3.6.1.2.1.2.2.1.15";
    public static final String OID_IF_OUT_OCTETS = "1.3.6.1.2.1.2.2.1.16";
    public static final String OID_IF_OUT_UCAST_PKTS = "1.3.6.1.2.1.2.2.1.17";
    public static final String OID_IF_OUT_NUCAST_PKTS = "1.3.6.1.2.1.2.2.1.18";
    public static final String OID_IF_OUT_DISCARDS = "1.3.6.1.2.1.2.2.1.19";
    public static final String OID_IF_OUT_ERRORS = "1.3.6.1.2.1.2.2.1.20";
    public static final String OID_IF_OUT_QLEN = "1.3.6.1.2.1.2.2.1.21";
    public static final String OID_IF_ALIAS = "1.3.6.1.2.1.31.1.1.1.18";
    public static final String OID_IF_HIGH_SPEED = "1.3.6.1.2.1.31.1.1.1.15";

    private String community;
    private String address;
    private int snmpVersion = SnmpConstants.version2c;
    private int timeout = 5000;
    private int retries = 2;

    // Cache para detecção de SO
    private Boolean isWindows = null;

    public SnmpHelper(String ip, String community) {
        this.address = "udp:" + ip + "/161"; // Porta SNMP padrão
        this.community = community;
        // Configurações mais tolerantes para melhor compatibilidade
        this.timeout = 5000; // 5 segundos
        this.retries = 3;
    }

    public String getAsString(String oid) throws Exception {
        CommunityTarget<UdpAddress> target = createTarget();
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);

        ResponseEvent<UdpAddress> responseEvent = snmp.send(pdu, target);

        try {
            snmp.close();
        } catch (Exception e) {
            // Ignora erros ao fechar
        }

        if (responseEvent != null && responseEvent.getResponse() != null) {
            PDU response = responseEvent.getResponse();

            if (response.getErrorStatus() != 0) {
                throw new RuntimeException("Erro SNMP: " + response.getErrorStatusText()
                        + " (código: " + response.getErrorStatus() + ") para OID: " + oid);
            }

            if (response.size() > 0) {
                VariableBinding vb = response.get(0);
                String result = vb.getVariable().toString();

                if (result.contains("noSuchObject") || result.contains("noSuchInstance")) {
                    throw new RuntimeException("OID não suportado pelo dispositivo: " + oid);
                }

                return result;
            }
        }

        throw new RuntimeException("SNMP Timeout ou sem resposta para OID: " + oid);
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
        if (isWindows == null) {
            String sysDescr = getAsString(OID_SYS_DESCR).toLowerCase();
            isWindows = sysDescr.contains("windows") || sysDescr.contains("microsoft");
        }
        return isWindows;
    }

    /**
     * Detecta se é um sistema Linux/Unix (com cache)
     */
    public boolean isLinuxSystem() throws Exception {
        if (isWindows == null) {
            isWindowsSystem(); // Popula o cache
        }
        return !isWindows;
    }

    /**
     * Obtém a carga média da CPU (em porcentagem 0-100) usando Host Resources
     * MIB. Tenta em todos os sistemas operacionais, pois é a métrica mais
     * universal de 'uso'.
     */
    @Deprecated
    public String getCpuLoadPercent() throws Exception {
        return getCpuLoad1Min();
    }

    public Double getCpuUsagePercent() throws Exception {
        System.out.println("\n=== Iniciando coleta de CPU ===");

        try {
            if (isLinuxSystem()) {
                return getLinuxCpuUsage();
            } else {
                return getWindowsCpuUsage();
            }
        } catch (Exception e) {
            System.err.println("❌ Erro na coleta principal de CPU: " + e.getMessage());
            // Tenta fallback genérico
            return getFallbackCpuUsage();
        }
    }

    private Double getLinuxCpuUsage() throws Exception {
        System.out.println("📊 Detectado: Sistema Linux/Unix");

        // MÉTODO 1: Percentuais pré-calculados (mais preciso)
        try {
            String idlePercent = getAsString(OID_SS_CPU_IDLE_PERCENT);
            if (idlePercent != null && !idlePercent.contains("noSuch")) {
                double idle = Double.parseDouble(idlePercent.trim());
                double usage = 100.0 - idle;

                // Validação
                if (usage >= 0 && usage <= 100) {
                    System.out.println("✅ CPU (Net-SNMP Percentual): " + String.format("%.2f%%", usage));
                    return clampPercent(usage);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️  Net-SNMP Percentual não disponível: " + e.getMessage());
        }

        // MÉTODO 2: Cálculo via User + System percentuais
        try {
            String userPercent = getAsString(OID_SS_CPU_USER_PERCENT);
            String systemPercent = getAsString(OID_SS_CPU_SYSTEM_PERCENT);

            if (userPercent != null && systemPercent != null
                    && !userPercent.contains("noSuch") && !systemPercent.contains("noSuch")) {
                double user = Double.parseDouble(userPercent.trim());
                double system = Double.parseDouble(systemPercent.trim());
                double usage = user + system;

                if (usage >= 0 && usage <= 100) {
                    System.out.println("✅ CPU (User+System): " + String.format("%.2f%%", usage));
                    return clampPercent(usage);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️  User+System não disponível: " + e.getMessage());
        }

        // MÉTODO 3: Host Resources MIB (funciona em muitos Linux)
        try {
            List<VariableBinding> cpuLoads = snmpWalk(OID_HR_PROCESSOR_LOAD);

            if (!cpuLoads.isEmpty()) {
                double totalLoad = 0;
                int validCount = 0;

                for (VariableBinding vb : cpuLoads) {
                    try {
                        String valueStr = vb.getVariable().toString().trim();
                        double load = Double.parseDouble(valueStr);

                        if (load >= 0 && load <= 100) {
                            totalLoad += load;
                            validCount++;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

                if (validCount > 0) {
                    double avgLoad = totalLoad / validCount;
                    System.out.println("✅ CPU (Host Resources): " + String.format("%.2f%%", avgLoad)
                            + " (média de " + validCount + " cores)");
                    return clampPercent(avgLoad);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️  Host Resources MIB não disponível em Linux: " + e.getMessage());
        }

        // MÉTODO 4: Load Average convertido para porcentagem
        try {
            return getLoadAverageAsPercent();
        } catch (Exception e) {
            System.out.println("⚠️  Load Average não disponível: " + e.getMessage());
        }

        throw new Exception("Nenhum método de coleta de CPU funcionou para Linux");
    }

   
    private Double getWindowsCpuUsage() throws Exception {
        System.out.println("📊 Detectado: Sistema Windows");

        // MÉTODO 1: Host Resources MIB - média de hrProcessorLoad por core
        try {
            List<VariableBinding> cpuLoads = snmpWalk(OID_HR_PROCESSOR_LOAD);

            if (!cpuLoads.isEmpty()) {
                double totalLoad = 0;
                int validCount = 0;

                for (VariableBinding vb : cpuLoads) {
                    try {
                        String valueStr = vb.getVariable().toString().trim();
                        double load = Double.parseDouble(valueStr);

                        // Validação: 0..100
                        if (load >= 0 && load <= 100) {
                          return load;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

                if (validCount > 0) {
                    double avgLoad = totalLoad / validCount;
                    System.out.println("✅ CPU (Host Resources média de " + validCount + " cores): "
                            + String.format("%.2f%%", avgLoad));
                    return avgLoad;
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️  Host Resources MIB (walk) não disponível: " + e.getMessage());
        }

        // MÉTODO 2: Host Resources MIB - tentar índices comuns individuais
        int[] commonIndexes = {1, 2, 0, 196608};
        double total = 0;
        int count = 0;
        for (int idx : commonIndexes) {
            try {
                String value = getAsString(OID_HR_PROCESSOR_LOAD + "." + idx);
                if (value != null && !value.contains("noSuch")) {
                    double load = Double.parseDouble(value.trim());
                    if (load >= 0 && load <= 100) {
                        total += load;
                        count++;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (count == 0) {
            // Última tentativa com varredura de 1..32
            for (int idx = 1; idx <= 32; idx++) {
                try {
                    String value = getAsString(OID_HR_PROCESSOR_LOAD + "." + idx);
                    if (value != null && !value.contains("noSuch")) {
                        double load = Double.parseDouble(value.trim());
                        if (load >= 0 && load <= 100) {
                            total += load;
                            count++;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (count > 0) {
            double avg = total / count;
            System.out.println("✅ CPU (Host Resources índices individuais): " + String.format("%.2f%%", avg)
                    + " (" + count + " núcleos)");
            return avg;
        }

        // MÉTODO 3: OID específico Windows (pode variar por agente SNMP)
        try {
            // Tenta alguns índices usuais
            String[] candidates = {OID_WIN_CPU_UTIL + ".0", OID_WIN_CPU_UTIL + ".1", OID_WIN_CPU_UTIL};
            for (String oid : candidates) {
                try {
                    String winCpu = getAsString(oid);
                    if (winCpu != null && !winCpu.contains("noSuch")) {
                        double usage = Double.parseDouble(winCpu.trim());
                        if (usage >= 0 && usage <= 100) {
                            System.out.println("✅ CPU (Windows OID): " + String.format("%.2f%%", usage)
                                    + " via " + oid);
                            return usage;
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️  Windows OID específico não disponível: " + e.getMessage());
        }

        throw new Exception("Nenhum método de coleta de CPU funcionou para Windows");
    }

    private Double getLoadAverageAsPercent() throws Exception {
        String load1min = getAsString(OID_CPU_LOAD_1MIN);

        if (load1min != null && !load1min.contains("noSuch")) {
            double load = Double.parseDouble(load1min.trim());

            // Tenta detectar número de CPUs
            int cpuCount = getCpuCount();

            // Calcula porcentagem baseada no load e número de CPUs
            double percent = (load / cpuCount) * 100.0;

            // Limita entre 0 e 100
            percent = Math.min(100.0, Math.max(0.0, percent));

            System.out.println("✅ CPU (Load Average): " + String.format("%.2f", load)
                    + " → " + String.format("%.2f%%", percent)
                    + " (" + cpuCount + " cores)");

            return clampPercent(percent);
        }

        throw new Exception("Load Average não disponível");
    }

    private Double getFallbackCpuUsage() throws Exception {
        System.out.println("⚠️  Tentando método de fallback genérico...");

        // Tenta Host Resources mesmo em Linux (alguns suportam)
        try {
            List<VariableBinding> cpuLoads = snmpWalk(OID_HR_PROCESSOR_LOAD);
            if (!cpuLoads.isEmpty()) {
                double totalLoad = 0;
                int count = 0;

                for (VariableBinding vb : cpuLoads) {
                    try {
                        double load = Double.parseDouble(vb.getVariable().toString().trim());
                        if (load >= 0 && load <= 100) {
                            totalLoad += load;
                            count++;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

                if (count > 0) {
                    double avg = totalLoad / count;
                    System.out.println("✅ CPU (Fallback HR): " + String.format("%.2f%%", avg));
                    return clampPercent(avg);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️  Fallback HR falhou: " + e.getMessage());
        }

        throw new Exception("Todos os métodos de coleta de CPU falharam");
    }

    /**
     * Tenta detectar o número de CPUs/cores do sistema
     */
    private int getCpuCount() {
        try {
            // 1) Tentar contar entradas de hrProcessorLoad (mais comum)
            List<VariableBinding> loads = snmpWalk(OID_HR_PROCESSOR_LOAD);
            if (!loads.isEmpty()) {
                return Math.max(1, loads.size());
            }

            // 2) Fallback: contar entradas de FRWID
            List<VariableBinding> processors = snmpWalk(OID_HR_PROCESSOR_FRWID);
            if (!processors.isEmpty()) {
                return Math.max(1, processors.size());
            }
        } catch (Exception e) {
            // Ignora erro
        }

        // Fallback: assume 1 core se não conseguir detectar
        return 1;
    }

    private double clampPercent(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(100.0, v));
    }

    /**
     * @deprecated Use getCpuUsagePercent() para melhor precisão
     */
    @Deprecated
    public String getCpuLoad1Min() throws Exception {
        try {
            Double usage = getCpuUsagePercent();
            return String.format("%.2f", usage);
        } catch (Exception e) {
            System.err.println("❌ Não foi possível obter CPU: " + e.getMessage());
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
     * Helper para obter informações de memória do Windows via Host Resources
     * MIB
     */
    private String getWindowsMemoryFromHostResources(String type) throws Exception {
        for (int i = 1; i <= 10; i++) {
            try {
                String storageType = getAsString(OID_HR_STORAGE_TYPE + "." + i);
                if (storageType != null && storageType.contains("1.3.6.1.2.1.25.2.1.2")) {
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
                if ((storageType != null && storageType.contains("1.3.6.1.2.1.25.2.1.4"))
                        || (description != null && (description.contains("C:") || description.contains("/")))) {

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
        System.out.println("\n=== DEBUG SNMP OIDs ===");

        String[] testOids = {
            OID_SYS_DESCR, OID_SYS_UPTIME, OID_HOSTNAME,
            OID_HR_PROCESSOR_LOAD + ".1", OID_CPU_LOAD_1MIN,
            OID_SS_CPU_IDLE_PERCENT, OID_SS_CPU_USER_PERCENT,
            OID_MEM_TOTAL_REAL, OID_HR_STORAGE_TYPE + ".1",
            OID_HR_STORAGE_SIZE + ".1", OID_IF_NUMBER
        };

        String[] testNames = {
            "System Description", "System Uptime", "Hostname",
            "CPU Load (HR)", "CPU Load Average",
            "CPU Idle %", "CPU User %",
            "Memory Total", "Storage Type",
            "Storage Size", "Interface Count"
        };

        for (int i = 0; i < testOids.length; i++) {
            try {
                String result = getAsString(testOids[i]);
                System.out.println("[✅ OK] " + testNames[i] + ": " + result);
            } catch (Exception e) {
                System.out.println("[❌ FAIL] " + testNames[i] + ": " + e.getMessage());
            }
        }

        System.out.println("\n=== Teste de CPU Inteligente ===");
        try {
            Double cpuUsage = getCpuUsagePercent();
            System.out.println("[✅ OK] CPU Usage: " + String.format("%.2f%%", cpuUsage));
        } catch (Exception e) {
            System.out.println("[❌ FAIL] CPU Usage: " + e.getMessage());
        }

        System.out.println("=== FIM DEBUG ===\n");
    }

    /**
     * Diagnóstico detalhado para coleta de discos
     * Ajuda a identificar qual MIB está disponível no servidor SNMP
     */
    public void diagnosticDiskCollection() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║              DIAGNÓSTICO DE COLETA DE DISCOS                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        try {
            String sysDescr = getSystemDescription();
            System.out.println("🖥️  Sistema: " + sysDescr);
            
            boolean isWin = isWindowsSystem();
            System.out.println("🔍 Tipo detectado: " + (isWin ? "WINDOWS" : "LINUX/UNIX"));
        } catch (Exception e) {
            System.out.println("❌ Erro ao detectar sistema: " + e.getMessage());
        }

        System.out.println("\n--- TESTE 1: UCD-MIB (Net-SNMP) ---");
        System.out.println("Testando OID: 1.3.6.1.4.1.2021.9.1.2 (disk paths)");
        int ucdDisksFound = 0;
        for (int i = 1; i <= 5; i++) {
            try {
                String path = getAsString(OID_DISK_PATH + "." + i);
                if (path != null && !path.contains("noSuch")) {
                    System.out.println("  ✅ Índice " + i + ": " + path);
                    ucdDisksFound++;
                }
            } catch (Exception e) {
                // Silent
            }
        }
        if (ucdDisksFound == 0) {
            System.out.println("  ❌ Nenhum disco encontrado via UCD-MIB");
        } else {
            System.out.println("  ✅ UCD-MIB funcional! (" + ucdDisksFound + " discos)");
        }

        System.out.println("\n--- TESTE 2: Host Resources MIB Storage ---");
        System.out.println("Testando OID: 1.3.6.1.2.1.25.2.3.1.3 (storage descriptions)");
        int hrStorageFound = 0;
        int hrFilesystemsFound = 0;
        for (int i = 1; i <= 100; i++) {
            try {
                String type = getAsString(OID_HR_STORAGE_TYPE + "." + i);
                String desc = getAsString(OID_HR_STORAGE_DESCR + "." + i);
                
                if (type != null && !type.contains("noSuch") && desc != null) {
                    System.out.println("  📊 Índice " + i + ": " + desc + " (tipo: " + type + ")");
                    hrStorageFound++;
                    
                    if (desc.startsWith("/") || type.endsWith(".4")) {
                        hrFilesystemsFound++;
                    }
                }
            } catch (Exception e) {
                // Silent
            }
        }
        if (hrStorageFound == 0) {
            System.out.println("  ❌ Host Resources MIB não disponível");
        } else {
            System.out.println("  ✅ Host Resources MIB disponível!");
            System.out.println("     ↳ " + hrStorageFound + " storage entries");
            System.out.println("     ↳ " + hrFilesystemsFound + " parecem ser filesystems");
        }

        System.out.println("\n--- RECOMENDAÇÕES ---");
        if (ucdDisksFound == 0 && hrFilesystemsFound == 0) {
            System.out.println("⚠️  NENHUMA fonte de dados de disco encontrada!");
            System.out.println("    Você precisa:");
            System.out.println("    1. Instalar Net-SNMP com suporte completo MIB");
            System.out.println("    2. Configurar /etc/snmp/snmpd.conf com:");
            System.out.println("       - rocommunity public");
            System.out.println("       - disk / 10000");
            System.out.println("    3. Reiniciar: sudo systemctl restart snmpd");
        } else if (ucdDisksFound > 0) {
            System.out.println("✅ UCD-MIB está funcionando. Discos devem ser coletados normalmente.");
        } else if (hrFilesystemsFound > 0) {
            System.out.println("✅ Host Resources MIB com filesystems está disponível.");
            System.out.println("   Usaremos este como fallback se UCD-MIB falhar.");
        }

        System.out.println("\n╚════════════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Coleta informações de todos os discos disponíveis
     *
     * @return Lista de DiskInfoDto com todos os discos encontrados
     */
    public java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> getAllDisks() {
        java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> diskList = new java.util.ArrayList<>();

        try {
            boolean isWindows = isWindowsSystem();
            System.out.println("🖥️  Sistema detectado: " + (isWindows ? "WINDOWS" : "LINUX/UNIX"));

            if (isWindows) {
                // Windows: usa Host Resources MIB para enumerar todos os discos
                System.out.println("📀 Coletando discos via Host Resources MIB (Windows)...");
                diskList = collectWindowsDisks();
            } else {
                // Linux: usa Net-SNMP para enumerar discos
                System.out.println("📀 Coletando discos via UCD-MIB (Linux)...");
                diskList = collectLinuxDisks();
            }

            System.out.println("📊 Total de discos encontrados: " + diskList.size());

            // Calcula percentuais para todos os discos
            for (com.victorqueiroga.serverwatch.dto.DiskInfoDto disk : diskList) {
                disk.calculateUsagePercent();
                System.out.println("   - " + disk.getPath() + ": " + disk.getUsagePercent() + "% utilizado");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao coletar lista de discos: " + e.getMessage());
            e.printStackTrace();
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

                    if (totalUnits != null && !totalUnits.contains("noSuch")
                            && usedUnits != null && !usedUnits.contains("noSuch")
                            && unitSize != null && !unitSize.contains("noSuch")) {

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
            System.out.println("🔍 Coletando discos Linux via SNMP...");
            
            // Net-SNMP enumera discos nos índices 1, 2, 3...
            // Aumentar para 20 para contemplar mais discos
            for (int i = 1; i <= 20; i++) {
                try {
                    String path = getAsString(OID_DISK_PATH + "." + i);
                    
                    // Se path é nulo ou contém erro SNMP, pula
                    if (path == null || path.contains("noSuch") || path.trim().isEmpty()) {
                        continue;
                    }

                    String total = getAsString(OID_DISK_TOTAL + "." + i);
                    String used = getAsString(OID_DISK_USED + "." + i);
                    String avail = getAsString(OID_DISK_AVAIL + "." + i);

                    System.out.println("📊 Disco encontrado [índice " + i + "]: " + path);
                    System.out.println("   Total: " + total + " KB, Usado: " + used + " KB, Disponível: " + avail + " KB");

                    if (total != null && !total.contains("noSuch")
                            && used != null && !used.contains("noSuch")
                            && avail != null && !avail.contains("noSuch")) {

                        try {
                            long totalKB = Long.parseLong(total.trim());
                            long usedKB = Long.parseLong(used.trim());
                            long availKB = Long.parseLong(avail.trim());

                            // Converte para GB
                            long totalGB = totalKB / (1024 * 1024);
                            long usedGB = usedKB / (1024 * 1024);
                            long availableGB = availKB / (1024 * 1024);

                            // Adicionar disco mesmo se totalGB == 0 (para discos pequenos)
                            if (totalKB > 0) {
                                com.victorqueiroga.serverwatch.dto.DiskInfoDto disk = new com.victorqueiroga.serverwatch.dto.DiskInfoDto();
                                disk.setPath(path);
                                disk.setDescription(path + " filesystem");
                                disk.setTotalGB(totalGB);
                                disk.setUsedGB(usedGB);
                                disk.setAvailableGB(availableGB);
                                disk.setType("Linux Filesystem");
                                disk.calculateUsagePercent();

                                diskList.add(disk);
                                System.out.println("   ✅ Disco adicionado: " + totalGB + " GB total, " + usedGB + " GB usado");
                            }
                        } catch (NumberFormatException nfe) {
                            System.out.println("   ⚠️ Erro ao parsear números para disco em índice " + i + ": " + nfe.getMessage());
                        }
                    }

                } catch (Exception e) {
                    // Ignora erros de índices individuais silenciosamente
                }
            }

            System.out.println("📈 Total de discos coletados (UCD-MIB): " + diskList.size());

            // Se não encontrou discos com UCD-MIB, tenta Host Resources MIB (fallback)
            if (diskList.isEmpty()) {
                System.out.println("⚠️  UCD-MIB não retornou discos. Tentando Host Resources MIB (fallback)...");
                diskList = collectLinuxDisksViaHostResources();
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao coletar discos Linux: " + e.getMessage());
            e.printStackTrace();
        }

        return diskList;
    }

    /**
     * Coleta discos do Linux usando Host Resources MIB (fallback quando UCD-MIB não funciona)
     */
    private java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> collectLinuxDisksViaHostResources() {
        java.util.List<com.victorqueiroga.serverwatch.dto.DiskInfoDto> diskList = new java.util.ArrayList<>();

        try {
            System.out.println("🔍 Coletando discos Linux via Host Resources MIB...");
            
            int storageEntriesFound = 0;
            int diskEntriesFound = 0;
            
            // Enumera índices de storage do Host Resources MIB
            // Aumentado para 100 para contemplar índices maiores (some systems use indices > 50)
            for (int i = 1; i <= 100; i++) {
                try {
                    // Verifica o tipo de storage
                    String storageType = getAsString(OID_HR_STORAGE_TYPE + "." + i);
                    if (storageType == null || storageType.contains("noSuch")) {
                        continue;
                    }

                    storageEntriesFound++;

                    // Pega descrição do storage
                    String description = getAsString(OID_HR_STORAGE_DESCR + "." + i);
                    if (description == null || description.trim().isEmpty()) {
                        continue;
                    }

                    System.out.println("📊 Storage encontrado [índice " + i + "]: " + description + " (tipo: " + storageType + ")");

                    // Filtra por tipos comuns de disco em Linux
                    // Tipo .4 = fixed disk (padrão em Linux também)
                    // Alternativamente, filtra por descrição contendo / (mount points Linux)
                    // Também pula tipos de memória (1.1 = other, 1.2 = RAM, 1.3 = virtual)
                    String typeStr = storageType.trim();
                    
                    // Se é memória física (Physical memory, Virtual memory, etc), pula
                    if (typeStr.endsWith(".1") || typeStr.endsWith(".2") || typeStr.endsWith(".3")) {
                        System.out.println("   ⏭️  Pulando (é memória, não disco)");
                        continue;
                    }
                    
                    // Se não começa com "/" e não é disco fixo (.4), pula
                    if (!description.startsWith("/") && !storageType.endsWith(".4")) {
                        System.out.println("   ⏭️  Pulando (descrição não é mount point e tipo não é .4)");
                        continue;
                    }

                    diskEntriesFound++;

                    // Coleta métricas do disco
                    String totalUnits = getAsString(OID_HR_STORAGE_SIZE + "." + i);
                    String usedUnits = getAsString(OID_HR_STORAGE_USED + "." + i);
                    String unitSize = getAsString(OID_HR_STORAGE_UNITS + "." + i);

                    if (totalUnits != null && !totalUnits.contains("noSuch")
                            && usedUnits != null && !usedUnits.contains("noSuch")
                            && unitSize != null && !unitSize.contains("noSuch")) {

                        try {
                            long total = Long.parseLong(totalUnits.trim());
                            long used = Long.parseLong(usedUnits.trim());
                            long unit = Long.parseLong(unitSize.trim());

                            // Converte para GB (total em unidades, cada unidade = unit bytes)
                            long totalBytes = total * unit;
                            long usedBytes = used * unit;
                            long availableBytes = totalBytes - usedBytes;
                            
                            long totalGB = totalBytes / (1024 * 1024 * 1024);
                            long usedGB = usedBytes / (1024 * 1024 * 1024);
                            long availableGB = availableBytes / (1024 * 1024 * 1024);

                            // Só adiciona se tiver tamanho válido (pelo menos 1 MB)
                            if (totalBytes > (1024 * 1024)) {
                                com.victorqueiroga.serverwatch.dto.DiskInfoDto disk = new com.victorqueiroga.serverwatch.dto.DiskInfoDto();
                                disk.setPath(description);
                                disk.setDescription(description + " filesystem");
                                disk.setTotalGB(totalGB);
                                disk.setUsedGB(usedGB);
                                disk.setAvailableGB(availableGB);
                                disk.setType("Linux Filesystem (HR-MIB)");
                                disk.calculateUsagePercent();

                                diskList.add(disk);
                                System.out.println("   ✅ Disco adicionado: " + totalGB + " GB total, " + usedGB + " GB usado, " + availableGB + " GB disponível");
                            } else {
                                System.out.println("   ⏭️  Pulando (tamanho muito pequeno: " + totalBytes + " bytes)");
                            }
                        } catch (NumberFormatException nfe) {
                            System.out.println("   ⚠️ Erro ao parsear números: " + nfe.getMessage());
                        }
                    }

                } catch (Exception e) {
                    // Ignora erros de índices individuais
                }
            }

            System.out.println("📈 Total de entries de storage encontradas: " + storageEntriesFound);
            System.out.println("📈 Total de entries candidatas a disco: " + diskEntriesFound);
            System.out.println("📈 Total de discos coletados (Host Resources MIB): " + diskList.size());
            
            // Se ainda não encontrou discos, log diagnóstico
            if (diskList.isEmpty() && storageEntriesFound > 0) {
                System.out.println("⚠️  DIAGNÓSTICO: Host Resources MIB encontrou " + storageEntriesFound 
                    + " storage entries, mas nenhuma corresponde a disco. "
                    + "O SNMP agent deste servidor pode não ter filesystem info configurada.");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao coletar discos Linux via Host Resources: " + e.getMessage());
        }

        return diskList;
    }

    /**
     * Extrai a letra do drive da descrição do Windows (ex: "C:\" de "C:\
     * Label:System Serial Number 123456")
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

    /**
     * Realiza um SNMP Walk (sequência de GET NEXT) a partir de um OID base.
     * Útil para iterar tabelas como hrProcessorLoad.
     *
     * * @param oidBase OID raiz da tabela (ex: 1.3.6.1.2.1.25.3.3.1.2)
     * @return Uma lista de VariableBindings (OID e Valor) encontrados.
     */
    public List<VariableBinding> snmpWalk(String oidBase) throws Exception {
        List<VariableBinding> resultList = new ArrayList<>();
        CommunityTarget<UdpAddress> target = createTarget();

        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        transport.listen();

        try {
            OID currentOid = new OID(oidBase);
            boolean finished = false;

            while (!finished) {
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(currentOid));
                pdu.setType(PDU.GETNEXT);

                ResponseEvent<UdpAddress> responseEvent = snmp.send(pdu, target);

                if (responseEvent == null || responseEvent.getResponse() == null) {
                    throw new RuntimeException("SNMP Timeout durante WALK.");
                }

                PDU response = responseEvent.getResponse();
                VariableBinding vb = response.get(0);

                // 1. Verificar fim da MIB ou erro
                if (response.getErrorStatus() != 0 || vb.getVariable().toString().contains("noSuch")) {
                    finished = true;
                    continue;
                }
                OID oidString = new OID(oidBase);

                // 2. Verificar se o novo OID ainda está sob a OID base
                if (vb.getOid().startsWith(oidString)) {
                    resultList.add(vb);
                    // 3. Preparar para o próximo GETNEXT
                    currentOid = vb.getOid();
                } else {
                    finished = true; // Saiu do escopo do WALK
                }
            }
        } finally {
            try {
                snmp.close();
            } catch (Exception e) {
            }
        }

        if (resultList.isEmpty()) {
            throw new RuntimeException("SNMP Walk não retornou resultados válidos para OID: " + oidBase);
        }

        return resultList;
    }
}
