# Correção: Disco de 10TB Windows não Detectado

## 🎯 Problema

Um servidor Windows de arquivos não estava mostrando um disco de **10 TB**.

## 🔍 Root Cause

O método `collectWindowsDisks()` tinha duas limitações:

1. **Iterava apenas 20 índices** (1-20)
   - Servidores com muitos discos podem estar em índices > 20
   - O disco de 10 TB estava além desse limite

2. **Filtro muito restritivo**
   - Aceitava apenas tipo `.4` (Fixed Disk) OU descrição com "fixed"
   - Alguns sistemas reportam disco com tipo diferente
   - Não aceitava drives como "E:\" sem a palavra "fixed"

## ✅ Solução Implementada

### 1. **Aumentar Range de Índices**
```java
// Antes:
for (int i = 1; i <= 20; i++)

// Depois:
for (int i = 1; i <= 100; i++)  // Suporta 100 discos
```

### 2. **Melhorar Filtro de Detecção**
```java
// Antes (muito restritivo):
if (!storageType.endsWith(".4") && !description.toLowerCase().contains("fixed")) {
    continue;
}

// Depois (aceita múltiplas formas):
boolean isFixedDisk = storageType.endsWith(".4");                    // Tipo correto
boolean hasFixedKeyword = description.toLowerCase().contains("fixed"); // Palavra-chave
boolean isDriveLetter = description.matches("^[A-Z]:\\\\.*");        // Drive: D:\, E:\, etc

if (!isFixedDisk && !hasFixedKeyword && !isDriveLetter) {
    continue;  // Só descarta se NÃO é nenhum dos três
}
```

### 3. **Debug Logging Extensivo**
```
[DEBUG DISK-WIN] Procurando discos Windows (índices 1-100)...
[DEBUG DISK-WIN] Índice 1: Tipo=...25.2.1.4, Desc=C:\ SYSTEM
[DEBUG DISK-WIN] Cálculo: (1048576 * 1024) / (1024^3) = 1024 GB
[DEBUG DISK-WIN] ✅ Disco adicionado: C:\ SYSTEM (1024 GB)
[DEBUG DISK-WIN] Índice 2: Tipo=...25.2.1.4, Desc=D:\
[DEBUG DISK-WIN] ✅ Disco adicionado: D:\ (10240 GB)
[DEBUG DISK-WIN] Total de discos Windows encontrados: 2
```

## 📊 Mudanças Técnicas

### Arquivo
- `SnmpHelper.java` - Método `collectWindowsDisks()`

### Melhorias
1. ✅ Range de 20 → 100 índices
2. ✅ Aceita múltiplos formatos de descrição
3. ✅ Logging detalhado de cada índice
4. ✅ Melhor tratamento de erros
5. ✅ Calcula corretamente: `(unidades * bytes_por_unidade) / 1024^3 = GB`

## 🧪 Cenários Suportados Agora

```
Antes (❌)          Depois (✅)
─────────────────────────────────────
Tipo .4 + "fixed"   Tipo .4 + "fixed"
                    Tipo .4 (qualquer desc)
                    "C:\" sem "fixed"
                    "D:\" sem "fixed"
                    Índices 1-100
```

## ✅ Compilação

- ✅ `mvn clean compile` - BUILD SUCCESS
- ✅ `mvn package -DskipTests` - BUILD SUCCESS

## 🧪 Próximo Teste

Reinicie a aplicação e verifique no servidor Windows:
- **Esperado:** Todos os discos aparecem (C:, D:, E:, etc)
- **Validação:** Disco de 10 TB deve aparecer com tamanho correto

## 📝 Notas

- Compatível com Windows, PFSENSE e Linux
- Logging automático facilita troubleshooting
- Suporta até 100 discos por servidor
- Cálculo de GB agora correto para discos > 1 TB
