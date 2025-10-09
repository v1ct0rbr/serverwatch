#!/bin/bash

# ====================================
# ServerWatch - Keycloak Setup Script
# ====================================

echo "🚀 Configurando integração Keycloak para ServerWatch..."
echo

# Verificar se o arquivo .env existe
if [ ! -f ".env" ]; then
    echo "📋 Criando arquivo .env a partir do .env.example..."
    cp .env.example .env
    echo "✅ Arquivo .env criado!"
else
    echo "📋 Arquivo .env já existe."
fi

echo
echo "🔧 Configurações necessárias:"
echo "============================================="
echo

# Solicitar configurações do Keycloak
read -p "🌐 URL do servidor Keycloak (ex: https://keycloak.example.com): " KEYCLOAK_URL
read -p "🏠 Nome do Realm (ex: serverwatch): " REALM_NAME
read -p "🆔 Client ID (ex: serverwatch-client): " CLIENT_ID
read -p "🔐 Client Secret: " CLIENT_SECRET

# URL da aplicação
read -p "🌍 URL da aplicação (ex: http://localhost:8080): " APP_URL

# Porta da aplicação
read -p "🔌 Porta da aplicação (padrão: 8080): " SERVER_PORT
SERVER_PORT=${SERVER_PORT:-8080}

echo
echo "💾 Configurações de banco de dados:"
read -p "🏢 Host do banco (padrão: localhost): " DB_HOST
DB_HOST=${DB_HOST:-localhost}

read -p "🔌 Porta do banco (padrão: 5432): " DB_PORT
DB_PORT=${DB_PORT:-5432}

read -p "📊 Nome do banco (padrão: serverwatch_db): " DB_NAME
DB_NAME=${DB_NAME:-serverwatch_db}

read -p "👤 Usuário do banco (padrão: serverwatch_user): " DB_USER
DB_USER=${DB_USER:-serverwatch_user}

read -p "🔐 Senha do banco: " DB_PASSWORD

echo
echo "📝 Atualizando arquivo .env..."

# Atualizar o arquivo .env
cat > .env << EOF
# ===========================================
# DATABASE CONFIGURATION
# ===========================================
DATABASE_HOST=${DB_HOST}
DATABASE_PORT=${DB_PORT}
DATABASE_USER=${DB_USER}
DATABASE_PASSWORD=${DB_PASSWORD}
DATABASE_NAME=${DB_NAME}

# ===========================================
# KEYCLOAK CONFIGURATION
# ===========================================
KEYCLOAK_AUTH_SERVER_URL=${KEYCLOAK_URL}
KEYCLOAK_REALM=${REALM_NAME}
KEYCLOAK_CLIENT_ID=${CLIENT_ID}
KEYCLOAK_CLIENT_SECRET=${CLIENT_SECRET}

# ===========================================
# KEYCLOAK ADMIN API CONFIGURATION
# ===========================================
KEYCLOAK_ADMIN_CLIENT_ID=${CLIENT_ID}-admin
KEYCLOAK_ADMIN_CLIENT_SECRET=change-me-in-production

# ===========================================
# APPLICATION CONFIGURATION
# ===========================================
APP_BASE_URL=${APP_URL}
SERVER_PORT=${SERVER_PORT}

# ===========================================
# SECURITY CONFIGURATION
# ===========================================
JWT_ACCESS_TOKEN_VALIDITY=3600
JWT_REFRESH_TOKEN_VALIDITY=86400
SESSION_TIMEOUT=1800

# ===========================================
# LOGGING CONFIGURATION
# ===========================================
LOGGING_LEVEL_SECURITY=INFO
LOGGING_LEVEL_KEYCLOAK=DEBUG

# ===========================================
# DEVELOPMENT/PRODUCTION FLAGS
# ===========================================
SPRING_PROFILES_ACTIVE=local
DEBUG_OAUTH2=true
ENABLE_SSL=false
EOF

echo "✅ Arquivo .env atualizado com sucesso!"
echo

echo "🔐 Configuração do Client no Keycloak:"
echo "============================================="
echo "1. Acesse o Keycloak Admin Console: ${KEYCLOAK_URL}/admin"
echo "2. Selecione o realm: ${REALM_NAME}"
echo "3. Vá em Clients → Create Client"
echo "4. Configure:"
echo "   - Client ID: ${CLIENT_ID}"
echo "   - Client Type: OpenID Connect"
echo "   - Client authentication: ON"
echo
echo "5. Na aba Settings, configure:"
echo "   - Valid redirect URIs: ${APP_URL}/login/oauth2/code/keycloak"
echo "   - Valid post logout redirect URIs: ${APP_URL}/login?logout=true"
echo "   - Web origins: ${APP_URL}"
echo
echo "6. Na aba Credentials, copie o Client Secret e atualize o arquivo .env"

echo
echo "👥 Configuração de Roles:"
echo "============================================="
echo "Crie as seguintes roles no realm:"
echo "   - USER (usuário padrão)"
echo "   - ADMIN (administrador)"
echo

echo "🚦 Para testar a integração:"
echo "============================================="
echo "1. Execute: ./mvnw spring-boot:run"
echo "2. Acesse: ${APP_URL}"
echo "3. Clique em 'Entrar com Keycloak'"
echo "4. Faça login no Keycloak"
echo

echo "📚 Documentação completa:"
echo "Consulte o arquivo KEYCLOAK_SETUP.md para mais detalhes."
echo

echo "🎉 Configuração concluída!"
echo "Não se esqueça de configurar o client no Keycloak Admin Console."