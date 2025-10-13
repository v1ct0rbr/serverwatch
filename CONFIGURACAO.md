# Configuração de Propriedades Customizadas - ServerWatch

## Visão Geral

A aplicação ServerWatch agora utiliza um sistema de configuração modular que resolve o problema de "Unknown property" do Spring Boot através de classes `@ConfigurationProperties` dedicadas.

## Estrutura de Configuração

### 1. Arquivos de Configuração

- **`application.yaml`** - Configurações padrão do Spring Boot
- **`application-keycloak.yaml`** - Configurações específicas do Keycloak
- **`application-local.yaml`** - Configurações para desenvolvimento local

### 2. Classes de Propriedades

#### KeycloakProperties.java
```java
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {
    // Configurações do Keycloak
}
```

#### ServerWatchProperties.java  
```java
@ConfigurationProperties(prefix = "serverwatch")
public class ServerWatchProperties {
    // Configurações da aplicação
}
```

#### CustomConfigurationManager.java
- Gerencia o carregamento e validação das propriedades
- Calcula URLs automaticamente
- Fornece métodos utilitários

## Como Usar

### 1. Injetar Dependências

```java
@Service
@RequiredArgsConstructor
public class MyService {
    
    private final KeycloakProperties keycloakProperties;
    private final ServerWatchProperties serverWatchProperties;
    
    public void exemploUso() {
        // Acessar configurações do Keycloak
        String realm = keycloakProperties.getRealm();
        String issuerUrl = keycloakProperties.getUrls().getIssuer();
        
        // Acessar configurações da aplicação
        String appName = serverWatchProperties.getApplication().getName();
        int checkInterval = serverWatchProperties.getMonitoring().getServerCheckIntervalSeconds();
    }
}
```

### 2. Configurar Variáveis de Ambiente

```bash
# Keycloak
export KEYCLOAK_AUTH_SERVER_URL=https://keycloak.derpb.com.br
export KEYCLOAK_REALM=DERPB
export KEYCLOAK_CLIENT_ID=serverwatch-client
export KEYCLOAK_CLIENT_SECRET=your-secret-here
export KEYCLOAK_ADMIN_CLIENT_ID=serverwatch-admin
export KEYCLOAK_ADMIN_CLIENT_SECRET=admin-secret-here

# Database
export DATABASE_HOST=localhost
export DATABASE_PORT=5432
export DATABASE_NAME=serverwatch_db
export DATABASE_USER=serverwatch_user
export DATABASE_PASSWORD=serverwatch_password
```

### 3. Propriedades Disponíveis

#### Keycloak (keycloak.*)
- `auth-server-url` - URL base do servidor Keycloak
- `realm` - Nome do realm
- `resource` - Client ID da aplicação
- `admin.client-id` - Client ID administrativo
- `admin.client-secret` - Client Secret administrativo
- `configuration.*` - Configurações avançadas

#### ServerWatch (serverwatch.*)
- `application.name` - Nome da aplicação
- `application.version` - Versão da aplicação
- `application.development-mode` - Modo de desenvolvimento
- `monitoring.server-check-interval-seconds` - Intervalo de verificação
- `monitoring.server-ping-timeout-seconds` - Timeout de ping
- `ui.theme` - Tema da interface
- `ui.items-per-page` - Itens por página
- `notifications.email.*` - Configurações de email

## Endpoints de Configuração

A aplicação oferece endpoints para visualizar as configurações:

- `GET /api/config/info` - Informações gerais
- `GET /api/config/keycloak-urls` - URLs do Keycloak
- `GET /api/config/development-mode` - Status do modo desenvolvimento

## Validações

O sistema inclui validações automáticas:

- ✅ URLs obrigatórias do Keycloak
- ✅ Realm e resource obrigatórios
- ✅ Intervalos de monitoramento válidos
- ✅ Configurações de email quando habilitado

## Profiles

### Profile Local (application-local.yaml)
```yaml
serverwatch:
  application:
    development-mode: true
    debug:
      enabled: true
      log-sql-queries: true
      log-keycloak-events: true
```

### Profile Produção
```yaml
serverwatch:
  application:
    development-mode: false
    debug:
      enabled: false
  monitoring:
    server-check-interval-seconds: 30
  notifications:
    email:
      enabled: true
```

## Benefícios

1. **Sem Erros de "Unknown Property"** - Classes `@ConfigurationProperties` eliminam warnings
2. **Validação Automática** - Propriedades são validadas na inicialização
3. **Tipagem Forte** - Propriedades são objetos Java tipados
4. **URLs Calculadas** - URLs do Keycloak são geradas automaticamente
5. **Configuração Modular** - Separação por domínio (keycloak, serverwatch, etc.)
6. **Documentação Integrada** - Propriedades documentadas no código

## Exemplo Completo

```java
@Component
@RequiredArgsConstructor 
public class KeycloakService {
    
    private final KeycloakProperties keycloakProperties;
    private final CustomConfigurationManager configManager;
    
    public String getLoginUrl() {
        // URL calculada automaticamente
        return keycloakProperties.getUrls().getAuth();
    }
    
    public boolean isDevelopment() {
        // Método utilitário
        return configManager.isDevelopmentMode();
    }
    
    public String getKeycloakEndpoint(String path) {
        // Helper method
        return configManager.getKeycloakUrl(path);
    }
}
```

## Troubleshooting

### Erro: "Unknown property"
- ✅ **Solução**: Classes `@ConfigurationProperties` criadas
- Certifique-se que `CustomConfigurationManager` está sendo carregado

### Erro: "Property validation failed"  
- Verifique se todas as propriedades obrigatórias estão configuradas
- Consulte os logs para detalhes da validação

### URLs não calculadas
- Certifique-se que `@PostConstruct` está sendo executado
- Verifique se `auth-server-url` e `realm` estão configurados