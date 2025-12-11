# 🚀 Guia de Início - Sistema de Tratamento de Erros

## ✅ Status Atual

**Problema**: ❌ Resolvido ✅
- Erro 500 ao acessar páginas
- `NoResourceFoundException` não era tratada
- Favicon quebrado no template

## 📋 Pré-requisitos

- ✅ Java 17+ (JDK OpenJDK)
- ✅ Maven 3.9.9+
- ✅ Banco de dados PostgreSQL 13+
- ✅ Keycloak (para autenticação)

Verifique com:
```bash
java -version
mvn --version
```

## 🏃 Iniciar a Aplicação

### 1. Compilar o Projeto
```bash
cd d:\projetos\der\serverwatch
mvn clean compile -DskipTests
```

**Resultado esperado:**
```
✅ Sucesso!
[INFO] BUILD SUCCESS
```

### 2. Executar a Aplicação
```bash
mvn spring-boot:run
```

**Resultado esperado:**
```
2025-12-11T15:45:00.000-03:00  INFO ... - Started ServermonitorApplication in 8.234 seconds
2025-12-11T15:45:00.000-03:00  INFO ... - Serverwatch application started successfully
```

### 3. Acessar a Aplicação

Abra no navegador:
```
http://localhost:8080
```

## 🧪 Testes Rápidos

### Teste 1: Home Page
```bash
curl -i http://localhost:8080
# Esperado: HTTP 200
```

### Teste 2: Página não encontrada
```bash
curl -i http://localhost:8080/pagina-inexistente
# Esperado: HTTP 404 com template error.html
```

### Teste 3: Erro de validação
```bash
curl -i http://localhost:8080/example-error/validate/abc
# Esperado: HTTP 400 com mensagem de erro
```

### Teste 4: Erro geral
```bash
curl -i http://localhost:8080/example-error/unhandled-error
# Esperado: HTTP 500 com template error.html
```

## 📊 Verificações

### No Navegador
1. Abra `http://localhost:8080/pagina-inexistente`
2. Verifique:
   - ✅ Template error.html renderizado
   - ✅ Status code 404 exibido
   - ✅ Ícone 🔍 visível
   - ✅ Cor de fundo em azul claro/rosa
   - ✅ Botões "Voltar" e "Início" funcionam

### Nos Logs
Abra o console onde executou `mvn spring-boot:run` e verifique:

Para 404:
```
WARN ... - Recurso não encontrado: GET http://localhost:8080/pagina-inexistente
```

Para erro de negócio:
```
ERROR ... - Erro de negócio: ID inválido fornecido
```

Para exceção geral:
```
ERROR ... - Erro interno do servidor
java.lang.Exception: ...
```

## 🛑 Parar a Aplicação

No terminal onde está executando:
```bash
Ctrl+C
```

Ou no terminal Maven:
```
^C
```

## 🔧 Troubleshooting

### Erro: "Connection refused"
```
❌ Connection refused: localhost:5432
```
**Solução**: Verifique se PostgreSQL está rodando
```bash
# Windows
sc query postgresql

# Linux/Mac
sudo systemctl status postgresql
```

### Erro: "Keycloak unreachable"
```
❌ Connection refused: keycloak.derpb.com.br:8443
```
**Solução**: Keycloak pode estar offline. Não impede inicialização em desenvolvimento.

### Erro 404 ao acessar home
```
❌ HTTP 404 / not found
```
**Solução**: Verifique se a aplicação iniciou corretamente. Procure por:
```
✅ Started ServermonitorApplication
```

### Erro ao compilar
```
❌ [ERROR] BUILD FAILURE
```
**Solução**: Limpe tudo e recompile
```bash
mvn clean
mvn compile -DskipTests
```

## 📁 Estrutura de Diretórios Importante

```
d:\projetos\der\serverwatch\
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/victorqueiroga/serverwatch/
│   │   │       └── controller/
│   │   │           ├── CustomErrorController.java
│   │   │           └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── templates/
│   │           └── error.html
│   └── test/
├── pom.xml
└── README.md
```

## 📖 Documentação Relacionada

- 📄 [ERROR_HANDLING.md](ERROR_HANDLING.md) - Guia de uso do sistema
- 📄 [SOLUCAO_ERRO_500.md](SOLUCAO_ERRO_500.md) - Detalhes do problema resolvido
- 📄 [TESTE_RAPIDO_ERROS.md](TESTE_RAPIDO_ERROS.md) - Guia de testes
- 📄 [RESUMO_CORRECOES_ERRO_500.md](RESUMO_CORRECOES_ERRO_500.md) - Resumo das correções

## ✅ Checklist de Validação

- [ ] Maven compilou com sucesso
- [ ] Aplicação iniciou sem erros
- [ ] Home page carrega (HTTP 200)
- [ ] Página inexistente retorna 404 com template
- [ ] Logs aparecem no console
- [ ] Botões de erro funcionam
- [ ] Sem erros JavaScript no navegador

## 🆘 Suporte

Se encontrar problemas:

1. **Verifique os logs** - Primeira e melhor fonte de informação
2. **Recompile tudo** - `mvn clean compile -DskipTests`
3. **Limpe cache** - Deletar pasta `target/` pode ajudar
4. **Reinicie a aplicação** - Às vezes resolve problemas de cache

## 🎯 Próximos Passos

Após verificar que tudo funciona:

1. **Testes automatizados**
   ```bash
   mvn test
   ```

2. **Build completo**
   ```bash
   mvn clean package
   ```

3. **Documentação adicional**
   - Criar exemplos de uso do error handling
   - Integrar com sistema de logging centralizado
   - Adicionar monitoramento de erros

## 📞 Contato

Se encontrar problemas não listados aqui, verifique:
- Logs da aplicação
- Stack trace completo (file `target/logs/error.log` se disponível)
- Configuração do ambiente (variáveis de ambiente)

---

**Happy testing! 🎉**
