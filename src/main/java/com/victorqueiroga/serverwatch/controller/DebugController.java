package com.victorqueiroga.serverwatch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.victorqueiroga.serverwatch.utils.SnmpHelper;

/**
 * Debug controller para diagnóstico de SNMP
 * Apenas para desenvolvimento/debug
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/snmp/{ip}/{community}")
    public String diagnosticSnmp(@PathVariable String ip, @PathVariable String community) {
        try {
            SnmpHelper snmp = new SnmpHelper(ip, community);
            snmp.diagnosticDiskCollection();
            return "✅ Diagnóstico executado. Verifique os logs do servidor.";
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }

    @GetMapping("/disks/{ip}/{community}")
    public String testDiskCollection(@PathVariable String ip, @PathVariable String community) {
        try {
            SnmpHelper snmp = new SnmpHelper(ip, community);
            var disks = snmp.getAllDisks();
            
            StringBuilder sb = new StringBuilder();
            sb.append("✅ Total de discos coletados: ").append(disks.size()).append("\n\n");
            
            for (var disk : disks) {
                sb.append("📀 ").append(disk.getPath()).append("\n");
                sb.append("   Total: ").append(disk.getTotalGB()).append(" GB\n");
                sb.append("   Usado: ").append(disk.getUsedGB()).append(" GB\n");
                sb.append("   Disponível: ").append(disk.getAvailableGB()).append(" GB\n");
                sb.append("   Uso: ").append(disk.getUsagePercent()).append("%\n");
                sb.append("   Tipo: ").append(disk.getType()).append("\n\n");
            }
            
            return sb.toString();
        } catch (Exception e) {
            return "❌ Erro: " + e.getMessage();
        }
    }
}
