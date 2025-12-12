# Diagnóstico: Problema de Leitura de Memória RAM no PFSENSE

## Problema Relatado
"Estou com problemas para ler dados do PFSENSE. Consigo ler várias informações, mas para a memória ram os dados estão indisponíveis"

## Análise da Causa

### 1. PFSENSE é baseado em FreeBSD
- **Sistema Operacional**: FreeBSD (não é Linux, não é Windows)
- **Implicação**: As OIDs específicas para Linux (Net-SNMP) NÃO funcionam no PFSENSE
- **Implicação**: As OIDs do Windows (Host Resources) podem não funcionar corretamente

### 2. OIDs Atualmente Configuradas no Código

#### Para Memória Total (Linux):
```
OID_MEM_TOTAL_REAL = "1.3.6.1.4.1.2021.4.5.0"  ❌ Não funciona em PFSENSE
```

#### Para Memória Usada (Linux):
```
OID_MEM_USED_REAL = "1.3.6.1.4.1.2021.4.14.0"  ❌ Não funciona em PFSENSE
```

#### Para Memória Disponível (Linux):
```
OID_MEM_AVAIL_REAL = "1.3.6.1.4.1.2021.4.6.0"  ❌ Não funciona em PFSENSE
```

#### Para Memória Livre (Linux):
```
OID_MEM_FREE_REAL = "1.3.6.1.4.1.2021.4.11.0"  ❌ Não funciona em PFSENSE
```

### 3. Por que isso acontece?
As OIDs com prefixo `1.3.6.1.4.1.2021` (NET-SNMP) são específicas de agentes SNMP instalados em sistemas Unix/Linux. 

**PFSENSE por padrão:**
- Usa o SNMP nativo do FreeBSD
- Não tem o agente NET-SNMP instalado
- Não suporta essas OIDs de memória

## Solução

### Opção 1: Usar Host Resources MIB (RFC 2790) - ✅ RECOMENDADO
**Compatibilidade**: Funciona em PFSENSE, Windows, Linux

```
HOST RESOURCES MIB (Standard)
1.3.6.1.2.1.25.2.3.1.2  - Storage Type
1.3.6.1.2.1.25.2.3.1.4  - Storage Units
1.3.6.1.2.1.25.2.3.1.5  - Storage Size (Total)
1.3.6.1.2.1.25.2.3.1.6  - Storage Used

Type para RAM = 1.3.6.1.2.1.25.2.1.2
```

**Como funciona:**
1. Itera pelos índices de storage (1, 2, 3, ...)
2. Verifica se o tipo é RAM (1.3.6.1.2.1.25.2.1.2)
3. Coleta Units, Size e Used para aquele índice
4. Calcula: Total = Units × Size, Usado = Units × Used

### Opção 2: Instalar NET-SNMP em PFSENSE
Se você tem controle sobre o PFSENSE e quer usar as OIDs NET-SNMP:

```bash
# Acessar PFSENSE via SSH
ssh admin@<pfsense-ip>

# Instalar net-snmp
pkg install net-snmp
```

**Desvantagem**: Adiciona complexidade, requer manutenção

### Opção 3: Usar OIDs Específicas do PFSENSE
PFSENSE também expõe algumas métricas via:
```
UCD-SNMP-MIB (compatível com NET-SNMP)
Mesmo que você não tenha instalado NET-SNMP, pode estar disponível
```

## Implementação Recomendada

Modificar o `SnmpHelper.java` para:

1. **Detectar PFSENSE** (por system description)
2. **Para PFSENSE**, usar exclusivamente Host Resources MIB
3. **Fallback para** Host Resources MIB para outros sistemas também

### Detecção de PFSENSE

```java
public boolean isPfSenseSystem() throws Exception {
    if (isPfsense == null) {
        String sysDescr = getAsString(OID_SYS_DESCR).toLowerCase();
        isPfsense = sysDescr.contains("pfsense") || 
                    sysDescr.contains("freebsd") ||
                    sysDescr.contains("netgate");
    }
    return isPfsense;
}
```

### Método de Coleta para PFSENSE

```java
public String getPfSenseMemoryTotal() throws Exception {
    // Usa Host Resources MIB
    // Procura por storage do tipo RAM (1.3.6.1.2.1.25.2.1.2)
    return getHostResourcesMemoryTotal();
}
```

## Passo a Passo para Verificar

### 1. Testar OIDs via SNMP direto

```bash
# Substituir <pfsense-ip> e <community> pelos valores corretos

# Testar System Description
snmpget -v 2c -c <community> <pfsense-ip> .1.3.6.1.2.1.1.1.0

# Testar Host Resources - Storage Type no índice 1
snmpget -v 2c -c <community> <pfsense-ip> .1.3.6.1.2.1.25.2.3.1.2.1

# Listar todos os storages disponíveis
snmpwalk -v 2c -c <community> <pfsense-ip> .1.3.6.1.2.1.25.2.3.1.2

# Detalhado: Units, Size, Used para cada storage
snmpwalk -v 2c -c <community> <pfsense-ip> .1.3.6.1.2.1.25.2.3.1
```

### 2. Interpretar Resultados

Se você ver algo como:

```
.1.3.6.1.2.1.25.2.3.1.2.1 = OID: .1.3.6.1.2.1.25.2.1.2    ✅ RAM encontrada
.1.3.6.1.2.1.25.2.3.1.4.1 = INTEGER: 1024                  ✅ Units (bytes)
.1.3.6.1.2.1.25.2.3.1.5.1 = INTEGER: 2000000               ✅ Size (em units)
.1.3.6.1.2.1.25.2.3.1.6.1 = INTEGER: 1000000               ✅ Used (em units)
```

**Cálculo:**
- Total = 1024 × 2000000 = 2,048,000,000 bytes = 2,048 MB = 2 GB
- Usado = 1024 × 1000000 = 1,024,000,000 bytes = 1,024 MB = 1 GB
- Disponível = 2 GB - 1 GB = 1 GB

## Código Atual vs. Esperado

### ❌ Código Atual em `getMemoryTotal()`
```java
if (isWindowsSystem()) {
    return getWindowsMemoryFromHostResources("total");
} else {
    // ❌ Tenta Net-SNMP, falha em PFSENSE
    return getAsString(OID_MEM_TOTAL_REAL);  
}
```

### ✅ Código Esperado
```java
if (isPfSenseSystem()) {
    return getHostResourcesMemoryTotal();  // Host Resources MIB
} else if (isWindowsSystem()) {
    return getWindowsMemoryFromHostResources("total");
} else {
    try {
        // Tenta Net-SNMP (Linux)
        return getAsString(OID_MEM_TOTAL_REAL);
    } catch (Exception e) {
        // Fallback para Host Resources MIB
        return getHostResourcesMemoryTotal();
    }
}
```

## Estrutura Host Resources MIB

```
1.3.6.1.2.1.25.2        - Storage group
  .3.1.2.<index>        - Storage Type
  .3.1.3.<index>        - Storage Description
  .3.1.4.<index>        - Storage Allocation Units (bytes)
  .3.1.5.<index>        - Storage Size (in units)
  .3.1.6.<index>        - Storage Used (in units)
  .3.1.7.<index>        - Storage Allocation Failures

Storage Type OIDs:
1.3.6.1.2.1.25.2.1.1   - Other
1.3.6.1.2.1.25.2.1.2   - RAM 
1.3.6.1.2.1.25.2.1.3   - Virtual Memory
1.3.6.1.2.1.25.2.1.4   - Fixed Disk
1.3.6.1.2.1.25.2.1.5   - Removable Disk
1.3.6.1.2.1.25.2.1.6   - Floppy Disk
1.3.6.1.2.1.25.2.1.7   - Compact Disc
1.3.6.1.2.1.25.2.1.8   - RAM Disc
1.3.6.1.2.1.25.2.1.9   - Flash Memory
1.3.6.1.2.1.25.2.1.10  - Network Disc
```

## Próximas Ações

1. ✅ Executar testes SNMP conforme "Passo a Passo para Verificar" acima
2. ⏳ Modificar `SnmpHelper.java` para adicionar suporte PFSENSE
3. ⏳ Testar integração com PFSENSE
4. ⏳ Validar coleta de memória RAM

---

**Resumo Executivo**: O PFSENSE não suporta as OIDs NET-SNMP (Linux) atualmente configuradas. A solução é usar **Host Resources MIB**, que é universal e funciona em PFSENSE, Windows e Linux.
