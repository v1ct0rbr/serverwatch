/**
 * JavaScript específico para Dashboard de Monitoramento
 * Arquivo: /static/js/monitoring-dashboard.js
 */

let refreshInterval = 30000; // 30 segundos por padrão
let intervalId;
let soundAlerts = true;
let desktopNotifications = true;

// Inicialização da página
document.addEventListener('DOMContentLoaded', function () {
    console.log('Inicializando página de monitoramento');

    loadServerInfo();

    document.getElementById('test_connection').addEventListener('click', function () {
        const serverIp = this.getAttribute('data-ip');
        
        // Solicita permissão para notificações dentro do handler de evento do usuário
        if (desktopNotifications && 'Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission().then(permission => {
                console.log('Permissão de notificação:', permission);
            });
        }

        testConnectivity(serverIp);
    });
});

// Carrega dados de monitoramento (usa cache se disponível)
async function loadServerInfo() {
    const serverId = document.getElementById('server_id').value;

    try {
        console.log('Carregando dados de monitoramento via cache...');
        const [summaryResponse] = await Promise.all([
            fetch(`/api/monitoring/servers/${serverId}`),
        ]);

        const summary = await summaryResponse.json();

        updateServerConnectionStatus(summary.online);

        // Atualiza timestamp
        document.getElementById('last-update-time').textContent = new Date().toLocaleTimeString();

    } catch (error) {
        console.error('Erro ao carregar dados de monitoramento:', error);
        showError('Erro ao carregar dados de monitoramento');
    }
}


function updateServerConnectionStatus(isConnected) {
    const connectionStatusDiv = document.getElementById('connection-status');
    if (isConnected) {
        connectionStatusDiv.innerHTML = `
           <div class="text-center">
                                    <div class="text-success mb-2">
                                        <i class="bi bi-wifi" style="font-size: 2rem;"></i>
                                    </div>
                                    <h6 class="mb-0">Conectividade</h6>
                                    <span class="badge bg-success">Ativa</span>
                                </div>
        `;
    } else {
        connectionStatusDiv.innerHTML = `
            <span class="text-danger">
                <i class="fas fa-exclamation-triangle"></i> Desconectado do servidor de monitoramento            
            </span>
        `;
    }
    /*  <div class="col-6">
                               <div class="text-center">
                                   <div class="text-success mb-2">
                                       <i class="bi bi-wifi" style="font-size: 2rem;"></i>
                                   </div>
                                   <h6 class="mb-0">Conectividade</h6>
                                   <span class="badge bg-success">Ativa</span>
                               </div>
                           </div>
                           <div class="col-6">
                               <div class="text-center">
                                   <div class="text-primary mb-2">
                                       <i class="bi bi-speedometer2" style="font-size: 2rem;"></i>
                                   </div>
                                   <h6 class="mb-0">Latência</h6>
                                   <span class="text-muted">12ms</span>
                               </div>
                           </div>
*/
}

async function testConnectivity(serverIp) {
    document.getElementById('test_connection').disabled = true;
    document.getElementById('test_connection').innerHTML = `
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        Testando...
    `;
    const [test] = await Promise.all([
        fetch(`/api/monitoring/test/${serverIp}`),
    ]);
    const result = await test.json();
    document.getElementById('test_connection').disabled = false;
    document.getElementById('test_connection').innerHTML = `
        <i class="bi bi-arrow-clockwise me-1"></i>Testar Conectividade
    `;
    console.log('Resultado do teste de conectividade:', result);
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
            
            <!-- Multiple Disks -->
            ${createDisksSection(server)}
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

// Cria seção de múltiplos discos
function createDisksSection(server) {
    // Se não tiver diskList, usa o disco principal (backward compatibility)
    if (!server.diskList || server.diskList.length === 0) {
        if (server.diskTotal && server.diskUsed) {
            return `
                <div class="metric-row mb-2">
                    <div class="d-flex justify-content-between align-items-center">
                        <small class="text-muted">
                            <i class="fas fa-hdd me-1"></i>Espaço em Disco
                        </small>
                        <span class="metric-badge badge ${getDiskBadgeColor(server.diskUsagePercent || 0)}">
                            ${server.diskUsagePercent ? server.diskUsagePercent.toFixed(1) : 'N/A'}%
                        </span>
                    </div>
                    <div class="metric-details">
                        <small class="text-muted">
                            ${formatBytes(server.diskUsed * 1024 * 1024 * 1024)} / ${formatBytes(server.diskTotal * 1024 * 1024 * 1024)}
                            ${server.diskAvailable ? ` (${formatBytes(server.diskAvailable * 1024 * 1024 * 1024)} livre)` : ''}
                        </small>
                    </div>
                    <div class="progress mt-1" style="height: 6px;">
                        <div class="progress-bar ${getDiskProgressColor(server.diskUsagePercent || 0)}" 
                             style="width: ${server.diskUsagePercent || 0}%"></div>
                    </div>
                </div>
            `;
        } else {
            return `
                <div class="metric-row mb-2">
                    <small class="text-muted">
                        <i class="fas fa-hdd me-1"></i>Espaço em Disco
                    </small><br>
                    <span class="badge bg-secondary">Não disponível</span>
                </div>
            `;
        }
    }

    // Múltiplos discos
    let disksHtml = `
        <div class="metric-row mb-2">
            <div class="d-flex justify-content-between align-items-center">
                <small class="text-muted">
                    <i class="fas fa-hdd me-1"></i>Discos (${server.diskList.length})
                </small>
                <button class="btn btn-sm btn-outline-secondary" type="button" data-bs-toggle="collapse" 
                        data-bs-target="#disks-${server.serverId}" aria-expanded="false">
                    <i class="fas fa-chevron-down"></i>
                </button>
            </div>
            <div class="collapse mt-2" id="disks-${server.serverId}">
    `;

    server.diskList.forEach((disk, index) => {
        disksHtml += `
            <div class="disk-item mb-2 p-2 border rounded">
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <strong class="text-primary">${disk.path}</strong>
                    <span class="badge ${getDiskBadgeColor(disk.usagePercent || 0)}">
                        ${disk.usagePercent ? disk.usagePercent.toFixed(1) : '0'}%
                    </span>
                </div>
                <div class="metric-details">
                    <small class="text-muted">
                        ${disk.description || 'Sem descrição'}
                    </small><br>
                    <small class="text-muted">
                        ${formatBytes(disk.usedGB * 1024 * 1024 * 1024)} / ${formatBytes(disk.totalGB * 1024 * 1024 * 1024)}
                        (${formatBytes(disk.availableGB * 1024 * 1024 * 1024)} livre)
                    </small>
                </div>
                <div class="progress mt-1" style="height: 6px;">
                    <div class="progress-bar ${getDiskProgressColor(disk.usagePercent || 0)}" 
                         style="width: ${disk.usagePercent || 0}%"></div>
                </div>
            </div>
        `;
    });

    disksHtml += `
            </div>
        </div>
    `;

    return disksHtml;
}

// Função auxiliar para cores da barra de progresso do disco
function getDiskProgressColor(usage) {
    if (usage >= 90) return 'bg-danger';
    if (usage >= 80) return 'bg-warning';
    if (usage >= 70) return 'bg-info';
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