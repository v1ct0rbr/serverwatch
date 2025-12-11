# Tratamento de Erros do Sistema

## Visão Geral

O sistema ServerWatch implementa um tratamento robusto de erros com:
- **CustomErrorController**: Trata erros HTTP via `/error`
- **GlobalExceptionHandler**: Trata exceções lançadas explicitamente na aplicação
- **Template Único (error.html)**: Template responsivo que adapta-se dinamicamente conforme o status code

## Arquitetura

### 1. CustomErrorController
Arquivo: `src/main/java/com/victorqueiroga/serverwatch/controller/CustomErrorController.java`

- Implementa `ErrorController` do Spring Boot
- Processa TODOS os erros HTTP automaticamente
- Mapeia para o endpoint `/error`
- Renderiza o template `error.html` com variáveis dinâmicas

### 2. GlobalExceptionHandler
Arquivo: `src/main/java/com/victorqueiroga/serverwatch/controller/GlobalExceptionHandler.java`

Tratador global com `@ControllerAdvice` que captura exceções lançadas explicitamente na aplicação.

#### Exceções Tratadas:

| Status | Exceção | Descrição |
|--------|---------|-----------|
| 404 | `NoHandlerFoundException` | Recurso/URL não encontrado |
| 403 | `AccessDeniedException` | Acesso negado |
| 400 | `MethodArgumentNotValidException` | Validação de entrada falhou |
| 400 | `MethodArgumentTypeMismatchException` | Tipo de argumento inválido |
| 400 | `BusinessException` | Erro de negócio customizado |
| 500 | `Exception` | Erro geral não tratado |

### 3. Template error.html
Arquivo: `src/main/resources/templates/error.html`

- Template único e responsivo
- Cores e ícones específicos para cada status code
- Exibe detalhes contextuais (validação, timestamp, URI, código de erro)
- Botões de ação (Voltar, Início)

## Como Usar

### Exemplo 1: Lançar erro de negócio em um Controller

```java
@PostMapping("/servers")
public String createServer(@Valid ServerDTO dto, Model model) {
    if (serverService.existsByName(dto.getName())) {
        throw new GlobalExceptionHandler.BusinessException(
            "Servidor com este nome já existe",
            "ERR_SERVER_DUPLICATE",
            "Verifique o nome do servidor e tente novamente com um nome único."
        );
    }
    
    Server server = serverService.save(dto);
    return "redirect:/servers/" + server.getId();
}
```

### Exemplo 2: Usar em um Service

```java
@Service
public class ServerService {
    
    public Server getServerById(Long id) {
        return serverRepository.findById(id)
            .orElseThrow(() -> new GlobalExceptionHandler.BusinessException(
                "Servidor não encontrado",
                "ERR_SERVER_NOT_FOUND",
                "O servidor com ID " + id + " não existe no sistema."
            ));
    }
}
```

### Exemplo 3: Validação com BusinessException

```java
public void updateServer(Server server, ServerUpdateDTO dto) {
    if (dto.getPort() < 1 || dto.getPort() > 65535) {
        throw new GlobalExceptionHandler.BusinessException(
            "Porta inválida",
            "ERR_INVALID_PORT",
            "A porta deve estar entre 1 e 65535. Valor fornecido: " + dto.getPort()
        );
    }
    
    server.setPort(dto.getPort());
    serverRepository.save(server);
}
```

## Códigos de Status Suportados

### 400 - Bad Request
- **Cor**: Rosa/Vermelho
- **Ícone**: ❌
- **Casos de uso**:
  - Validação de entrada falhou
  - Tipo de dados incorreto
  - Parâmetros obrigatórios ausentes
  - Erro de negócio

### 401 - Unauthorized
- **Cor**: Laranja/Amarelo
- **Ícone**: 🔐
- **Casos de uso**:
  - Usuário não autenticado
  - Token expirado

### 403 - Forbidden
- **Cor**: Ciano/Roxo escuro
- **Ícone**: 🔒
- **Casos de uso**:
  - Usuário sem permissão
  - Acesso negado a recurso

### 404 - Not Found
- **Cor**: Azul claro/Rosa
- **Ícone**: 🔍
- **Casos de uso**:
  - Página/recurso não existe
  - URL inválida

### 500 - Internal Server Error
- **Cor**: Laranja/Vermelho
- **Ícone**: ⚠️
- **Casos de uso**:
  - Erro inesperado na aplicação
  - Exceção não tratada
  - Erro de banco de dados

### 503 - Service Unavailable
- **Cor**: Creme/Salmão
- **Ícone**: 🚧
- **Casos de uso**:
  - Sistema em manutenção
  - Recurso indisponível temporariamente

## Padrão de Resposta

Cada erro recebe os seguintes atributos do modelo:

```java
model.addAttribute("status", 400);                    // HTTP Status
model.addAttribute("message", "Mensagem do erro");    // Mensagem principal
model.addAttribute("detail", "Detalhes adicionais"); // Detalhes específicos
model.addAttribute("uri", "/path/to/resource");       // URI da requisição
model.addAttribute("timestamp", LocalDateTime.now()); // Timestamp do erro
model.addAttribute("errorCode", "ERR_CODE");          // Para BusinessException
model.addAttribute("errors", fieldErrors);            // Erros de validação (se houver)
```

## Logging

Todos os erros são registrados automaticamente:

```java
logger.warn("Recurso não encontrado: {} {}", method, url);           // 404
logger.warn("Acesso negado para: {}", uri);                          // 403
logger.error("Argumento inválido enviado para: {}", uri);            // 400
logger.error("Erro de negócio: {}", message);                        // BusinessException
logger.error("Erro interno do servidor", exception);                 // 500
```

## Configuração no application.yaml

```yaml
spring:
  mvc:
    throw-exception-if-no-handler-found: true

server:
  error:
    whitelabel:
      enabled: false
    path: /error
    include-message: always
    include-binding-errors: always
    include-stacktrace: on-param
    include-exception: false
```

## Fluxo de Processamento

```
Usuario faz requisição HTTP
        ↓
    ↙───┴───↖
  /         \
Erro HTTP  Exceção explícita
  |            |
  v            v
CustomErrorController → error.html
GlobalExceptionHandler → error.html
  |
  v
Template renderizado dinamicamente
```

## Boas Práticas

1. **Use BusinessException para erros de negócio**
   - Sempre que a regra de negócio falhar
   - Evite lançar exceções genéricas

2. **Forneça mensagens claras**
   - Evite mensagens genéricas
   - Inclua detalhes que ajudem o usuário
   - Sugira próximos passos

3. **Use códigos de erro padronizados**
   - Exemplo: `ERR_VALIDATION_FAILED`, `ERR_RESOURCE_NOT_FOUND`
   - Facilita logging e rastreamento
   - Útil para suporte técnico

4. **Não exponha detalhes técnicos**
   - Stack traces devem ir apenas nos logs
   - Template mostra apenas mensagens amigáveis

5. **Adicione contexto quando possível**
   - Inclua o que o usuário tentou fazer
   - Mostre que dados são inválidos

6. **Use traduções apropriadas**
   - Mensagens devem estar em português
   - Ícones são universais

## Exemplos de Erros Customizados

### Validação de Range
```java
if (value < min || value > max) {
    throw new BusinessException(
        "Valor fora do intervalo permitido",
        "ERR_VALUE_OUT_OF_RANGE",
        String.format("O valor deve estar entre %d e %d. Recebido: %d", min, max, value)
    );
}
```

### Recurso Duplicado
```java
if (repository.exists(field, value)) {
    throw new BusinessException(
        "Recurso já existe",
        "ERR_RESOURCE_ALREADY_EXISTS",
        field + " '" + value + "' já está registrado no sistema."
    );
}
```

### Operação Não Permitida
```java
if (!canPerformOperation()) {
    throw new BusinessException(
        "Operação não permitida",
        "ERR_OPERATION_NOT_ALLOWED",
        "Você não pode executar esta ação no estado atual do recurso."
    );
}
```

## Testes

Para testar os diferentes tipos de erro:

- **404**: Acesse uma URL inexistente `/pagina-inexistente`
- **400**: POST com dados inválidos
- **403**: Acesse um recurso sem permissão (ex: `/admin`)
- **500**: Faça algo que lance uma exceção não tratada
- **BusinessException**: Use o `ErrorHandlingExampleController` para testes

Exemplo controller para testes: `src/main/java/com/victorqueiroga/serverwatch/controller/ErrorHandlingExampleController.java`

## Troubleshooting

### Template error.html não está sendo renderizado
1. Verifique se o arquivo existe em `src/main/resources/templates/error.html`
2. Limpe a cache do Thymeleaf: `mvn clean`
3. Reinicie a aplicação

### CustomErrorController não está sendo chamado
1. Verifique a configuração: `server.error.whitelabel.enabled: false`
2. Verifique a configuração: `server.error.path: /error`

### GlobalExceptionHandler não está capturando exceções
1. Verifique se a classe tem `@ControllerAdvice`
2. Verifique se o método tem `@ExceptionHandler`
3. Garanta que a exceção é do tipo esperado

### Erros de validação não aparecem
1. Use `@Valid` na anotação do parâmetro
2. Verifique se `include-binding-errors: always` está configurado
