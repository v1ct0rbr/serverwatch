package com.victorqueiroga.serverwatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiskInfoDto {

    public static final long CRITICAL_USAGE_THRESHOLD = 90L;    // Limite de uso critico em porcentagem
    private String path;        // Caminho do disco (ex: /, C:, /var)
    private String description; // Descrição do disco
    private Long totalGB;       // Total em GB
    private Long usedGB;        // Usado em GB
    private Long availableGB;   // Disponível em GB
    private Double usagePercent; // Percentual de uso
    private String type;        // Tipo do disco (Fixed Disk, etc)

    private Boolean isCritical;

    /**
     * Calcula o percentual de uso automaticamente
     */
    public void calculateUsagePercent() {
        if (totalGB != null && totalGB > 0 && usedGB != null) {
            this.usagePercent = (double) usedGB / totalGB * 100;
        } else {
            this.usagePercent = 0.0;
        }
    }

    public String verifyCriticalDiskLetter() {
        if (this.usagePercent != null && this.usagePercent >= CRITICAL_USAGE_THRESHOLD) {
            return this.path;
        } else {
            return null;
        }
    }
}
