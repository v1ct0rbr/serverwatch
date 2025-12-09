# Configuração de Role Mappers no Keycloak

## Diagnóstico

**PROBLEMA IDENTIFICADO**: O token JWT do Keycloak NÃO contém nenhuma informação de roles.

### Token Atual (Vazio de Roles)
```
Realm Access: null ✗
Resource Access: null ✗
Roles: null ✗
```

### Causa
O client scope "roles" está atribuído ao cliente, mas **NÃO possui mappers configurados** para incluir as roles no token JWT.

---

## Solução: Configurar Mappers no Keycloak

### Passo 1: Acessar Client Scopes
1. Abra o Keycloak Admin Console: `https://keycloak.derpb.com.br`
2. Selecione o realm **testes**
3. Vá para **Client Scopes** (menu esquerdo)
4. Procure pelo scope chamado **roles**
5. Clique nele para abrir

### Passo 2: Verificar/Adicionar Mappers

No escopo "roles", vá para a aba **Mappers**.

Você precisa de DOIS mappers:

#### Mapper 1: User Realm Role (para realm_access.roles)

**Se não existir**, crie um novo:

1. Clique em **Add Mapper** → **From predefined mappers**
2. Selecione **User Realm Role**
3. Confirme

**Configuração padrão (já vem correta):**
- **Name**: User Realm Role
- **Mapper Type**: User Realm Role
- **Token Claim Name**: realm_access.roles
- **Claim JSON Type**: String
- **Multivalued**: ON
- **Add to ID token**: ON ✓
- **Add to Access token**: ON ✓

#### Mapper 2: User Client Role (para resource_access.roles)

**Se não existir**, crie um novo:

1. Clique em **Add Mapper** → **From predefined mappers**
2. Selecione **User Client Role**
3. Confirme

**Configuração padrão (já vem correta):**
- **Name**: User Client Role
- **Mapper Type**: User Client Role
- **Token Claim Name**: resource_access.${client_id}.roles
- **Claim JSON Type**: String
- **Multivalued**: ON
- **Add to ID token**: ON ✓
- **Add to Access token**: ON ✓

### Passo 3: Verificar Atribuição do Scope ao Cliente

1. Vá para **Clients** (menu esquerdo)
2. Abra o cliente **teste-cli**
3. Vá para a aba **Client Scopes**
4. Verifique se o scope **roles** está na coluna **Setup** (lado esquerdo)
   - Se estiver em **Add optional client scopes**, mova para **Assigned Client Scopes** clicando no botão de seta
   - O scope deve estar **ASSIGNED** (não opcional)

### Passo 4: Verificar Atribuição de Roles ao Usuário

1. Vá para **Users** (menu esquerdo)
2. Procure por **victorqueiroga**
3. Vá para a aba **Role Mapping**
4. Verifique se há roles atribuídas:
   - **Realm Roles**: Deve mostrar as roles do realm (ex: SERVERWATCH_USER, SERVERWATCH_MONITOR, etc.)
   - **Client Roles**: Deve mostrar roles específicas do cliente (se houver)

Se as roles não estiverem visíveis:
- Clique em **Assign Role**
- Selecione as roles desejadas (SERVERWATCH_USER, SERVERWATCH_MONITOR, NETNOTIFY_ADMIN, etc.)
- Confirme

### Passo 5: Testar

Após configurar os mappers:

1. **Faça logout** da aplicação
2. **Limpe o cache** do navegador (Ctrl+Shift+Delete)
3. **Faça login novamente**
4. Acesse `http://localhost:8080/debug/current-user`
5. **Verifique os logs** procurando por:

```
[KeycloakUserService] VERIFICANDO CLAIMS ESPECÍFICOS:
  realm_access: {roles=[SERVERWATCH_USER, SERVERWATCH_MONITOR, ...]}
  resource_access: null
  roles: null
  groups: null
```

Se os mappers estiverem corretos, você deve ver algo como:
```
realm_access: {roles=[SERVERWATCH_USER, SERVERWATCH_MONITOR, NETNOTIFY_ADMIN, user]}
```

---

## Alternativa: Usar PowerShell Script

Se preferir usar o script automatizado (já existe no projeto):

```powershell
.\setup-keycloak-roles.ps1
```

Este script:
1. Conecta ao Keycloak Admin API
2. Verifica/cria os mappers necessários
3. Valida a configuração
4. Limpa o cache

---

## Verificação Final

Após os mappers estarem configurados e ativados, o aplicativo deve:

1. ✓ Receber roles no token JWT (em `realm_access.roles`)
2. ✓ Extrair as roles no `CustomOidcUserService`
3. ✓ Mapear as roles do Keycloak para roles da aplicação
4. ✓ Salvar as roles no banco de dados
5. ✓ Exibir as roles na página de perfil

---

## Referências

- [Keycloak Role Mappers Documentation](https://www.keycloak.org/docs/latest/server_admin/index.html#_client_scopes)
- [OIDC Token Mappers](https://www.keycloak.org/docs/latest/server_admin/index.html#_oidc_token_mappers)
- [Realm Roles vs Client Roles](https://www.keycloak.org/docs/latest/server_admin/index.html#_realm_roles)

---

## Próximos Passos

1. Configure os mappers conforme descrito acima
2. Teste o login
3. Capture os novos logs de token
4. Confirme que `realm_access` e `resource_access` agora contêm as roles
5. A sincronização automática funcionará uma vez que as roles estejam no token
