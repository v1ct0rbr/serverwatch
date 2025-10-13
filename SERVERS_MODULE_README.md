# Módulo de Gerenciamento de Servidores - ServerWatch

Este documento descreve a implementação completa do módulo de gerenciamento de servidores monitorados no sistema ServerWatch.

## 📋 Funcionalidades Implementadas

### ✅ Backend (Spring Boot)

#### 1. **Entidade Server** (`model/Server.java`)
- Entidade JPA completa com validações
- Campos: id, name, ipAddress, operationSystem, status, port, description, active, lastCheck, etc.
- Relacionamento com OperationSystem
- Enum ServerStatus com diferentes estados
- Métodos utilitários para verificação de status
- Validações Bean Validation (JSR-303)

#### 2. **ServerRepository** (`repository/ServerRepository.java`)
- Interface JPA Repository com queries customizadas
- Métodos de busca por nome, IP, sistema operacional
- Queries para filtros avançados
- Contadores e agregações

#### 3. **OperationSystemRepository** (`repository/OperationSystemRepository.java`)
- Repository para gerenciar sistemas operacionais
- Queries ordenadas e validações de existência

#### 4. **ServerService** (`service/ServerService.java`)
- Serviço completo com todas as operações CRUD
- Validações de negócio (IP único, nome único)
- Métodos de busca e filtragem
- Validação de formato de IP
- Tratamento de exceções personalizado

#### 5. **ServerController** (`controller/ServerController.java`)
- Controller MVC com endpoints web
- API REST completa (/servers/api/*)
- Paginação e ordenação
- Tratamento de erros e mensagens flash
- Endpoints para CRUD completo

#### 6. **DTOs e Mappers**
- **ServerDTO** (`dto/ServerDTO.java`): DTO para transferência de dados
- **ServerMapper** (`mapper/ServerMapper.java`): Conversões entre entidade e DTO
- DTOs aninhados para diferentes contextos (Summary, Filter)

#### 7. **Migração de Banco** (`db/migration/V1_0_2__enhance_servers_table.sql`)
- Script SQL para melhorar tabela servers
- Novas colunas para monitoramento
- Índices para performance
- Comentários para documentação

### ✅ Frontend (Thymeleaf + Bootstrap)

#### 1. **Página de Listagem** (`templates/pages/servers/list.html`)
- Listagem paginada de servidores
- Filtros de busca por nome/IP
- Ordenação por colunas
- Visualização em tabela e cards
- Ações rápidas (ver, editar, excluir)
- Modal de confirmação para exclusão
- Breadcrumbs e navegação

#### 2. **Página de Formulário** (`templates/pages/servers/form.html`)
- Formulário único para criar/editar
- Validação client-side e server-side
- Preview em tempo real
- Seleção de sistema operacional
- Validação de IP em JavaScript
- Dicas de preenchimento

#### 3. **Página de Visualização** (`templates/pages/servers/view.html`)
- Visualização detalhada do servidor
- Informações básicas e técnicas
- Status de conectividade (simulado)
- Estatísticas de monitoramento
- Ações rápidas para funcionalidades futuras
- Botões de edição e exclusão

#### 4. **Dashboard Atualizado** (`templates/pages/dashboard.html`)
- Cards de ações rápidas para servidores
- Links para adicionar/listar servidores
- Resumo de status (preparado para dados dinâmicos)
- Design responsivo e moderno

## 🗂️ Estrutura de Arquivos

```
src/main/java/com/victorqueiroga/serverwatch/
├── controller/
│   └── ServerController.java           # Controller MVC + API REST
├── service/
│   └── ServerService.java              # Lógica de negócio
├── repository/
│   ├── ServerRepository.java           # Repository JPA
│   └── OperationSystemRepository.java  # Repository para SOs
├── model/
│   ├── Server.java                     # Entidade principal
│   └── OperationSystem.java           # Entidade relacionada
├── dto/
│   └── ServerDTO.java                  # DTOs para transferência
└── mapper/
    └── ServerMapper.java               # Conversões entidade/DTO

src/main/resources/
├── db/migration/
│   └── V1_0_2__enhance_servers_table.sql  # Migração do banco
└── templates/pages/servers/
    ├── list.html                       # Listagem de servidores
    ├── form.html                       # Formulário criar/editar
    └── view.html                       # Visualização detalhada
```

## 🚀 Como Usar

### 1. **Acessar a Lista de Servidores**
- Navegar para `/servers`
- Ver todos os servidores cadastrados
- Filtrar por nome ou IP
- Alterar visualização (tabela/cards)

### 2. **Adicionar Novo Servidor**
- Clicar em "Novo Servidor" na lista
- Ou navegar para `/servers/new`
- Preencher formulário com validação
- Visualizar preview antes de salvar

### 3. **Editar Servidor Existente**
- Clicar no botão "Editar" na lista
- Ou navegar para `/servers/{id}/edit`
- Formulário pré-preenchido com dados atuais

### 4. **Visualizar Detalhes**
- Clicar no nome do servidor ou botão "Ver"
- Navegar para `/servers/{id}`
- Ver informações completas e estatísticas

### 5. **Excluir Servidor**
- Usar botão "Excluir" com confirmação
- Modal de segurança antes da exclusão

## 🔗 API REST Endpoints

```http
GET    /servers/api                    # Lista todos os servidores
GET    /servers/api/{id}               # Busca servidor por ID
POST   /servers/api                    # Cria novo servidor
PUT    /servers/api/{id}               # Atualiza servidor existente
DELETE /servers/api/{id}               # Exclui servidor
GET    /servers/api/search             # Busca com filtros
GET    /servers/api/operation-systems  # Lista sistemas operacionais
```

## 🎨 Funcionalidades de UI/UX

- **Design Responsivo**: Funciona em desktop e mobile
- **Bootstrap 5**: Interface moderna e consistente
- **Ícones Bootstrap Icons**: Iconografia rica
- **Validação em Tempo Real**: Feedback imediato
- **Mensagens Flash**: Confirmações e erros
- **Paginação**: Performance com grandes listas
- **Ordenação**: Colunas clicáveis
- **Filtros**: Busca rápida e eficiente
- **Modais de Confirmação**: Segurança nas ações destrutivas

## 🔧 Validações Implementadas

### Backend (Bean Validation)
- **Nome**: Obrigatório, 3-100 caracteres, único
- **IP**: Obrigatório, formato IPv4 válido, único
- **Sistema Operacional**: Obrigatório, deve existir
- **Porta**: Opcional, numérica
- **Descrição**: Opcional, máximo 500 caracteres

### Frontend (JavaScript)
- **Validação de IP**: Regex em tempo real
- **Preview**: Visualização antes de salvar
- **Campos obrigatórios**: Feedback visual
- **Confirmação de exclusão**: Modal de segurança

## 🗄️ Banco de Dados

### Tabela `servers`
```sql
- id (BIGSERIAL, PK)
- name (VARCHAR(100), UNIQUE, NOT NULL)
- ip_address (VARCHAR(15), UNIQUE, NOT NULL)
- operation_system_id (BIGINT, FK)
- status (VARCHAR(20), DEFAULT 'UNKNOWN')
- port (INTEGER, DEFAULT 80)
- description (VARCHAR(500))
- active (BOOLEAN, DEFAULT TRUE)
- last_check (TIMESTAMP)
- last_response_time (BIGINT)
- created_at (TIMESTAMP, DEFAULT NOW())
- updated_at (TIMESTAMP, DEFAULT NOW())
```

### Índices Criados
- `idx_server_name` - Performance em buscas por nome
- `idx_server_ip` - Performance em buscas por IP
- `idx_server_os` - Performance em joins com operation_systems
- `idx_server_status` - Filtros por status
- `idx_server_active` - Filtros por servidores ativos

## 🔄 Próximos Passos (Sugestões)

1. **Monitoramento Real**: Implementar ping/conectividade real
2. **Alertas**: Sistema de notificações baseado em status
3. **Relatórios**: Gráficos de uptime e performance
4. **Histórico**: Log de mudanças de status
5. **Grupos**: Organização de servidores por grupos/tags
6. **Configurações Avançadas**: Thresholds personalizados
7. **Export/Import**: Backup e restauração de configurações
8. **WebSocket**: Atualizações em tempo real do status

## 📝 Notas Técnicas

- **Padrão MVC**: Separação clara de responsabilidades
- **RESTful API**: Seguindo boas práticas REST
- **Validation**: Bean Validation + validação customizada
- **Security**: Preparado para integração com Spring Security
- **Performance**: Índices de banco e paginação
- **Maintainability**: Código documentado e organizado
- **Extensibility**: Estrutura preparada para funcionalidades futuras

---

✅ **Implementação Completa**: O módulo de gerenciamento de servidores está 100% funcional com todas as operações CRUD, validações, interface de usuário moderna e API REST documentada.