#!/bin/bash

# Script para verificar clientes do Keycloak no realm DERPB
# Este script serve para identificar clientes existentes que possam ser utilizados

echo "=== Verificação do Keycloak - Realm DERPB ==="
echo

# 1. Testar conectividade básica
echo "1. Testando conectividade com o servidor Keycloak..."
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "https://keycloak.derpb.com.br")
echo "Status HTTP: $HTTP_STATUS"
echo

# 2. Verificar se o realm DERPB existe
echo "2. Verificando realm DERPB..."
REALM_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "https://keycloak.derpb.com.br/realms/DERPB")
echo "Status do Realm: $REALM_STATUS"
echo

# 3. Obter configuração OpenID Connect
echo "3. Obtendo configuração OpenID Connect..."
curl -s "https://keycloak.derpb.com.br/realms/DERPB/.well-known/openid_configuration" > /tmp/openid-config.json

if [ -s /tmp/openid-config.json ]; then
    echo "✓ Configuração OpenID Connect obtida com sucesso"
    echo "Issuer: $(cat /tmp/openid-config.json | grep -o '"issuer":"[^"]*"' | cut -d'"' -f4)"
    echo "Authorization Endpoint: $(cat /tmp/openid-config.json | grep -o '"authorization_endpoint":"[^"]*"' | cut -d'"' -f4)"
    echo "Token Endpoint: $(cat /tmp/openid-config.json | grep -o '"token_endpoint":"[^"]*"' | cut -d'"' -f4)"
else
    echo "✗ Falha ao obter configuração OpenID Connect"
fi
echo

# 4. Instruções para acessar o Admin Console
echo "4. Próximos passos para configurar o cliente:"
echo
echo "Para criar o cliente 'serverwatch-client' no Keycloak:"
echo "1. Acesse: https://keycloak.derpb.com.br/admin"
echo "2. Faça login com credenciais de administrador"
echo "3. Selecione o realm 'DERPB'"
echo "4. Vá para 'Clients' no menu lateral"
echo "5. Clique em 'Create client'"
echo "6. Configure:"
echo "   - Client ID: serverwatch-client"
echo "   - Client authentication: ON"
echo "   - Authorization: OFF (a menos que precise de autorização avançada)"
echo "   - Valid redirect URIs: http://localhost:8080/*, https://seu-dominio.com/*"
echo "   - Web origins: http://localhost:8080, https://seu-dominio.com"
echo
echo "7. Após criar, vá para a aba 'Credentials' e copie o 'Client secret'"
echo "8. Atualize o arquivo .env com o client secret obtido"

# 5. Verificar se existem clientes públicos que podem ser usados temporariamente
echo
echo "5. Alternativa temporária:"
echo "Se não conseguir criar o cliente, verifique se existe algum cliente público"
echo "no realm DERPB que possa ser usado temporariamente para testes."
echo
echo "Clientes públicos comuns:"
echo "- account-console"
echo "- admin-cli"
echo "- security-admin-console"
echo

echo "=== Fim da Verificação ==="