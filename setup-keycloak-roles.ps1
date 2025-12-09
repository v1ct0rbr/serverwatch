# ====================================
# Keycloak Role Mapper Setup Script
# ====================================
# Este script configura o Keycloak para mapear roles ao token JWT

param(
    [string]$KeycloakUrl = "https://keycloak.derpb.com.br",
    [string]$Realm = "testes",
    [string]$ClientId = "teste-cli",
    [string]$AdminUser = "admin",
    [string]$AdminPassword = "admin"
)

Write-Host "🚀 Iniciando configuração de roles no Keycloak..." -ForegroundColor Green
Write-Host "  Keycloak URL: $KeycloakUrl"
Write-Host "  Realm: $Realm"
Write-Host "  Client: $ClientId"
Write-Host ""

# ====================================
# 1. Obter token de acesso admin
# ====================================
Write-Host "📝 Step 1: Obtendo token de acesso admin..." -ForegroundColor Cyan

$tokenUrl = "$KeycloakUrl/realms/master/protocol/openid-connect/token"
$tokenBody = @{
    grant_type    = "password"
    client_id     = "admin-cli"
    username      = $AdminUser
    password      = $AdminPassword
} | ConvertTo-Json

try {
    $tokenResponse = Invoke-WebRequest -Uri $tokenUrl `
        -Method Post `
        -ContentType "application/x-www-form-urlencoded" `
        -Body ([System.Web.HttpUtility]::ParseQueryString(@{
            grant_type = "password"
            client_id  = "admin-cli"
            username   = $AdminUser
            password   = $AdminPassword
        })).ToString() `
        -SkipCertificateCheck

    $token = ($tokenResponse.Content | ConvertFrom-Json).access_token
    Write-Host "✅ Token obtido com sucesso!" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao obter token: $_" -ForegroundColor Red
    exit 1
}

# ====================================
# 2. Obter ID do cliente
# ====================================
Write-Host ""
Write-Host "📝 Step 2: Obtendo ID do cliente..." -ForegroundColor Cyan

$clientsUrl = "$KeycloakUrl/admin/realms/$Realm/clients?clientId=$ClientId"
$headers = @{ Authorization = "Bearer $token" }

try {
    $clientsResponse = Invoke-WebRequest -Uri $clientsUrl `
        -Method Get `
        -Headers $headers `
        -SkipCertificateCheck

    $clients = $clientsResponse.Content | ConvertFrom-Json
    
    if ($clients.Count -eq 0) {
        Write-Host "❌ Cliente '$ClientId' não encontrado!" -ForegroundColor Red
        exit 1
    }
    
    $clientUuid = $clients[0].id
    Write-Host "✅ Cliente encontrado: $clientUuid" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao obter cliente: $_" -ForegroundColor Red
    exit 1
}

# ====================================
# 3. Obter Client Scopes do cliente
# ====================================
Write-Host ""
Write-Host "📝 Step 3: Obtendo client scopes..." -ForegroundColor Cyan

$scopesUrl = "$KeycloakUrl/admin/realms/$Realm/clients/$clientUuid/client-scopes"

try {
    $scopesResponse = Invoke-WebRequest -Uri $scopesUrl `
        -Method Get `
        -Headers $headers `
        -SkipCertificateCheck

    $scopes = $scopesResponse.Content | ConvertFrom-Json
    Write-Host "✅ Client scopes obtidos" -ForegroundColor Green
    
    # Verificar se 'roles' está presente
    $rolesScope = $scopes | Where-Object { $_.name -eq "roles" }
    if ($rolesScope) {
        Write-Host "   ✓ Scope 'roles' encontrado: $($rolesScope.id)" -ForegroundColor Green
    } else {
        Write-Host "   ✗ Scope 'roles' NÃO encontrado!" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Erro ao obter scopes: $_" -ForegroundColor Red
    exit 1
}

# ====================================
# 4. Obter ID do Scope 'roles'
# ====================================
Write-Host ""
Write-Host "📝 Step 4: Obtendo ID do scope 'roles'..." -ForegroundColor Cyan

$rolesSourceUrl = "$KeycloakUrl/admin/realms/$Realm/client-scopes?search=roles"

try {
    $rolesSourceResponse = Invoke-WebRequest -Uri $rolesSourceUrl `
        -Method Get `
        -Headers $headers `
        -SkipCertificateCheck

    $rolesScopes = $rolesSourceResponse.Content | ConvertFrom-Json
    $rolesScope = $rolesScopes | Where-Object { $_.name -eq "roles" }
    
    if (-not $rolesScope) {
        Write-Host "❌ Scope 'roles' não encontrado no realm!" -ForegroundColor Red
        exit 1
    }
    
    $rolesScopeId = $rolesScope.id
    Write-Host "✅ Scope 'roles' ID: $rolesScopeId" -ForegroundColor Green
} catch {
    Write-Host "❌ Erro ao obter scope 'roles': $_" -ForegroundColor Red
    exit 1
}

# ====================================
# 5. Verificar se 'roles' já está atribuído
# ====================================
Write-Host ""
Write-Host "📝 Step 5: Verificando se 'roles' já está atribuído..." -ForegroundColor Cyan

$assignedUrl = "$KeycloakUrl/admin/realms/$Realm/clients/$clientUuid/scope-mappings/client-scopes"

try {
    $assignedResponse = Invoke-WebRequest -Uri $assignedUrl `
        -Method Get `
        -Headers $headers `
        -SkipCertificateCheck

    $assigned = $assignedResponse.Content | ConvertFrom-Json
    $rolesAssigned = $assigned | Where-Object { $_.id -eq $rolesScopeId }
    
    if ($rolesAssigned) {
        Write-Host "✅ Scope 'roles' já está atribuído!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Scope 'roles' não está atribuído, atribuindo agora..." -ForegroundColor Yellow
        
        # Atribuir scope
        $assignBody = @(@{
            id   = $rolesScopeId
            name = "roles"
        }) | ConvertTo-Json
        
        try {
            Invoke-WebRequest -Uri $assignedUrl `
                -Method Post `
                -Headers $headers `
                -ContentType "application/json" `
                -Body $assignBody `
                -SkipCertificateCheck | Out-Null
            
            Write-Host "✅ Scope 'roles' atribuído com sucesso!" -ForegroundColor Green
        } catch {
            Write-Host "❌ Erro ao atribuir scope: $_" -ForegroundColor Red
            exit 1
        }
    }
} catch {
    Write-Host "⚠️  Não foi possível verificar scope atribuído, continuando..." -ForegroundColor Yellow
}

# ====================================
# 6. Obter mappers do scope 'roles'
# ====================================
Write-Host ""
Write-Host "📝 Step 6: Verificando mappers do scope 'roles'..." -ForegroundColor Cyan

$mappersUrl = "$KeycloakUrl/admin/realms/$Realm/client-scopes/$rolesScopeId/protocol-mappers/models"

try {
    $mappersResponse = Invoke-WebRequest -Uri $mappersUrl `
        -Method Get `
        -Headers $headers `
        -SkipCertificateCheck

    $mappers = $mappersResponse.Content | ConvertFrom-Json
    
    $realmRoleMapper = $mappers | Where-Object { $_.name -eq "realm roles" }
    
    if ($realmRoleMapper) {
        Write-Host "✅ Mapper 'realm roles' já existe!" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Mapper 'realm roles' não encontrado, criando..." -ForegroundColor Yellow
        
        # Criar mapper
        $mapperBody = @{
            name           = "realm roles"
            protocol       = "openid-connect"
            protocolMapper = "oidc-usermodel-realm-role-mapper"
            consentRequired = $false
            config         = @{
                "multivalued"                 = "true"
                "userinfo.token.claim"        = "true"
                "id.token.claim"              = "true"
                "access.token.claim"          = "true"
                "claim.name"                  = "realm_access.roles"
                "jsonType.label"              = "String"
            }
        } | ConvertTo-Json
        
        try {
            Invoke-WebRequest -Uri $mappersUrl `
                -Method Post `
                -Headers $headers `
                -ContentType "application/json" `
                -Body $mapperBody `
                -SkipCertificateCheck | Out-Null
            
            Write-Host "✅ Mapper 'realm roles' criado com sucesso!" -ForegroundColor Green
        } catch {
            Write-Host "❌ Erro ao criar mapper: $_" -ForegroundColor Red
            exit 1
        }
    }
} catch {
    Write-Host "❌ Erro ao obter mappers: $_" -ForegroundColor Red
    exit 1
}

# ====================================
# Conclusão
# ====================================
Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║         ✅ CONFIGURAÇÃO CONCLUÍDA COM SUCESSO!             ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Próximos passos:" -ForegroundColor Cyan
Write-Host "  1. Faça LOGOUT da aplicação"
Write-Host "  2. Limpe os COOKIES do navegador"
Write-Host "  3. Faça LOGIN novamente"
Write-Host "  4. Acesse http://localhost:8080/debug/current-user"
Write-Host "  5. Verifique se 'realm_access' e 'resource_access' aparecem nos logs"
Write-Host ""
Write-Host "As roles devem agora aparecer no token JWT! 🎉" -ForegroundColor Green
