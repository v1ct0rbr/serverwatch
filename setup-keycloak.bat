@echo off
rem ====================================
rem ServerWatch - Keycloak Setup Script
rem ====================================

echo 🚀 Configurando integração Keycloak para ServerWatch...
echo.

rem Verificar se o arquivo .env existe
if not exist ".env" (
    echo 📋 Criando arquivo .env a partir do .env.example...
    copy .env.example .env > nul
    echo ✅ Arquivo .env criado!
) else (
    echo 📋 Arquivo .env já existe.
)

echo.
echo 🔧 Configurações necessárias:
echo =============================================
echo.

rem Solicitar configurações do Keycloak
set /p KEYCLOAK_URL="🌐 URL do servidor Keycloak (ex: https://keycloak.example.com): "
set /p REALM_NAME="🏠 Nome do Realm (ex: serverwatch): "
set /p CLIENT_ID="🆔 Client ID (ex: serverwatch-client): "
set /p CLIENT_SECRET="🔐 Client Secret: "

rem URL da aplicação
set /p APP_URL="🌍 URL da aplicação (ex: http://localhost:8080): "

rem Porta da aplicação
set /p SERVER_PORT="🔌 Porta da aplicação (padrão: 8080): "
if "%SERVER_PORT%"=="" set SERVER_PORT=8080

echo.
echo 💾 Configurações de banco de dados:
set /p DB_HOST="🏢 Host do banco (padrão: localhost): "
if "%DB_HOST%"=="" set DB_HOST=localhost

set /p DB_PORT="🔌 Porta do banco (padrão: 5432): "
if "%DB_PORT%"=="" set DB_PORT=5432

set /p DB_NAME="📊 Nome do banco (padrão: serverwatch_db): "
if "%DB_NAME%"=="" set DB_NAME=serverwatch_db

set /p DB_USER="👤 Usuário do banco (padrão: serverwatch_user): "
if "%DB_USER%"=="" set DB_USER=serverwatch_user

set /p DB_PASSWORD="🔐 Senha do banco: "

echo.
echo 📝 Atualizando arquivo .env...

rem Criar o arquivo .env
(
echo # ===========================================
echo # DATABASE CONFIGURATION
echo # ===========================================
echo DATABASE_HOST=%DB_HOST%
echo DATABASE_PORT=%DB_PORT%
echo DATABASE_USER=%DB_USER%
echo DATABASE_PASSWORD=%DB_PASSWORD%
echo DATABASE_NAME=%DB_NAME%
echo.
echo # ===========================================
echo # KEYCLOAK CONFIGURATION
echo # ===========================================
echo KEYCLOAK_AUTH_SERVER_URL=%KEYCLOAK_URL%
echo KEYCLOAK_REALM=%REALM_NAME%
echo KEYCLOAK_CLIENT_ID=%CLIENT_ID%
echo KEYCLOAK_CLIENT_SECRET=%CLIENT_SECRET%
echo.
echo # ===========================================
echo # KEYCLOAK ADMIN API CONFIGURATION
echo # ===========================================
echo KEYCLOAK_ADMIN_CLIENT_ID=%CLIENT_ID%-admin
echo KEYCLOAK_ADMIN_CLIENT_SECRET=change-me-in-production
echo.
echo # ===========================================
echo # APPLICATION CONFIGURATION
echo # ===========================================
echo APP_BASE_URL=%APP_URL%
echo SERVER_PORT=%SERVER_PORT%
echo.
echo # ===========================================
echo # SECURITY CONFIGURATION
echo # ===========================================
echo JWT_ACCESS_TOKEN_VALIDITY=3600
echo JWT_REFRESH_TOKEN_VALIDITY=86400
echo SESSION_TIMEOUT=1800
echo.
echo # ===========================================
echo # LOGGING CONFIGURATION
echo # ===========================================
echo LOGGING_LEVEL_SECURITY=INFO
echo LOGGING_LEVEL_KEYCLOAK=DEBUG
echo.
echo # ===========================================
echo # DEVELOPMENT/PRODUCTION FLAGS
echo # ===========================================
echo SPRING_PROFILES_ACTIVE=local
echo DEBUG_OAUTH2=true
echo ENABLE_SSL=false
) > .env

echo ✅ Arquivo .env atualizado com sucesso!
echo.

echo 🔐 Configuração do Client no Keycloak:
echo =============================================
echo 1. Acesse o Keycloak Admin Console: %KEYCLOAK_URL%/admin
echo 2. Selecione o realm: %REALM_NAME%
echo 3. Vá em Clients → Create Client
echo 4. Configure:
echo    - Client ID: %CLIENT_ID%
echo    - Client Type: OpenID Connect
echo    - Client authentication: ON
echo.
echo 5. Na aba Settings, configure:
echo    - Valid redirect URIs: %APP_URL%/login/oauth2/code/keycloak
echo    - Valid post logout redirect URIs: %APP_URL%/login?logout=true
echo    - Web origins: %APP_URL%
echo.
echo 6. Na aba Credentials, copie o Client Secret e atualize o arquivo .env

echo.
echo 👥 Configuração de Roles:
echo =============================================
echo Crie as seguintes roles no realm:
echo    - USER (usuário padrão)
echo    - ADMIN (administrador)
echo.

echo 🚦 Para testar a integração:
echo =============================================
echo 1. Execute: mvnw.cmd spring-boot:run
echo 2. Acesse: %APP_URL%
echo 3. Clique em 'Entrar com Keycloak'
echo 4. Faça login no Keycloak
echo.

echo 📚 Documentação completa:
echo Consulte o arquivo KEYCLOAK_SETUP.md para mais detalhes.
echo.

echo 🎉 Configuração concluída!
echo Não se esqueça de configurar o client no Keycloak Admin Console.

pause