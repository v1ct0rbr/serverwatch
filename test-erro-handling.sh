#!/bin/bash

# Script de diagnóstico para testar o tratamento de erros

set -e

cd "$(dirname "$0")" || exit

SERVER="http://localhost:8080"
echo "🧪 Iniciando testes de erro handling..."
echo "📍 Servidor: $SERVER"
echo ""

# Função para fazer requisição e verificar status
test_endpoint() {
    local url=$1
    local expected_status=$2
    local description=$3
    
    echo "🔍 Testando: $description"
    echo "   URL: $url"
    
    response=$(curl -s -w "%{http_code}" "$url" -o /dev/null 2>/dev/null || echo "000")
    
    if [ "$response" = "$expected_status" ]; then
        echo "   ✅ Status: $response (Esperado)"
    else
        echo "   ❌ Status: $response (Esperado: $expected_status)"
    fi
    echo ""
}

# Aguarda o servidor iniciar
echo "⏳ Aguardando servidor iniciar..."
for i in {1..30}; do
    if curl -s "$SERVER" > /dev/null 2>&1; then
        echo "✅ Servidor está online"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ Servidor não respondeu após 30 segundos"
        exit 1
    fi
    sleep 1
done
echo ""

# Testes
test_endpoint "$SERVER" "200" "GET / (Home)"
test_endpoint "$SERVER/pagina-inexistente" "404" "GET /pagina-inexistente (404)"
test_endpoint "$SERVER/example-error/validate/abc" "400" "GET /example-error/validate/abc (BusinessException)"
test_endpoint "$SERVER/example-error/port/70000" "400" "GET /example-error/port/70000 (Range Error)"
test_endpoint "$SERVER/example-error/unhandled-error" "500" "GET /example-error/unhandled-error (500)"

echo "✅ Testes concluídos!"
echo ""
echo "💡 Próximas verificações:"
echo "   1. Acesse $SERVER/pagina-inexistente no navegador"
echo "   2. Verifique se o template error.html é renderizado com status 404"
echo "   3. Verifique se as cores e ícones aparecem corretamente"
echo "   4. Verifique os logs da aplicação para mensagens de erro"
