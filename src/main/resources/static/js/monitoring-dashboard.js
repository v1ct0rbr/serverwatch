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

// Carrega dados de monitoramento
async function loadMonitoringData() {
    try {
        console.log('Carregando dados de monitoramento...');
        
        // Carrega resumo
        const summaryResponse = await fetch('/api/monitoring/summary');
        const summary = await summaryResponse.json();
        updateSummaryCards(summary);
        
        // Carrega lista de servidores
        const serversResponse = await fetch('/api/monitoring/servers');
        const servers = await serversResponse.json();
        displayServers(servers);
        
        // Atualiza timestamp
        document.getElementById('last-update-time').textContent = new Date().toLocaleTimeString();
        
    } catch (error) {
        console.error('Erro ao carregar dados de monitoramento:', error);
        showError('Erro ao carregar dados de monitoramento');
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
    return `
        <div class="row g-2">
            ${server.uptime ? `
            <div class="col-12 mb-2">
                <small class="text-muted">Uptime</small><br>
                <span class="badge bg-info">${server.uptime}</span>
            </div>
            ` : ''}
            
            ${server.cpuLoad1Min !== null ? `
            <div class="col-6">
                <small class="text-muted">CPU Load</small><br>
                <span class="metric-badge badge ${getCpuBadgeColor(server.cpuLoad1Min)}">${server.cpuLoad1Min.toFixed(1)}%</span>
            </div>
            ` : ''}
            
            ${server.memoryUsagePercent !== null ? `
            <div class="col-6">
                <small class="text-muted">Memória</small><br>
                <span class="metric-badge badge ${getMemoryBadgeColor(server.memoryUsagePercent)}">${server.memoryUsagePercent.toFixed(1)}%</span>
            </div>
            ` : ''}
            
            ${server.diskUsagePercent !== null ? `
            <div class="col-6">
                <small class="text-muted">Disco</small><br>
                <span class="metric-badge badge ${getDiskBadgeColor(server.diskUsagePercent)}">${server.diskUsagePercent.toFixed(1)}%</span>
            </div>
            ` : ''}
            
            ${server.interfaceCount !== null ? `
            <div class="col-6">
                <small class="text-muted">Interfaces</small><br>
                <span class="badge bg-secondary">${server.interfaceCount}</span>
            </div>
            ` : ''}
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
        'UNKNOWN': 'Desconhecido'
    };
    return texts[status] || 'Desconhecido';
}

function getStatusBadgeColor(status) {
    const colors = {
        'ONLINE': 'success',
        'OFFLINE': 'danger',
        'WARNING': 'warning',
        'UNKNOWN': 'secondary'
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

function showError(message) {
    // Implementar sistema de notificações
    console.error(message);
    alert(message); // Temporário
}

function showSuccess(message) {
    console.log(message);
    alert(message); // Temporário
}