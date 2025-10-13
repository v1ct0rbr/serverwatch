#!/bin/bash

echo "=== DIAGNÓSTICO DE CLIENT KEYCLOAK ==="
echo "Realm: DERPB"
echo "Base URL: https://keycloak.derpb.com.br"
echo

# Lista de client IDs para testar
CLIENTS=(
    "serverwatch-client"
    "DERPB-realm"
    "derpb-client"
    "serverwatch"
    "account-console"
    "admin-cli"
    "security-admin-console"
)

echo "🔍 Testando conectividade com o realm DERPB..."
REALM_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "https://keycloak.derpb.com.br/realms/DERPB")
echo "Status do realm DERPB: $REALM_STATUS"

if [ "$REALM_STATUS" = "200" ]; then
    echo "✅ Realm DERPB acessível"
else
    echo "❌ Realm DERPB não acessível - Status: $REALM_STATUS"
fi

echo
echo "🔑 Clientes sugeridos para testar:"
for client in "${CLIENTS[@]}"; do
    echo "   - $client"
done

echo
echo "📋 PRÓXIMAS AÇÕES:"
echo "1. Acesse: https://keycloak.derpb.com.br/admin"
echo "2. Vá para realm 'DERPB'"
echo "3. Menu 'Clients'"
echo "4. Verifique se existe algum destes clientes:"
for client in "${CLIENTS[@]}"; do
    echo "   ✓ $client"
done

echo
echo "5. Se não existir, crie um novo cliente:"
echo "   - Client ID: serverwatch-client"
echo "   - Client Type: OpenID Connect"
echo "   - Client authentication: ON"
echo "   - Valid redirect URIs: http://localhost:8080/*"

echo
echo "6. Após criar, copie o Client Secret e atualize o .env"

echo
echo "=== FIM DO DIAGNÓSTICO ==="