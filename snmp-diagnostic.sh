#!/bin/bash
# SNMP Diagnostic Script for Linux Disk Collection
# Run this on your Linux servers to diagnose SNMP availability for disk information

echo "==========================================="
echo "SNMP Disk Information Diagnostic"
echo "==========================================="
echo ""

# Check if snmpwalk is installed
if ! command -v snmpwalk &> /dev/null; then
    echo "❌ snmpwalk não está instalado. Instale com:"
    echo "   Ubuntu/Debian: sudo apt-get install snmp"
    echo "   CentOS/RHEL: sudo yum install net-snmp-utils"
    exit 1
fi

# Get local IP (assuming eth0, adjust if needed)
LOCAL_IP=$(hostname -I | awk '{print $1}')
COMMUNITY="public"  # Adjust if your SNMP community is different

echo "ℹ️  Testando SNMP em: $LOCAL_IP"
echo "ℹ️  Community: $COMMUNITY"
echo ""

echo "--- Teste 1: UCD-MIB (Net-SNMP Disk OIDs) ---"
echo "Tentando coletar via 1.3.6.1.4.1.2021.9.1.2 (disk paths):"
snmpwalk -v 2c -c $COMMUNITY $LOCAL_IP 1.3.6.1.4.1.2021.9.1.2 2>/dev/null | head -20
if [ $? -ne 0 ]; then
    echo "❌ UCD-MIB não disponível"
else
    echo "✅ UCD-MIB disponível"
fi

echo ""
echo "--- Teste 2: Host Resources MIB Storage Table ---"
echo "Tentando coletar via 1.3.6.1.2.1.25.2.3.1 (storage info):"
snmpwalk -v 2c -c $COMMUNITY $LOCAL_IP 1.3.6.1.2.1.25.2.3.1.3 2>/dev/null | head -20
if [ $? -ne 0 ]; then
    echo "❌ Host Resources MIB não disponível"
else
    echo "✅ Host Resources MIB disponível"
fi

echo ""
echo "--- Teste 3: Mount points do sistema (para referência) ---"
df -h | grep -E "^/dev" | awk '{print $1, $2, $3, $4}'

echo ""
echo "==========================================="
echo "Se os testes 1 e 2 falharem, você precisa:"
echo "1. Instalar Net-SNMP com suporte MIB:"
echo "   Ubuntu/Debian: sudo apt-get install snmp snmp-mibs-downloader"
echo "   CentOS/RHEL: sudo yum install net-snmp net-snmp-utils net-snmp-libs"
echo ""
echo "2. Editar /etc/snmp/snmpd.conf e adicionar:"
echo "   sysdescr Linux Server"
echo "   rocommunity public  127.0.0.1"
echo "   rocommunity public"
echo "   disk / 10000"
echo "   disk /home 5000"
echo "   disk /var 5000"
echo ""
echo "3. Reiniciar SNMP daemon:"
echo "   sudo systemctl restart snmpd"
echo "==========================================="
