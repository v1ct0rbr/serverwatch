# Estrutura Thymeleaf - ServerWatch

Esta é a estrutura de pastas e arquivos criada para o projeto ServerWatch utilizando o Thymeleaf como template engine.

## 📁 Estrutura de Diretórios

```
src/main/resources/
├── templates/                          # Templates Thymeleaf
│   ├── layout/                         # Layouts principais
│   │   └── main.html                   # Layout base da aplicação
│   ├── fragments/                      # Fragmentos reutilizáveis
│   │   ├── header.html                 # Cabeçalho da aplicação
│   │   ├── navbar.html                 # Menu de navegação
│   │   └── footer.html                 # Rodapé da aplicação
│   ├── pages/                          # Páginas da aplicação
│   │   └── dashboard.html              # Página do dashboard
│   └── error/                          # Páginas de erro
│       ├── 404.html                    # Página não encontrada
│       └── 500.html                    # Erro interno do servidor
└── static/                             # Recursos estáticos
    ├── css/                            # Arquivos CSS
    │   └── main.css                    # Estilos principais
    ├── js/                             # Arquivos JavaScript
    │   └── main.js                     # Script principal
    ├── images/                         # Imagens e ícones
    └── lib/                            # Bibliotecas externas
```

## 🎨 Arquivos Principais

### 1. Layout Base (`layout/main.html`)
- Layout principal que define a estrutura HTML base
- Inclui Bootstrap 5 para responsividade
- Utiliza fragmentos para header, navbar e footer
- Define meta tags e recursos CSS/JS

### 2. Fragmentos (`fragments/`)
- **header.html**: Cabeçalho com título e informações de status
- **navbar.html**: Menu de navegação responsivo com links principais
- **footer.html**: Rodapé com informações do sistema

### 3. Páginas (`pages/`)
- **dashboard.html**: Página principal com cards de status e métricas

### 4. Páginas de Erro (`error/`)
- **404.html**: Página customizada para recursos não encontrados
- **500.html**: Página customizada para erros internos do servidor

### 5. Recursos Estáticos (`static/`)
- **main.css**: Estilos customizados com variáveis CSS e animações
- **main.js**: JavaScript principal com funções utilitárias

## 🚀 Como Usar

### 1. Criando Novas Páginas
Para criar uma nova página, siga este padrão:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" th:replace="~{layout/main :: layout (~{::content})}">

<div th:fragment="content">
    <!-- Seu conteúdo aqui -->
    <h2>Minha Nova Página</h2>
    <p>Conteúdo da página...</p>
</div>

</html>
```

### 2. Usando Fragmentos
Para incluir fragmentos em suas páginas:

```html
<!-- Incluir um fragmento completo -->
<div th:replace="~{fragments/header :: header}"></div>

<!-- Incluir com parâmetros -->
<nav th:replace="~{fragments/navbar :: navbar}"></nav>
```

### 3. Recursos Estáticos
Para referenciar recursos estáticos:

```html
<!-- CSS -->
<link th:href="@{/css/main.css}" rel="stylesheet">

<!-- JavaScript -->
<script th:src="@{/js/main.js}"></script>

<!-- Imagens -->
<img th:src="@{/images/logo.png}" alt="Logo">
```

## 🎯 Funcionalidades Incluídas

### 1. Design Responsivo
- Bootstrap 5 integrado
- Layout adaptável para desktop e mobile
- Componentes responsivos

### 2. Sistema de Navegação
- Menu principal com indicação da página ativa
- Navegação responsiva para dispositivos móveis
- Dropdown para opções do usuário

### 3. Cards de Status
- Cards informativos com métricas do sistema
- Animações CSS suaves
- Design moderno com shadows e hover effects

### 4. Tratamento de Erros
- Páginas customizadas para erros 404 e 500
- Design consistente com o resto da aplicação
- Navegação para retorno ao sistema

### 5. JavaScript Utilitário
- Funções para AJAX requests
- Validação de formulários
- Sistema de notificações
- Formatação de números e datas

## 🎨 Customização

### Cores e Temas
As cores principais estão definidas em variáveis CSS no arquivo `main.css`:

```css
:root {
    --primary-color: #0d6efd;
    --secondary-color: #6c757d;
    --success-color: #198754;
    --warning-color: #ffc107;
    --danger-color: #dc3545;
    /* ... outras cores */
}
```

### Animações
O arquivo CSS inclui várias classes de animação:
- `.fade-in`: Animação de fade com movimento
- `.slide-in`: Animação de slide lateral
- Hover effects nos cards e botões

## 📝 Próximos Passos

1. **Criar Controllers**: Implemente os controllers Spring Boot para servir as páginas
2. **Adicionar Modelos**: Crie as classes de modelo para os dados
3. **Implementar APIs**: Desenvolva endpoints REST para dados dinâmicos
4. **Adicionar Formulários**: Crie formulários para gerenciamento de servidores
5. **Integrar Banco de Dados**: Configure JPA/Hibernate para persistência

## 🔧 Dependências Necessárias

Certifique-se de que o `pom.xml` inclui as dependências do Thymeleaf:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

A estrutura está pronta para desenvolvimento! 🎉