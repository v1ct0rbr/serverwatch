# Diagnóstico: Roles Não Estão Sendo Atribuídas

## 🔍 Problema

Quando você acessa `/debug/current-user`, as application roles aparecem vazias ou apenas com valor padrão.

## 📋 Possíveis Causas

### 1. ❌ Roles não estão no token Keycloak
O Keycloak não está incluindo as roles no JWT

### 2. ❌ Roles estão no token mas com nome diferente
Exemplo: token tem "admin" mas código procura "ADMIN"

### 3. ❌ Método `hasRole()` não está funcionando corretamente
Problema na validação das authorities

### 4. ❌ Banco de dados não está sendo atualizado
UserService executa mas a sincronização falha

## 🔧 Como Diagnosticar

### Passo 1: Verificar Logs do CustomOidcUserService

Quando você faz login, procure nos logs por:

```
[DEBUG] Authorities padrão do OIDC (incluem scopes): ...
[DEBUG] Roles do realm encontradas: [...]
[DEBUG] Roles do cliente teste-cli encontradas: [...]
[INFO] Authorities extraídas do Keycloak: [...]
```

**Se você vir:**
```
[WARN] Nenhuma role encontrada no token do Keycloak!
```

→ **Ir para: Solução 1**

### Passo 2: Verificar Logs do UserService

Procure por:
```
=== ROLE MAPPING DEBUG ===
Usuário: john_doe
Total de authorities: 3
  Authority: ROLE_ADMIN
  Authority: ROLE_OFFLINE_ACCESS
  Authority: ROLE_DEFAULT_ROLES_TESTES
Verificando hasRole('ADMIN'): true
Verificando hasRole('USER'): false
✓ Usuário john_doe tem role ADMIN, adicionadas todas as roles
Application Roles mapeadas: [SYSTEM_ADMIN, SERVER_MANAGER, ALERT_MANAGER, REPORT_VIEWER, MONITORING_VIEWER]
=== FIM ROLE MAPPING DEBUG ===
```

**Se `hasRole('ADMIN')` retornar `false`:**
→ **Ir para: Solução 2**

**Se Application Roles estiver vazio:**
→ **Ir para: Solução 3**

### Passo 3: Verificar Página de Debug

Acesse: `http://localhost:8080/debug/current-user`

**Você deverá ver:**
- ✅ Keycloak User → Authorities: [ROLE_ADMIN]
- ✅ Local User → Application Roles: [SYSTEM_ADMIN, SERVER_MANAGER, ...]

**Se Application Roles vazio:**
→ **Ir para: Solução 4**

## 🛠️ Soluções

### Solução 1: Roles Não Estão no Token Keycloak

**Sintomas:**
```
[WARN] Nenhuma role encontrada no token do Keycloak!
```

**Checklist:**
1. ✅ Abra Keycloak Admin Console: `https://keycloak.derpb.com.br/admin/master/console/`
2. ✅ Vá para **Realm Roles** (no menu esquerdo)
3. ✅ Verifique se existem roles como "admin", "user", etc
4. ✅ Se não existirem, crie-as:
   - Clique **Create role**
   - Nome: `admin`
   - Clique **Create**
5. ✅ Vá para **Users**
6. ✅ Selecione seu usuário
7. ✅ Clique na aba **Role mapping**
8. ✅ Clique **Assign role**
9. ✅ Selecione `admin`
10. ✅ Clique **Assign**
11. ✅ Faça logout e login novamente

**Resultado esperado após login:**
```
[DEBUG] Roles do realm encontradas: [admin]
[INFO] Authorities extraídas do Keycloak: [ROLE_ADMIN]
```

---

### Solução 2: Roles Têm Nome Diferente no Token

**Sintomas:**
```
Total de authorities: 3
  Authority: ROLE_ADMIN
Verificando hasRole('ADMIN'): false  ← False! Problema aqui
```

**Causa Provável:**
O método `hasRole()` está procurando por "ROLE_ADMIN" (com ROLE_ prefix), mas a authority tem um nome diferente.

**Checklist:**
1. ✅ Verifique quais authorities aparecem nos logs
2. ✅ Copie exatamente como aparecem (case-sensitive!)
3. ✅ Exemplo: Se aparecer `ROLE_ADMIN_REALM`, o `hasRole()` deve procurar por `ADMIN_REALM`

**Arquivo a verificar:** `src/main/java/com/victorqueiroga/serverwatch/security/KeycloakUser.java`

```java
public boolean hasRole(String role) {
    return authorities != null && authorities.stream()
        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.toUpperCase()));
}
```

Se as authorities aparecerem como `ROLE_ADMIN` e você chamar `hasRole("ADMIN")` → procura "ROLE_ADMIN" ✅ (correto)

**Se não funcionar, verifique se a authority é realmente "ROLE_ADMIN"**

---

### Solução 3: Authorities Vazias

**Sintomas:**
```
Total de authorities: 0
Verificando hasRole('ADMIN'): false
✗ Usuário john_doe não possui nenhuma role mapeada!
```

**Causa Provável:**
`CustomOidcUserService` não está extraindo as roles corretamente.

**Checklist:**
1. ✅ Verifique se `CustomOidcUserService` está sendo chamado
2. ✅ Procure por este log no início do login:
   ```
   [DEBUG] Authorities padrão do OIDC (incluem scopes): [...]
   ```
3. ✅ Se não aparecer, o serviço não está sendo usado
4. ✅ Verifique se está anotado com `@Service`:
   ```java
   @Service
   public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
   ```
5. ✅ Verifique se está configurado no `SecurityConfiguration`:
   ```java
   http.oauth2Login(oauth2 -> oauth2
       .userInfoEndpoint(userInfo -> userInfo
           .oidcUserService(customOidcUserService)
       )
   );
   ```

---

### Solução 4: Application Roles Vazias no Banco

**Sintomas:**
```
Application Roles mapeadas: [SYSTEM_ADMIN, SERVER_MANAGER, ...]  ← Log mostra OK
Mas ao acessar /debug/current-user → Application Roles: []  ← Vazio!
```

**Causa Provável:**
Sincronização não está salvando no banco de dados.

**Checklist:**
1. ✅ Verifique se há logs de sincronização:
   ```
   getOrCreateUser: Buscando usuário com Keycloak ID: ...
   Usuário existente encontrado: john_doe
   Roles mudaram para usuário john_doe: [] -> [SYSTEM_ADMIN, ...]
   Usuário john_doe sincronizado com sucesso. Roles: [...]
   ```

2. ✅ Se não aparecer "Usuário sincronizado com sucesso":
   - UserService pode não estar sendo chamado
   - Verifique se está anotado com `@Service`
   - Verifique se está com `@Transactional`

3. ✅ Se aparecer mas roles não estiverem no banco:
   - Problema no banco de dados
   - Verifique se a coluna exists: `SELECT application_roles FROM users WHERE keycloak_id = '...';`
   - Verifique permissões do usuário no banco

4. ✅ Limpe a cache:
   - Apague cookies do navegador
   - Faça logout e login novamente
   - Acesse `/debug/current-user`

---

## 📊 Fluxo Completo de Debug

```
1. Faça login
   ↓
2. Procure por: "Authorities extraídas do Keycloak"
   ├─ Se vazio → Solução 1
   └─ Se tem dados → próximo passo
   ↓
3. Procure por: "Verificando hasRole('ADMIN'): true/false"
   ├─ Se false → Solução 2
   └─ Se true → próximo passo
   ↓
4. Procure por: "Usuário sincronizado com sucesso. Roles:"
   ├─ Se não aparecer → Solução 3
   └─ Se aparecer → próximo passo
   ↓
5. Acesse /debug/current-user
   ├─ Se Application Roles vazio → Solução 4
   └─ Se tem dados → ✅ FUNCIONANDO!
```

---

## 🔍 Logs Importantes para Coletar

Execute estes comandos para obter logs detalhados:

### Ver últimos 100 logs
```bash
# Se usar arquivo de log
tail -100 logs/application.log

# Se usar console do Debug Terminal
# Procure por "ROLE MAPPING DEBUG" até "FIM ROLE MAPPING DEBUG"
```

### Ativar DEBUG logging

Adicione ao `application.yaml`:
```yaml
logging:
  level:
    com.victorqueiroga.serverwatch.security: DEBUG
    com.victorqueiroga.serverwatch.service: INFO
```

---

## ✅ Checklist Final

Após implementar as soluções, verifique:

- [ ] Logs mostram "Roles do realm encontradas: [...]"
- [ ] Logs mostram "Verificando hasRole('ADMIN'): true"
- [ ] Logs mostram "Usuário sincronizado com sucesso"
- [ ] `/debug/current-user` mostra Application Roles
- [ ] Banco de dados tem dados nas colunas de roles
- [ ] Página de profile exibe roles corretamente

---

## 🆘 Se Ainda Não Funcionar

1. ✅ Colete TODOS os logs (do login até `/debug/current-user`)
2. ✅ Procure por linhas com "ROLE", "Authorities", "mapKeycloak"
3. ✅ Copie e compartilhe os logs
4. ✅ Descreva qual das 4 soluções você já tentou
