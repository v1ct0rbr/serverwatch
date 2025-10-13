-- Migration V1.0.3: Create alerts, severities and server_metrics tables
-- Create severities table
CREATE TABLE IF NOT EXISTS severities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    severity_level INTEGER NOT NULL UNIQUE CHECK (severity_level > 0),
    color VARCHAR(20) NOT NULL DEFAULT '#6c757d',
    bootstrap_class VARCHAR(50) NOT NULL DEFAULT 'secondary',
    icon VARCHAR(100) DEFAULT 'fas fa-exclamation',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for severities
CREATE INDEX IF NOT EXISTS idx_severity_level ON severities(severity_level);
CREATE INDEX IF NOT EXISTS idx_severity_name ON severities(name);

-- Create alerts table
CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    server_id BIGINT NOT NULL,
    severity_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL DEFAULT 'MONITORING',
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    current_value VARCHAR(200),
    threshold_value VARCHAR(200),
    metric_name VARCHAR(100),
    additional_data TEXT,
    resolved_by VARCHAR(100),
    resolution_comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    CONSTRAINT fk_alert_server FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE,
    CONSTRAINT fk_alert_severity FOREIGN KEY (severity_id) REFERENCES severities(id) ON DELETE RESTRICT
);

-- Create indexes for alerts
CREATE INDEX IF NOT EXISTS idx_alert_server ON alerts(server_id);
CREATE INDEX IF NOT EXISTS idx_alert_severity ON alerts(severity_id);
CREATE INDEX IF NOT EXISTS idx_alert_resolved ON alerts(resolved);
CREATE INDEX IF NOT EXISTS idx_alert_created ON alerts(created_at);
CREATE INDEX IF NOT EXISTS idx_alert_type ON alerts(alert_type);
CREATE INDEX IF NOT EXISTS idx_alert_status ON alerts(status);
CREATE INDEX IF NOT EXISTS idx_alert_metric ON alerts(metric_name);

-- Create server_metrics table
CREATE TABLE IF NOT EXISTS server_metrics (
    id BIGSERIAL PRIMARY KEY,
    server_id BIGINT NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,4) NOT NULL,
    unit VARCHAR(20),
    string_value VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    subcategory VARCHAR(100),
    snmp_oid VARCHAR(200),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_critical BOOLEAN DEFAULT FALSE,
    warning_threshold DECIMAL(15,4),
    critical_threshold DECIMAL(15,4),
    CONSTRAINT fk_metric_server FOREIGN KEY (server_id) REFERENCES servers(id) ON DELETE CASCADE
);

-- Create indexes for server_metrics
CREATE INDEX IF NOT EXISTS idx_metric_server ON server_metrics(server_id);
CREATE INDEX IF NOT EXISTS idx_metric_name ON server_metrics(metric_name);
CREATE INDEX IF NOT EXISTS idx_metric_timestamp ON server_metrics(timestamp);
CREATE INDEX IF NOT EXISTS idx_metric_server_name ON server_metrics(server_id, metric_name);
CREATE INDEX IF NOT EXISTS idx_metric_category ON server_metrics(category);

-- Insert default severities
INSERT INTO severities (name, severity_level, color, bootstrap_class, icon, description) VALUES
    ('Critical', 1, '#dc3545', 'danger', 'fas fa-exclamation-circle', 'Severidade crítica - requer ação imediata'),
    ('High', 2, '#fd7e14', 'warning', 'fas fa-exclamation-triangle', 'Severidade alta - requer atenção urgente'),
    ('Medium', 3, '#ffc107', 'warning', 'fas fa-exclamation', 'Severidade média - requer atenção'),
    ('Low', 4, '#20c997', 'info', 'fas fa-info-circle', 'Severidade baixa - monitoramento'),
    ('Info', 5, '#0dcaf0', 'info', 'fas fa-info', 'Informativo - apenas notificação')
ON CONFLICT (name) DO NOTHING;

-- Add trigger to update updated_at timestamp for severities
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_severities_updated_at 
    BEFORE UPDATE ON severities 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_alerts_updated_at 
    BEFORE UPDATE ON alerts 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments to tables
COMMENT ON TABLE severities IS 'Tabela de severidades dos alertas';
COMMENT ON TABLE alerts IS 'Tabela de alertas do sistema de monitoramento';
COMMENT ON TABLE server_metrics IS 'Tabela de métricas coletadas via SNMP dos servidores';

-- Add column comments
COMMENT ON COLUMN severities.severity_level IS 'Nível da severidade (1=Critical, 2=High, 3=Medium, 4=Low, 5=Info)';
COMMENT ON COLUMN severities.bootstrap_class IS 'Classe CSS do Bootstrap para estilização';
COMMENT ON COLUMN alerts.alert_type IS 'Tipo do alerta (MONITORING, PERFORMANCE, SECURITY, SYSTEM, NETWORK, CUSTOM)';
COMMENT ON COLUMN alerts.status IS 'Status do alerta (OPEN, IN_PROGRESS, RESOLVED, CLOSED, ACKNOWLEDGED)';
COMMENT ON COLUMN server_metrics.category IS 'Categoria da métrica (SYSTEM, CPU, MEMORY, DISK, NETWORK, PROCESS, SERVICE, CUSTOM)';
COMMENT ON COLUMN server_metrics.snmp_oid IS 'OID SNMP usado para coletar a métrica';