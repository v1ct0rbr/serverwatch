-- V1_0_2__enhance_servers_table.sql
-- Melhora a tabela de servidores com novas colunas para monitoramento

-- Adicionar novas colunas à tabela servers
ALTER TABLE servers 
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'UNKNOWN',
ADD COLUMN IF NOT EXISTS port INTEGER DEFAULT 80,
ADD COLUMN IF NOT EXISTS description VARCHAR(500),
ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS last_check TIMESTAMP,
ADD COLUMN IF NOT EXISTS last_response_time BIGINT,
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Atualizar comprimento da coluna ip_address para IPv4
ALTER TABLE servers ALTER COLUMN ip_address TYPE VARCHAR(15);

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_server_name ON servers(name);
CREATE INDEX IF NOT EXISTS idx_server_ip ON servers(ip_address);
CREATE INDEX IF NOT EXISTS idx_server_os ON servers(operation_system_id);
CREATE INDEX IF NOT EXISTS idx_server_status ON servers(status);
CREATE INDEX IF NOT EXISTS idx_server_active ON servers(active);

-- Comentários nas colunas para documentação
COMMENT ON COLUMN servers.status IS 'Status atual do servidor: ONLINE, OFFLINE, WARNING, MAINTENANCE, UNKNOWN';
COMMENT ON COLUMN servers.port IS 'Porta principal de monitoramento do servidor';
COMMENT ON COLUMN servers.description IS 'Descrição adicional do servidor';
COMMENT ON COLUMN servers.active IS 'Se o servidor está ativo para monitoramento';
COMMENT ON COLUMN servers.last_check IS 'Última vez que o servidor foi verificado';
COMMENT ON COLUMN servers.last_response_time IS 'Tempo de resposta da última verificação em milissegundos';
COMMENT ON COLUMN servers.created_at IS 'Data de criação do registro do servidor';
COMMENT ON COLUMN servers.updated_at IS 'Data da última atualização do registro';

-- Atualizar dados existentes se houver
UPDATE servers 
SET created_at = CURRENT_TIMESTAMP, 
    updated_at = CURRENT_TIMESTAMP 
WHERE created_at IS NULL;