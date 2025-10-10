-- V2_0_0__create_users_table.sql
-- Criação das tabelas para gerenciamento de usuários integrados com Keycloak

-- Tabela principal de usuários
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    keycloak_id VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255),
    first_login TIMESTAMP,
    last_login TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    preferences TEXT,
    theme VARCHAR(20) DEFAULT 'AUTO',
    language VARCHAR(10) DEFAULT 'pt-BR',
    timezone VARCHAR(50) DEFAULT 'America/Sao_Paulo',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela para roles específicas da aplicação
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índices para melhor performance
CREATE INDEX idx_user_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_active ON users(active);
CREATE INDEX idx_user_last_login ON users(last_login);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);

-- Função para atualizar o campo updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger para atualizar updated_at na tabela users
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Comentários nas tabelas e colunas
COMMENT ON TABLE users IS 'Usuários da aplicação integrados com Keycloak';
COMMENT ON COLUMN users.keycloak_id IS 'ID do usuário no Keycloak (subject do JWT)';
COMMENT ON COLUMN users.username IS 'Nome de usuário sincronizado com Keycloak';
COMMENT ON COLUMN users.email IS 'Email do usuário sincronizado com Keycloak';
COMMENT ON COLUMN users.full_name IS 'Nome completo sincronizado com Keycloak';
COMMENT ON COLUMN users.first_login IS 'Primeiro acesso à aplicação';
COMMENT ON COLUMN users.last_login IS 'Último acesso à aplicação';
COMMENT ON COLUMN users.active IS 'Se o usuário está ativo na aplicação';
COMMENT ON COLUMN users.preferences IS 'Preferências do usuário em formato JSON';
COMMENT ON COLUMN users.theme IS 'Tema preferido: LIGHT, DARK, AUTO';
COMMENT ON COLUMN users.language IS 'Idioma preferido (ISO 639-1)';
COMMENT ON COLUMN users.timezone IS 'Timezone do usuário';

COMMENT ON TABLE user_roles IS 'Roles específicas da aplicação atribuídas aos usuários';
COMMENT ON COLUMN user_roles.role IS 'Role da aplicação: SERVER_MANAGER, ALERT_MANAGER, etc.';