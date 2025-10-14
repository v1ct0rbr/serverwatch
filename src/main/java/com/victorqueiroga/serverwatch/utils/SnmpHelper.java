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
    
    // CPU Load OIDs - Host Resources MIB (mais universal que Net-SNMP)
    public static final String OID_CPU_LOAD_1MIN = "1.3.6.1.4.1.2021.10.1.3.1";  // 1 min load average
    public static final String OID_CPU_LOAD_5MIN = "1.3.6.1.4.1.2021.10.1.3.2";  // 5 min load average
    public static final String OID_CPU_LOAD_15MIN = "1.3.6.1.4.1.2021.10.1.3.3"; // 15 min load average
    public static final String OID_HR_PROCESSOR_LOAD = "1.3.6.1.2.1.25.3.3.1.2"; // Host Resources CPU Load
    
    // Memory OIDs - Net-SNMP específico (Linux/Unix)
    public static final String OID_MEM_TOTAL_REAL = "1.3.6.1.4.1.2021.4.5.0";    // Total Real Memory (KB)
    public static final String OID_MEM_AVAIL_REAL = "1.3.6.1.4.1.2021.4.6.0";    // Available Real Memory (KB)
    public static final String OID_MEM_USED_REAL = "1.3.6.1.4.1.2021.4.14.0";    // Used Real Memory (KB)
    public static final String OID_MEM_FREE_REAL = "1.3.6.1.4.1.2021.4.11.0";    // Free Real Memory (KB)
    
    // Disk/Storage OIDs - Net-SNMP
    public static final String OID_DISK_PATH = "1.3.6.1.4.1.2021.9.1.2";        // Disk Path
    public static final String OID_DISK_TOTAL = "1.3.6.1.4.1.2021.9.1.6";       // Total Disk Size (KB)
    public static final String OID_DISK_AVAIL = "1.3.6.1.4.1.2021.9.1.7";       // Available Disk (KB)
    public static final String OID_DISK_USED = "1.3.6.1.4.1.2021.9.1.8";        // Used Disk (KB)
    public static final String OID_DISK_PERCENT = "1.3.6.1.4.1.2021.9.1.9";     // Disk Usage %
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
        snmp.close();

        // Verificando resposta
        if (responseEvent != null && responseEvent.getResponse() != null) {
            VariableBinding vb = responseEvent.getResponse().get(0);
            return vb.getVariable().toString();
        } else {
            throw new RuntimeException("SNMP Timeout ou sem resposta para OID: " + oid);
        }
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
     * Obtém o load average de 1 minuto
     */
    public String getCpuLoad1Min() throws Exception {
        return getAsString(OID_CPU_LOAD_1MIN);
    }
    
    /**
     * Obtém a memória total em KB
     */
    public String getMemoryTotal() throws Exception {
        return getAsString(OID_MEM_TOTAL_REAL);
    }
    
    /**
     * Obtém a memória disponível em KB
     */
    public String getMemoryAvailable() throws Exception {
        return getAsString(OID_MEM_AVAIL_REAL);
    }
    
    /**
     * Obtém a memória usada em KB
     */
    public String getMemoryUsed() throws Exception {
        return getAsString(OID_MEM_USED_REAL);
    }
    
    /**
     * Obtém o número de interfaces de rede
     */
    public String getInterfaceCount() throws Exception {
        return getAsString(OID_IF_NUMBER);
    }
}
