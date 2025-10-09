# 🔐 Configuração Keycloak - ServerWatch

Este documento explica como configurar a integração do ServerWatch com o Keycloak para autenticação e autorização.

## 📋 Pré-requisitos

1. **Servidor Keycloak** rodando e acessível
2. **Realm configurado** no Keycloak
3. **Client registrado** para a aplicação ServerWatch
4. **Variáveis de ambiente** configuradas

## 🚀 Configuração Rápida

### 1. Configurar Variáveis de Ambiente

Copie o arquivo `.env.example` para `.env` e configure as variáveis:

```bash
cp .env.example .env
```

### 2. Configurações Principais

Edite o arquivo `.env` com suas configurações:

```env
# URL do seu servidor Keycloak (sem /realms no final)
KEYCLOAK_AUTH_SERVER_URL=https://seu-keycloak.com

# Nome do realm no Keycloak
KEYCLOAK_REALM=seu-realm

# Client ID registrado no Keycloak
KEYCLOAK_CLIENT_ID=serverwatch-client

# Client Secret (obter do Keycloak Admin Console)
KEYCLOAK_CLIENT_SECRET=seu-client-secret
```

## 🔧 Configuração do Client no Keycloak

### 1. Criar Client no Keycloak Admin Console

1. Acesse o **Keycloak Admin Console**
2. Selecione seu realm
3. Vá em **Clients** → **Create Client**
4. Configure:
   - **Client ID**: `serverwatch-client`
   - **Client Type**: `OpenID Connect`
   - **Client authentication**: `ON`

### 2. Configurações do Client

#### Aba Settings:
- **Valid redirect URIs**: 
  - `http://localhost:8080/login/oauth2/code/keycloak`
  - `https://seu-dominio.com/login/oauth2/code/keycloak`
- **Valid post logout redirect URIs**: 
  - `http://localhost:8080/login?logout=true`
  - `https://seu-dominio.com/login?logout=true`
- **Web origins**: 
  - `http://localhost:8080`
  - `https://seu-dominio.com`

#### Aba Advanced:
- **Access Token Lifespan**: `15 minutes`
- **Client Session Idle**: `30 minutes`
- **Client Session Max**: `12 hours`

### 3. Configurar Roles

#### 3.1 Roles do Realm:
- `USER` - Usuário padrão do sistema
- `ADMIN` - Administrador do sistema

#### 3.2 Roles do Client (opcional):
- `SERVERWATCH_USER` - Acesso ao ServerWatch
- `SERVERWATCH_ADMIN` - Administração do ServerWatch

### 4. Configurar Users

1. Crie usuários em **Users** → **Add user**
2. Atribua roles em **Role mapping**
3. Configure email verificado se necessário

## 🔑 Client Admin (Opcional)

Para usar a API Admin do Keycloak, configure um client de serviço:

### 1. Criar Service Account Client

1. **Client ID**: `serverwatch-admin`
2. **Client authentication**: `ON`
3. **Authorization**: `OFF`
4. **Standard flow**: `OFF`
5. **Direct access grants**: `OFF`
6. **Service accounts roles**: `ON`

### 2. Configurar Permissions

Na aba **Service account roles**, adicione:
- `realm-management` → `view-users`
- `realm-management` → `query-users`
- `realm-management` → `view-clients`

## 📝 Variáveis de Ambiente Completas

```env
# ===========================================
# DATABASE CONFIGURATION
# ===========================================
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_USER=serverwatch_user
DATABASE_PASSWORD=serverwatch_password
DATABASE_NAME=serverwatch_db

# ===========================================
# KEYCLOAK CONFIGURATION
# ===========================================
KEYCLOAK_AUTH_SERVER_URL=https://keycloak.derpb.com.br
KEYCLOAK_REALM=DERPB
KEYCLOAK_CLIENT_ID=serverwatch-client
KEYCLOAK_CLIENT_SECRET=seu-client-secret-aqui

# ===========================================
# KEYCLOAK ADMIN API (OPCIONAL)
# ===========================================
KEYCLOAK_ADMIN_CLIENT_ID=serverwatch-admin
KEYCLOAK_ADMIN_CLIENT_SECRET=seu-admin-secret-aqui

# ===========================================
# APPLICATION CONFIGURATION
# ===========================================
APP_BASE_URL=http://localhost:8080
SERVER_PORT=8080

# ===========================================
# SECURITY & LOGGING
# ===========================================
JWT_ACCESS_TOKEN_VALIDITY=3600
LOGGING_LEVEL_SECURITY=INFO
LOGGING_LEVEL_KEYCLOAK=DEBUG
SPRING_PROFILES_ACTIVE=local
DEBUG_OAUTH2=true
```

## 🚦 Testando a Integração

### 1. Iniciar a Aplicação

```bash
# Com Maven
./mvnw spring-boot:run

# Ou com Java
java -jar target/serverwatch-0.0.1-SNAPSHOT.jar
```

### 2. Testar Autenticação

1. Acesse: `http://localhost:8080`
2. Você será redirecionado para `/login`
3. Clique em **"Entrar com Keycloak"**
4. Faça login no Keycloak
5. Será redirecionado para `/dashboard`

### 3. Verificar Logs

Os logs devem mostrar:
```
DEBUG o.s.s.o.c.OidcUserService : Retrieving user info from: https://keycloak.../userinfo
INFO  c.v.s.security.KeycloakUserService : User authenticated: usuario@example.com
DEBUG c.v.s.security.KeycloakJwtConverter : Extracted roles: [ROLE_USER]
```

## 🔍 Troubleshooting

### Problema: Redirect URI não válido
**Solução**: Verifique se as URIs no client Keycloak estão corretas:
- `http://localhost:8080/login/oauth2/code/keycloak`

### Problema: Client secret inválido
**Solução**: 
1. Vá no Keycloak Admin → Clients → serverwatch-client → Credentials
2. Copie o **Client secret** para a variável `KEYCLOAK_CLIENT_SECRET`

### Problema: Token JWT inválido
**Solução**: Verifique se a URL do JWK Set está correta:
- `https://seu-keycloak.com/realms/seu-realm/protocol/openid-connect/certs`

### Problema: Roles não são carregadas
**Solução**: 
1. Verifique se as roles estão atribuídas ao usuário
2. Certifique-se que o client scope inclui `roles`
3. Verifique se o `KeycloakJwtAuthenticationConverter` está funcionando

## 📚 Recursos Adicionais

- **Keycloak Documentation**: https://www.keycloak.org/documentation
- **Spring Security OAuth2**: https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html
- **OpenID Connect**: https://openid.net/connect/

## 🆘 Suporte

Para problemas específicos:
1. Verifique os logs da aplicação
2. Verifique os logs do Keycloak
3. Teste as URLs de configuração manualmente
4. Valide os tokens JWT em https://jwt.io

---

**Nota**: Em produção, sempre use HTTPS e mantenha os secrets seguros!