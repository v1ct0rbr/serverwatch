# Correção: Extração Correta de Roles do Keycloak

## 🔴 Problema Identificado

As authorities carregadas estavam incluindo **SCOPES** em vez de **ROLES**:

```
❌ OIDC_USERSCOPE_openidSCOPE_emailSCOPE_profile
```

Isso ocorria porque o código estava copiando **todas** as authorities padrão do OidcUser, que incluem:
- Scopes OAuth2 (openid, email, profile)
- Prefixos padrão do Spring (OIDC_USER, SCOPE_)

## ✅ Solução Implementada

Modificado `CustomOidcUserService.java` para:

1. **Ignorar** as authorities padrão do OIDC (que incluem scopes)
2. **Extrair apenas** as roles reais do token Keycloak
3. **Processar** `realm_access.roles` e `resource_access.<client>.roles`

### Antes (ERRADO):
```java
// ❌ Copia TUDO incluindo scopes
Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

// ❌ Adiciona mais roles (mantém scopes)
authorities.addAll(keycloakAuthorities);

return new CustomOidcUser(oidcUser, authorities);  // ❌ Tem scopes!
```

### Depois (CORRETO):
```java
// ✅ Extrai APENAS roles do Keycloak
Set<GrantedAuthority> keycloakAuthorities = extractAuthorities(idToken);

// ✅ Retorna SEM scopes
return new CustomOidcUser(oidcUser, keycloakAuthorities);
```

## 📊 Fluxo Detalhado

### Token JWT do Keycloak

O token contém:
```json
{
  "scope": "openid email profile",           // ← OAuth2 scopes
  "realm_access": {
    "roles": ["admin", "offline_access", "default-roles-testes"]
  },
  "resource_access": {
    "teste-cli": {
      "roles": ["admin"]                     // ← Roles que queremos
    }
  }
}
```

### Processamento ANTES (ERRADO)

```
OidcUser padrão extrai:
  - SCOPE_openid
  - SCOPE_email
  - SCOPE_profile
  - OIDC_USER

Adiciona:
  + ROLE_ADMIN
  + ROLE_OFFLINE_ACCESS

Resultado: ❌ SCOPE_openid, SCOPE_email, SCOPE_profile, ROLE_ADMIN
```

### Processamento DEPOIS (CORRETO)

```
Extrai APENAS realm_access.roles e resource_access:
  ✅ ROLE_ADMIN
  ✅ ROLE_OFFLINE_ACCESS

Resultado: ✅ ROLE_ADMIN (sem scopes!)
```

## 🔍 Logs Esperados

Agora você verá logs como:

```
[DEBUG] Authorities padrão do OIDC (incluem scopes): [SCOPE_openid, SCOPE_email, SCOPE_profile, OIDC_USER]
[DEBUG] Roles do realm encontradas: [admin, offline_access, default-roles-testes]
[DEBUG] Roles do cliente teste-cli encontradas: [admin]
[INFO] Usuário OIDC carregado: john_doe
[INFO] Authorities extraídas do Keycloak: [ROLE_ADMIN, ROLE_OFFLINE_ACCESS, ROLE_DEFAULT_ROLES_TESTES]
[INFO] Total de roles extraídas: 3
```

## ⚠️ Se Nenhuma Role for Encontrada

Se você vir:
```
[WARN] Nenhuma role encontrada no token do Keycloak!
[WARN] Adicione roles ao usuário no Keycloak Admin Console
```

**Ações necessárias:**
1. Abra Keycloak Admin Console
2. Vá para Users
3. Selecione o usuário
4. Clique em "Role mapping"
5. Clique "Assign role"
6. Selecione roles (ex: admin)
7. Clique "Assign"

## 🧪 Como Testar

### 1. Fazer login com DEBUG enabled
Adicione ao `application.yaml`:
```yaml
logging:
  level:
    com.victorqueiroga.serverwatch.security: DEBUG
```

### 2. Fazer login e verificar logs

Procure por:
```
Authorities extraídas do Keycloak: [ROLE_ADMIN, ROLE_USER]
```

### 3. Acessar página de debug
```
http://localhost:8080/debug/current-user
```

Você deverá ver:
- ✅ Authorities: `[ROLE_ADMIN]` (sem SCOPE_*)
- ✅ Application Roles: `[SYSTEM_ADMIN, SERVER_MANAGER, ...]`

## 📋 Checklist de Verificação

- [ ] Build compilou com sucesso (`BUILD SUCCESS`)
- [ ] Aplicação iniciada sem erros
- [ ] Fez login no Keycloak
- [ ] Logs mostram "Roles do realm encontradas" (não SCOPES)
- [ ] `/debug/current-user` mostra apenas ROLE_* (sem SCOPE_*)
- [ ] Application roles foram mapeadas corretamente
- [ ] Roles no banco de dados foram atualizadas

## 🎯 Resumo das Mudanças

| Aspecto | Antes | Depois |
|---------|-------|--------|
| Authorities carregadas | SCOPE_openid, SCOPE_email, SCOPE_profile, ROLE_ADMIN | ROLE_ADMIN |
| Origem das authorities | OidcUser padrão + roles Keycloak | Apenas roles Keycloak |
| Sincronização de roles | Mapeava com scopes misturados | Apenas roles reais |
| Resultado final | Roles incorretas no banco | Roles corretas |

## 🔧 Código Alterado

**Arquivo:** `CustomOidcUserService.java`

**Mudança Principal:**
```java
// ❌ Antes: copia authorities padrão
Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
authorities.addAll(keycloakAuthorities);

// ✅ Depois: apenas roles Keycloak
Set<GrantedAuthority> keycloakAuthorities = extractAuthorities(idToken);
```

**Método `extractAuthorities`:**
- ✅ Agora retorna `Set<GrantedAuthority>` em vez de `Collection`
- ✅ Adiciona log quando nenhuma role é encontrada
- ✅ Log do total de roles extraídas

## 🚀 Próximos Passos

1. ✅ Compilação bem-sucedida
2. 🔄 Reiniciar aplicação
3. 🔐 Fazer login no Keycloak
4. 🔍 Verificar logs para "Roles do realm encontradas"
5. 📊 Acessar `/debug/current-user` para validar
6. ✅ Confirmar que roles sincronizaram corretamente

---

**Resultado Esperado:** Apenas ROLE_* aparecerão nas authorities, sem SCOPE_* 🎉
