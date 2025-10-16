/**
 * JavaScript específico para Dashboard de Monitoramento
 * Arquivo: /static/js/monitoring-dashboard.js
 */

let refreshInterval = 30000; // 30 segundos por padrão
let intervalId;
let soundAlerts = true;
let desktopNotifications = true;

// Inicialização da página
document.addEventListener('DOMContentLoaded', function() {
    console.log('Inicializando página de monitoramento');
    
    // Carrega configurações salvas
    loadSettings();
    
    // Carrega dados iniciais
    loadMonitoringData();
    
    // Inicia atualização automática
    startAutoRefresh();
    
    // Solicita permissão para notificações
    if (desktopNotifications && 'Notification' in window) {
        Notification.requestPermission();
    }
});

// Carrega dados de monitoramento (usa cache se disponível)
async function loadMonitoringData() {
    try {
        console.log('Carregando dados de monitoramento via cache...');
        const [summaryResponse, serversResponse] = await Promise.all([
            fetch('/api/monitoring/summary'),
            fetch('/api/monitoring/servers')
        ]);

        const summary = await summaryResponse.json();
        const servers = await serversResponse.json();
        console.log('Resumo:', summary);
        console.log('Servidores:', servers);

        updateSummaryCards(summary);
        displayServers(servers);

        // Atualiza timestamp
        document.getElementById('last-update-time').textContent = new Date().toLocaleTimeString();

    } catch (error) {
        console.error('Erro ao carregar dados de monitoramento:', error);
        showError('Erro ao carregar dados de monitoramento');
    }
}

// Força refresh completo via SNMP (limpa cache)
async function forceRefreshData() {
    try {
        console.log('=== FORÇANDO REFRESH SNMP COMPLETO ===');
        
        // Mostra loading
        const container = document.getElementById('servers-container');
        const loadingHtml = `
            <div class="col-12 text-center py-5">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Coletando dados SNMP...</span>
                </div>
                <h5 class="text-primary mt-3">Coletando dados SNMP dos servidores...</h5>
                <p class="text-muted">Aguarde, isso pode levar alguns segundos.</p>
            </div>
        `;
        container.innerHTML = loadingHtml;
        
        // Força refresh via API
        const [summaryResponse, refreshResponse] = await Promise.all([
            fetch('/api/monitoring/summary'),
            fetch('/api/monitoring/refresh', { method: 'POST' })
        ]);

        const summary = await summaryResponse.json();
        const servers = await refreshResponse.json();
        
        console.log('=== REFRESH SNMP CONCLUÍDO ===');
        console.log('Resumo atualizado:', summary);
        console.log('Servidores atualizados:', servers);

        updateSummaryCards(summary);
        displayServers(servers);

        // Atualiza timestamp
        document.getElementById('last-update-time').textContent = new Date().toLocaleTimeString();
        
        // Mostra sucesso
        showSuccess('Dados atualizados via SNMP com sucesso!');

    } catch (error) {
        console.error('Erro no refresh forçado:', error);
        showError('Erro ao atualizar dados via SNMP: ' + error.message);
    }
}

// Atualiza cards de resumo
function updateSummaryCards(summary) {
    document.getElementById('total-servers').textContent = summary.totalServers;
    document.getElementById('online-servers').textContent = summary.onlineServers;
    document.getElementById('offline-servers').textContent = summary.offlineServers;
    document.getElementById('warning-servers').textContent = summary.warningServers;
}

// Exibe lista de servidores
function displayServers(servers) {
    const container = document.getElementById('servers-container');
    
    if (servers.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center py-5">
                <i class="fas fa-server fa-3x text-muted mb-3"></i>
                <h5 class="text-muted">Nenhum servidor configurado</h5>
                <p class="text-muted">Adicione servidores para começar o monitoramento.</p>
                <a href="/servers/new" class="btn btn-primary">
                    <i class="fas fa-plus me-1"></i> Adicionar Servidor
                </a>
            </div>
        `;
        return;
    }
    
    container.innerHTML = servers.map(server => createServerCard(server)).join('');
}

// Cria card de servidor
function createServerCard(server) {
    const statusClass = server.status.toLowerCase();
    const statusIcon = getStatusIcon(server.status);
    const statusText = getStatusText(server.status);
    
    return `
        <div class="col-lg-4 col-md-6 mb-4">
            <div class="card server-card ${statusClass}" data-server-id="${server.serverId}">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <span class="status-indicator status-${statusClass}"></span>
                        <strong>${server.serverName}</strong>
                    </div>
                    <div class="dropdown">
                        <button class="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown">
                            <i class="fas fa-ellipsis-v"></i>
                        </button>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="#" onclick="refreshServer(${server.serverId})">
                                <i class="fas fa-sync-alt me-1"></i> Atualizar
                            </a></li>
                            <li><a class="dropdown-item" href="/servers/${server.serverId}">
                                <i class="fas fa-eye me-1"></i> Detalhes
                            </a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="/servers/${server.serverId}/edit">
                                <i class="fas fa-edit me-1"></i> Editar
                            </a></li>
                        </ul>
                    </div>
                </div>
                
                <div class="card-body">
                    <div class="row mb-3">
                        <div class="col-6">
                            <small class="text-muted">Status</small><br>
                            <span class="badge bg-${getStatusBadgeColor(server.status)}">${statusText}</span>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">IP Address</small><br>
                            <code>${server.ipAddress}</code>
                        </div>
                    </div>
                    
                    ${server.online ? createMetricsSection(server) : createOfflineSection(server)}
                </div>
                
                <div class="card-footer">
                    <small class="text-muted">
                        <i class="far fa-clock me-1"></i>
                        ${server.lastCheck ? formatDateTime(server.lastCheck) : 'Nunca verificado'}
                    </small>
                </div>
            </div>
        </div>
    `;
}

// Cria seção de métricas para servidores online
function createMetricsSection(server) {
    // Debug: Log dos dados do servidor
    console.log('Criando métricas para servidor:', server.serverName);
    console.log('CPU Load:', server.cpuLoad1Min);
    console.log('Memory Total:', server.memoryTotal, 'Used:', server.memoryUsed, 'Usage%:', server.memoryUsagePercent);
    console.log('Disk Total:', server.diskTotal, 'Used:', server.diskUsed, 'Usage%:', server.diskUsagePercent);
    console.log('Interface Count:', server.interfaceCount);
    
    return `
        <div class="metrics-section">
            ${server.uptime ? `
            <div class="metric-row mb-2">
                <small class="text-muted">
                    <i class="fas fa-clock me-1"></i>Uptime
                </small><br>
                <span class="badge bg-info">${server.uptime}</span>
            </div>
            ` : ''}
            
            <!-- CPU Metrics -->
            <div class="metric-row mb-2">
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-muted">
                        <i class="fas fa-microchip me-1"></i>CPU Load
                    </small>
                    <span class="metric-badge badge ${getCpuBadgeColor(server.cpuLoad1Min || 0)}">
                        ${server.cpuLoad1Min !== null ? server.cpuLoad1Min.toFixed(1) : 'N/A'}%
                    </span>
                </div>
                <div class="progress mt-1" style="height: 6px;">
                    <div class="progress-bar ${getCpuProgressColor(server.cpuLoad1Min || 0)}" 
                         style="width: ${server.cpuLoad1Min ? Math.min(server.cpuLoad1Min, 100) : 0}%"></div>
                </div>
            </div>
            
            <!-- Memory Metrics -->
            <div class="metric-row mb-2">
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-muted">
                        <i class="fas fa-memory me-1"></i>Memória RAM
                    </small>
                    <span class="metric-badge badge ${getMemoryBadgeColor(server.memoryUsagePercent || 0)}">
                        ${server.memoryUsagePercent ? server.memoryUsagePercent.toFixed(1) : 'N/A'}%
                    </span>
                </div>
                ${server.memoryTotal && server.memoryUsed ? `
                <div class="metric-details">
                    <small class="text-muted">
                        ${formatBytes(server.memoryUsed * 1024 * 1024)} / ${formatBytes(server.memoryTotal * 1024 * 1024)}
                        ${server.memoryAvailable ? ` (${formatBytes(server.memoryAvailable * 1024 * 1024)} livre)` : ''}
                    </small>
                </div>
                ` : `
                <div class="metric-details">
                    <small class="text-muted">Dados de memória não disponíveis</small>
                </div>
                `}
                <div class="progress mt-1" style="height: 6px;">
                    <div class="progress-bar ${getMemoryProgressColor(server.memoryUsagePercent || 0)}" 
                         style="width: ${server.memoryUsagePercent || 0}%"></div>
                </div>
            </div>
            
            <!-- Disk Metrics -->
            <div class="metric-row mb-2">
                <div class="d-flex justify-content-between align-items-center">
                    <small class="text-muted">
                        <i class="fas fa-hdd me-1"></i>Espaço em Disco
                    </small>
                    <span class="metric-badge badge ${getDiskBadgeColor(server.diskUsagePercent || 0)}">
                        ${server.diskUsagePercent ? server.diskUsagePercent.toFixed(1) : 'N/A'}%
                    </span>
                </div>
                ${server.diskTotal && server.diskUsed ? `
                <div class="metric-details">
                    <small class="text-muted">
                        ${formatBytes(server.diskUsed * 1024 * 1024 * 1024)} / ${formatBytes(server.diskTotal * 1024 * 1024 * 1024)}
                        ${server.diskAvailable ? ` (${formatBytes(server.diskAvailable * 1024 * 1024 * 1024)} livre)` : ''}
                    </small>
                </div>
                ` : `
                <div class="metric-details">
                    <small class="text-muted">Dados de disco não disponíveis</small>
                </div>
                `}
                <div class="progress mt-1" style="height: 6px;">
                    <div class="progress-bar ${getDiskProgressColor(server.diskUsagePercent || 0)}" 
                         style="width: ${server.diskUsagePercent || 0}%"></div>
                </div>
            </div>
            
            <!-- Network Interfaces -->
            <div class="metric-row">
                <small class="text-muted">
                    <i class="fas fa-network-wired me-1"></i>Interfaces de Rede
                </small><br>
                <span class="badge bg-secondary">${server.interfaceCount || 'N/A'}</span>
            </div>
        </div>
    `;
}

// Cria seção para servidores offline
function createOfflineSection(server) {
    return `
        <div class="text-center py-3">
            <i class="fas fa-exclamation-triangle fa-2x text-warning mb-2"></i>
            <div class="text-muted">Servidor indisponível</div>
            ${server.errorMessage ? `<small class="text-danger">${server.errorMessage}</small>` : ''}
        </div>
    `;
}

// Funções auxiliares para cores e ícones
function getStatusIcon(status) {
    const icons = {
        'ONLINE': 'fas fa-check-circle text-success',
        'OFFLINE': 'fas fa-times-circle text-danger',
        'WARNING': 'fas fa-exclamation-triangle text-warning',
        'UNKNOWN': 'fas fa-question-circle text-secondary'
    };
    return icons[status] || icons['UNKNOWN'];
}

function getStatusText(status) {
    const texts = {
        'ONLINE': 'Online',
        'OFFLINE': 'Offline',
        'WARNING': 'Alerta',
        'UNKNOWN': 'Desconhecido',
        'PENDING': 'Carregando...'
    };
    return texts[status] || 'Desconhecido';
}

function getStatusBadgeColor(status) {
    const colors = {
        'ONLINE': 'success',
        'OFFLINE': 'danger',
        'WARNING': 'warning',
        'UNKNOWN': 'secondary',
        'PENDING': 'info'
    };
    return colors[status] || 'secondary';
}

function getCpuBadgeColor(cpuLoad) {
    if (cpuLoad > 80) return 'bg-danger';
    if (cpuLoad > 60) return 'bg-warning';
    return 'bg-success';
}

function getMemoryBadgeColor(memUsage) {
    if (memUsage > 85) return 'bg-danger';
    if (memUsage > 70) return 'bg-warning';
    return 'bg-success';
}

function getDiskBadgeColor(diskUsage) {
    if (diskUsage > 90) return 'bg-danger';
    if (diskUsage > 75) return 'bg-warning';
    return 'bg-success';
}

// Atualiza servidor específico
async function refreshServer(serverId) {
    try {
        const response = await fetch(`/api/monitoring/servers/${serverId}/refresh`, {
            method: 'POST'
        });
        
        if (response.ok) {
            // Recarrega dados após atualização
            setTimeout(() => loadMonitoringData(), 1000);
        } else {
            showError('Erro ao atualizar servidor');
        }
    } catch (error) {
        console.error('Erro ao atualizar servidor:', error);
        showError('Erro ao atualizar servidor');
    }
}

// Atualiza todos os servidores
async function refreshAllServers() {
    const refreshBtn = document.querySelector('.refresh-btn');
    refreshBtn.classList.add('spinning');
    
    try {
        await loadMonitoringData();
    } finally {
        setTimeout(() => refreshBtn.classList.remove('spinning'), 1000);
    }
}

// Inicia atualização automática
function startAutoRefresh() {
    if (intervalId) {
        clearInterval(intervalId);
    }
    
    intervalId = setInterval(loadMonitoringData, refreshInterval);
    console.log(`Auto-refresh iniciado com intervalo de ${refreshInterval}ms`);
}

// Carrega configurações salvas
function loadSettings() {
    refreshInterval = parseInt(localStorage.getItem('monitoring-refresh-interval') || '30000');
    soundAlerts = localStorage.getItem('monitoring-sound-alerts') !== 'false';
    desktopNotifications = localStorage.getItem('monitoring-desktop-notifications') !== 'false';
    
    // Atualiza interface se os elementos existirem
    const refreshSelect = document.getElementById('refresh-interval');
    const soundCheck = document.getElementById('sound-alerts');
    const notifCheck = document.getElementById('desktop-notifications');
    
    if (refreshSelect) refreshSelect.value = refreshInterval / 1000;
    if (soundCheck) soundCheck.checked = soundAlerts;
    if (notifCheck) notifCheck.checked = desktopNotifications;
}

// Salva configurações
function saveSettings() {
    const refreshSelect = document.getElementById('refresh-interval');
    const soundCheck = document.getElementById('sound-alerts');
    const notifCheck = document.getElementById('desktop-notifications');
    
    if (refreshSelect) refreshInterval = parseInt(refreshSelect.value) * 1000;
    if (soundCheck) soundAlerts = soundCheck.checked;
    if (notifCheck) desktopNotifications = notifCheck.checked;
    
    // Salva no localStorage
    localStorage.setItem('monitoring-refresh-interval', refreshInterval.toString());
    localStorage.setItem('monitoring-sound-alerts', soundAlerts.toString());
    localStorage.setItem('monitoring-desktop-notifications', desktopNotifications.toString());
    
    // Reinicia auto-refresh com novo intervalo
    startAutoRefresh();
    
    // Fecha modal
    const modal = document.getElementById('settingsModal');
    if (modal && bootstrap.Modal.getInstance(modal)) {
        bootstrap.Modal.getInstance(modal).hide();
    }
    
    showSuccess('Configurações salvas com sucesso!');
}

// Utilitários
function formatDateTime(dateTimeStr) {
    return new Date(dateTimeStr).toLocaleString('pt-BR');
}

function formatBytes(bytes) {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

// Funções para cores dos progress bars
function getCpuProgressColor(usage) {
    if (usage >= 90) return 'bg-danger';
    if (usage >= 80) return 'bg-warning';
    if (usage >= 60) return 'bg-info';
    return 'bg-success';
}

function getMemoryProgressColor(usage) {
    if (usage >= 95) return 'bg-danger';
    if (usage >= 85) return 'bg-warning';
    if (usage >= 70) return 'bg-info';
    return 'bg-success';
}

function getDiskProgressColor(usage) {
    if (usage >= 95) return 'bg-danger';
    if (usage >= 90) return 'bg-warning';
    if (usage >= 80) return 'bg-info';
    return 'bg-success';
}

function showError(message) {
    // Implementar sistema de notificações
    console.error(message);
    alert(message); // Temporário
}

function showSuccess(message) {
    console.log(message);
    alert(message); // Temporário
}