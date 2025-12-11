# 🧪 Guia de Testes - Correção do Erro "authorization_request_not_found"

## Resumo da Correção
O erro "authorization_request_not_found" ocorre quando a sessão HTTP é perdida entre o redirect do Keycloak e o callback OAuth2. As seguintes correções foram implementadas:

1. ✅ **CustomAuthenticationFailureHandler** - Detecta e mapeia erros OAuth2
2. ✅ **SecurityConfiguration** - Melhorou gerenciamento de sessão
3. ✅ **AuthController** - Tratamento granular de erros com dicas úteis
4. ✅ **login.html** - Display de mensagens e dicas contextuais
5. ✅ **application.yaml** - Timeouts HTTP configurados

---

## 🧪 Teste 1: Primeiro Login (Simples)

### Pré-requisitos:
- Keycloak rodando e acessível
- Aplicação Spring Boot compilada
- Navegador web

### Passos:
1. Abra em modo anônimo: `Ctrl+Shift+N` (Chrome) ou `Ctrl+Shift+P` (Firefox)
2. Acesse: `http://localhost:8080/login`
3. Clique "Login com Keycloak"
4. Autentique com suas credenciais Keycloak
5. **Resultado Esperado:** Redirecionado para dashboard, logado com sucesso

### Se der erro:
- Verifique mensagem de erro exibida
- Se for "authorization_request_not_found" → mapeado para "Sua sessão expirou..."
- Limpe cookies e tente novamente
- Se persistir, verifique logs da aplicação

---

## 🧪 Teste 2: Timeout (Simular Keycloak Lento)

### Pré-requisitos:
- Ferramentas de network throttling ou proxy

### Opção A - Usando Browser Dev Tools:
1. Abra aplicação em modo normal
2. Press `F12` → DevTools → Network tab
3. Clique ícone de velocidade → "SLOW 3G" ou "Offline"
4. Clique "Login com Keycloak"
5. Cancele a conexão antes de completar
6. **Resultado Esperado:** Erro "timeout" exibido

### Opção B - Parar Keycloak:
1. Interrompa o container/serviço Keycloak
2. Tente fazer login
3. Aguarde ~30 segundos (timeout)
4. **Resultado Esperado:** Erro "Timeout na autenticação..."

### Verificar nos Logs:
```
[CustomAuthenticationFailureHandler] OAuth2 Authentication Failed - Code: timeout
[CustomAuthenticationFailureHandler] Detectado timeout na comunicação com Keycloak
```

---

## 🧪 Teste 3: Session Expiry (Simular Perda de Sessão)

### Pré-requisitos:
- Login bem-sucedido na aplicação
- Browser Dev Tools

### Passos:
1. Faça login com sucesso
2. Abra Dev Tools: `F12`
3. Vá para aba "Application" ou "Storage"
4. Expanda "Cookies" → `localhost:8080`
5. Delete o cookie `JSESSIONID`
6. Tente fazer logout
7. Clique "Login com Keycloak"
8. **Resultado Esperado:** Erro "Sua sessão expirou durante a autenticação."

### Verificar Dica:
Mensagem deve mostrar dica útil:
```
"Isso pode ocorrer se: a navegação demorou muito, você ficou inativo, 
ou o navegador perdeu cookies. Tente novamente."
```

---

## 🧪 Teste 4: Múltiplas Sessões Simultâneas

### Objetivo:
Verificar se a limitação de 1 sessão por usuário funciona.

### Pré-requisitos:
- Dois navegadores diferentes (ou abas em modo anônimo)
- Mesmo usuário Keycloak

### Passos:
1. **Navegador A:**
   - Abra `http://localhost:8080/login`
   - Clique "Login com Keycloak"
   - Autentique
   - Aguarde redirecionar para dashboard
   - **Status:** Logado em A

2. **Navegador B:**
   - Abra `http://localhost:8080/login`
   - Clique "Login com Keycloak"
   - Autentique com mesma conta
   - Aguarde redirecionar para dashboard
   - **Status:** Logado em B

3. **Volta para Navegador A:**
   - Clique em qualquer página
   - **Resultado Esperado:** Redirecionado para login com `?error=session_expired`
   - Mensagem: "Sua sessão expirou durante a autenticação."

### Interpretação:
- ✅ Comportamento correto: sessão anterior foi invalidada
- ✅ Segurança melhorada: apenas 1 sessão ativa por usuário
- ✅ Logout automático de outras sessões

---

## 🧪 Teste 5: Verificar Logs de Erro

### Arquivos de Log:
```
target/logs/
├── application.log
└── keycloak/
    └── keycloak.log
```

### Buscar por Erros Tratados:
```bash
# Grep para CustomAuthenticationFailureHandler
grep "OAuth2 Authentication Failed" logs/application.log

# Grep para erros específicos
grep "authorization_request_not_found" logs/application.log
grep "Sessão expirou" logs/application.log
grep "Detectado timeout" logs/application.log
```

### Log Esperado (Sucesso):
```
INFO  CustomOidcUserService - Usuário autenticado: user@example.com
INFO  KeycloakUserService - Sincronizando usuário com Keycloak: user@example.com
```

### Log Esperado (Erro Tratado):
```
WARN  CustomAuthenticationFailureHandler - OAuth2 Authentication Failed - Code: session_expired
WARN  AuthController - Sessão expirada detectada durante OAuth2 flow
```

---

## 🧪 Teste 6: Verificar Cookies e Headers

### Usando Browser Dev Tools:

1. **Abra DevTools:** `F12`
2. **Aba "Application" (Chrome) ou "Storage" (Firefox)**
3. **Cookies → localhost:8080**

#### Cookies Esperados Antes do Login:
```
JSESSIONID = [Session ID aleatório]
```

#### Cookies Esperados Após o Login:
```
JSESSIONID = [Nova sessão aleatória]
```

#### Headers HTTP Esperados:
1. **Request para /login:**
   ```
   Cookie: JSESSIONID=...
   ```

2. **Redirect para Keycloak:**
   ```
   Location: https://keycloak.example.com/auth/...?state=xxx&session_state=yyy
   ```

3. **Response do Keycloak (callback):**
   ```
   Code: 302/303 redirect
   Location: http://localhost:8080/login/oauth2/code/keycloak?code=xxx&state=xxx
   ```

---

## ✅ Checklist de Testes Completos

- [ ] **Teste 1:** Primeiro login funciona (modo anônimo)
- [ ] **Teste 2:** Timeout é detectado e exibido
- [ ] **Teste 3:** Session expiry é tratado corretamente
- [ ] **Teste 4:** Múltiplas sessões são limitadas a 1
- [ ] **Teste 5:** Logs contêm informações esperadas
- [ ] **Teste 6:** Cookies e headers estão corretos
- [ ] **Compilação:** `mvn clean compile` sem erros
- [ ] **Dashboard:** Acesso a /dashboard após login bem-sucedido
- [ ] **Logout:** Logout funciona e retorna a /login
- [ ] **Roles:** Roles do Keycloak aparecem corretamente

---

## 🔍 Troubleshooting

### Problema: "authorization_request_not_found" continua aparecendo

**Causas Possíveis:**
1. Session timeout do Keycloak muito curto
2. CORS não configurado corretamente
3. Cookie SameSite muito restritivo

**Soluções:**
```java
// 1. Verificar configuração do Keycloak
// Realm → Sessions → Default Session Idle Timeout: 30 minutos

// 2. Verificar CORS em SecurityConfiguration
// Deve permitir credenciais

// 3. Adicionar configuração de SameSite
// application.yaml
spring.session.web.http.cookie.same-site: Lax
```

---

### Problema: Não consegue logar nem uma vez

**Diagnóstico:**
1. Verificar se Keycloak está acessível: `curl http://keycloak:8080/auth/`
2. Verificar logs: `grep ERROR target/logs/application.log`
3. Verificar configuração OAuth2 em SecurityConfiguration
4. Verificar credenciais Keycloak em application.yaml

---

### Problema: Login funciona mas roles não aparecem

**Causa:** Keycloak role mappers não configurados

**Solução:**
1. Acesse Keycloak Admin Console
2. Realm → Clients → serverwatch-client
3. Client Scopes → roles
4. Mappers → "realm roles" e "client roles"
5. Confirme "Add to ID Token" está ativado

---

## 📊 Métricas de Sucesso

Se todos os testes passarem:

| Métrica | Esperado |
|---------|----------|
| Tempo 1º login | < 5 segundos |
| Tempo timeout | ~30 segundos (configurable) |
| Taxa de sucesso 1º login | 100% (em modo anônimo) |
| Sessões simultâneas por usuário | 1 (limitado) |
| Erros redirecionados corretamente | 100% |
| Dicas exibidas corretamente | 100% |

---

## 📞 Contato/Suporte

Se encontrar problemas persistentes:

1. **Verificar Logs:** `target/logs/application.log`
2. **Verificar Keycloak:** Admin Console → Events
3. **Verificar Navegador:** Dev Tools Network tab durante login
4. **Verificar Rede:** Connectivity entre App e Keycloak

---

**Última Atualização:** Dec 11, 2025  
**Status de Compilação:** ✅ SUCCESS  
**Versão:** Spring Boot 6.2.11, Spring Security 6.5.5, Keycloak 24+
