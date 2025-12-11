# Guia Rápido de Teste - Sistema de Tratamento de Erros

## 🚀 Como Iniciar a Aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## 🧪 Testes Recomendados

### 1. Erro 404 - Página Não Encontrada
```
URL: http://localhost:8080/pagina-que-nao-existe
Resultado Esperado: Template error.html com status 404, ícone 🔍, cor azul claro
```

### 2. Erro 400 - ID Inválido
```
URL: http://localhost:8080/example-error/validate/abc
Resultado Esperado: Template error.html com status 400, mensagem sobre ID inválido
```

### 3. Erro 400 - Porta Inválida
```
URL: http://localhost:8080/example-error/port/70000
Resultado Esperado: Template error.html com status 400, mensagem sobre porta inválida
```

### 4. Erro 400 - Servidor Não Encontrado
```
URL: http://localhost:8080/example-error/server/0
Resultado Esperado: Template error.html com status 400, código de erro ERR_SERVER_NOT_FOUND
```

### 5. Erro 400 - Range Inválido
```
URL: http://localhost:8080/example-error/range?min=100&max=10
Resultado Esperado: Template error.html com status 400, mensagem sobre intervalo inválido
```

### 6. Erro 500 - Erro Não Tratado
```
URL: http://localhost:8080/example-error/unhandled-error
Resultado Esperado: Template error.html com status 500, ícone ⚠️, cor vermelho/laranja
```

## 🔍 Verificações Visuais

### Status Code 400
- [ ] Fundo com gradient rosa/vermelho
- [ ] Ícone ❌
- [ ] Título "Requisição Inválida"
- [ ] Box informativo em azul

### Status Code 404
- [ ] Fundo com gradient azul claro/rosa
- [ ] Ícone 🔍
- [ ] Título "Página Não Encontrada"
- [ ] Mensagem sobre página inexistente

### Status Code 500
- [ ] Fundo com gradient laranja/vermelho
- [ ] Ícone ⚠️
- [ ] Título "Erro Interno do Servidor"
- [ ] Box informativo vermelho

### Status Code 403
- [ ] Fundo com gradient ciano/roxo
- [ ] Ícone 🔒
- [ ] Título "Acesso Negado"

## 📊 Verificações de Funcionalidade

### Template Renderiza Corretamente
- [ ] Cores diferentes para cada status code
- [ ] Ícones aparecem corretamente
- [ ] Mensagens são claras e em português
- [ ] Timestamp aparece formatado

### Dados São Exibidos
- [ ] Status code aparece em grande
- [ ] URI da requisição é mostrada
- [ ] Timestamp está no formato brasileiro (DD/MM/YYYY HH:MM:SS)
- [ ] Botões "Voltar" e "Início" funcionam

### Erros de Validação
- [ ] Para POST com dados inválidos, aparecem campos com erro
- [ ] Cada erro mostra o nome do campo e mensagem

### Erros de Negócio (BusinessException)
- [ ] Aparece código do erro (ERR_...)
- [ ] Mensagem é clara
- [ ] Detalhes adicionais aparecem

## 📝 Logs para Verificar

Abra o console da aplicação e procure por:

### Para 404:
```
WARN - Recurso não encontrado: GET http://localhost:8080/pagina-inexistente
```

### Para BusinessException:
```
ERROR - Erro de negócio: Servidor com este nome já existe
```

### Para exceção geral:
```
ERROR - Erro interno do servidor
java.lang.Exception: ...
```

## 🔧 Solução de Problemas

### Template não renderiza
1. Verifique se `error.html` existe em `src/main/resources/templates/`
2. Verifique se a aplicação foi recompilada (`mvn clean compile`)
3. Reinicie a aplicação

### Cores não aparecem corretamente
1. Limpe o cache do navegador (Ctrl+Shift+Delete)
2. Verifique se CSS inline está sendo renderizado
3. Inspecione o elemento HTML (F12 → Elements)

### Logs não aparecem
1. Verifique se `logging.level.root: INFO` ou superior em `application.yaml`
2. Verifique se a classe `GlobalExceptionHandler` tem `@ControllerAdvice`

## 📦 Arquivos Importantes

| Arquivo | Propósito |
|---------|-----------|
| `GlobalExceptionHandler.java` | Trata exceções explícitas |
| `CustomErrorController.java` | Trata erros HTTP |
| `error.html` | Template único para todos os erros |
| `ErrorHandlingExampleController.java` | Exemplos de teste |
| `application.yaml` | Configurações do Spring |

## ✅ Checklist Final

- [ ] Compilação bem-sucedida (`mvn clean compile`)
- [ ] Aplicação inicia sem erros (`mvn spring-boot:run`)
- [ ] 404 renderiza corretamente
- [ ] 400 renderiza corretamente
- [ ] 500 renderiza corretamente
- [ ] Logs aparecem no console
- [ ] Sem erros JavaScript no console do navegador
- [ ] Botões funcionam corretamente
- [ ] Template é responsivo (mobile)

## 🚨 Se Algo Não Funcionar

1. Verifique a compilação:
   ```bash
   mvn clean compile -X | tail -50
   ```

2. Verifique os logs:
   ```bash
   mvn spring-boot:run | grep -E "ERROR|WARN|Exception"
   ```

3. Verifique o navegador:
   - Abra Developer Tools (F12)
   - Vá para "Console"
   - Verifique se há erros JavaScript

4. Verifique o servidor:
   - Verifique se está na porta 8080
   - Verifique se CustomErrorController foi carregado
   - Verifique se GlobalExceptionHandler foi carregado

Contate o desenvolvedor se o problema persistir!
