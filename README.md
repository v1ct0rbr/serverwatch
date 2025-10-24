# ServerWatch

ServerWatch é um sistema de monitoramento de servidores que permite a visualização de métricas de performance via SNMP e autenticação via Keycloak.

## Funcionalidades

### Autenticação via Keycloak

O sistema utiliza o Keycloak para autenticação e autorização. O Keycloak é um sistema de gestão de identidade e acesso que fornece autenticação, autorização e gerenciamento de tokens.

### Monitoramento de Servidores via SNMP

O sistema coleta métricas de performance via SNMP e as exibe em forma de gráficos e tabelas. Os servidores monitorados podem ser adicionados via interface web ou via API.

### Configuração dos Servidores Monitorados

A configuração dos servidores monitorados é feita via interface web ou via API. Os servidores podem ser adicionados ou removidos e suas configurações podem ser alteradas.

## Configuração do Keycloak

### Configuração do Client

Para configurar o client Keycloak, é necessário criar um client no Keycloak Admin Console e configurar as seguintes informações:

* Client ID
* Client Secret
* Valid Redirect URIs
* Web Origins

### Configuração do Realm

Para configurar o realm, é necessário criar um realm no Keycloak Admin Console e configurar as seguintes informações:

* Nome do Realm
* URL do Realm

### Configuração do Usuário

Para configurar o usuário, é necessário criar um usuário no Keycloak Admin Console e configurar as seguintes informações:

* Nome do Usuário
* Email do Usuário
* Senha do Usuário

## Configuração do Servidor Monitorado

### Adicionar um Servidor Monitorado

Para adicionar um servidor monitorado, é necessário fornecer as seguintes informações:

* Nome do Servidor
* Endereço IP do Servidor
* Porta do Servidor
* Tipo do Servidor
* Localização do Servidor
* Comunidade SNMP do Servidor

### Remover um Servidor Monitorado

Para remover um servidor monitorado, é necessário fornecer o nome do servidor.

## API

A API do sistema fornece as seguintes funcionalidades:

* Adicionar um servidor monitorado
* Remover um servidor monitorado
* Listar todos os servidores monitorados
* Obter as métricas de performance de um servidor monitorado

## Exemplo de Uso

Para adicionar um servidor monitorado, é necessário fazer uma requisição POST para a API com as seguintes informações:

* Nome do Servidor
* Endereço IP do Servidor
* Porta do Servidor
* Tipo do Servidor
* Localização do Servidor
* Comunidade SNMP do Servidor

Exemplo de requisição:
