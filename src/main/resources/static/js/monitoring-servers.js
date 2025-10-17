/**
 * JavaScript específico para Lista de Servidores (Cards)
 * Arquivo: /static/js/monitoring-servers.js
 */

let currentPage = 1;
let totalPages = 1;
let serversData = [];

// Inicialização
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM carregado - iniciando sistema de monitoramento');
    
    // Carrega lista básica de servidores primeiro (sem métricas)
    loadServersList();
    
    // Inicia coleta de métricas em background após 500ms
    console.log('Agendando coleta de métricas para 500ms');
    setTimeout(() => {
        console.log('Iniciando coleta de métricas...');
        collectServerMetrics();
    }, 500);
    
    // Event listeners para filtros
    document.getElementById('search').addEventListener('input', debounce(loadServersList, 300));
    document.getElementById('status-filter').addEventListener('change', loadServersList);
    document.getElementById('type-filter').addEventListener('change', loadServersList);
    document.getElementById('location-filter').addEventListener('input', debounce(loadServersList, 300));
});

// Carrega lista de servidores (dados básicos - rápido)
async function loadServersList(page = 1) {
    try {
        console.log('Carregando lista de servidores, página:', page);
        currentPage = page;
        
        // Coleta filtros
        const search = document.getElementById('search')?.value || '';
        const status = document.getElementById('status-filter')?.value || '';
        const type = document.getElementById('type-filter')?.value || '';
        const location = document.getElementById('location-filter')?.value || '';
        
        // Monta query string
        const params = new URLSearchParams();
        if (search) params.append('search', search);
        if (status) params.append('status', status);
        if (type) params.append('type', type);
        if (location) params.append('location', location);
        params.append('page', page - 1); // Backend usa base 0
        params.append('size', 20);
        
        console.log('Fazendo chamada para:', `/api/monitoring/servers?${params}`);
        const response = await fetch(`/api/monitoring/servers?${params}`, {
            credentials: 'same-origin',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        console.log('Resposta da lista de servidores:', response.status, response.statusText);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const data = await response.json();
        console.log('Dados recebidos da lista:', data);
        
        serversData = data.content || data;
        console.log('Servidores processados:', serversData.length, serversData);
        displayServersCards(serversData);
        
        // Atualiza informações de paginação se disponível
        if (data.totalElements !== undefined) {
            updatePaginationInfo(data);
        }
        
    } catch (error) {
        console.error('Erro ao carregar lista de servidores:', error);
        showErrorInCards('Erro ao carregar dados dos servidores');
    }
}

// Coleta métricas de todos os servidores via API (em background)
async function collectServerMetrics() {
    try {
        console.log('Iniciando coleta de métricas em background...');
        showMetricsLoadingState();
        
        console.log('Fazendo chamada para API:', '/api/monitoring/servers/collect-metrics');
        const response = await fetch('/api/monitoring/servers/collect-metrics', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            }
        });
        
        console.log('Resposta da API:', response.status, response.statusText);
        
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }
        
        const serversWithMetrics = await response.json();
        console.log(`Métricas coletadas para ${serversWithMetrics.length} servidores:`, serversWithMetrics);
        
        // Atualiza os cards com as métricas
        updateCardsWithMetrics(serversWithMetrics);
        console.log('Cards atualizados com métricas');
        
    } catch (error) {
        console.error('Erro ao coletar métricas dos servidores:', error);
        showMetricsError(error.message);
    }
}

// Exibe cards de servidores
function displayServersCards(servers) {
    const container = document.getElementById('servers-cards-container');
    
    if (!servers || servers.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center py-5">
                <i class="fas fa-server fa-3x text-muted mb-3"></i>
                <h5 class="text-muted">Nenhum servidor encontrado</h5>
                <p class="text-muted">Verifique os filtros ou adicione novos servidores.</p>
                <a href="/servers/new" class="btn btn-primary">
                    <i class="fas fa-plus me-1"></i> Adicionar Servidor
                </a>
            </div>
        `;
        return;
    }
    
    container.innerHTML = servers.map(server => createServerCard(server)).join('');
}

// Cria card individual do servidor
function createServerCard(server) {
    const statusClass = server.status.toLowerCase();
    const statusIcon = getStatusIcon(server.status);
    const statusText = getStatusText(server.status);
    
    // Para servidores com status PENDING, mostra estado de carregamento
    const isPending = server.status === 'PENDING';
    
    return `
        <div class="col-lg-4 col-md-6 col-sm-12 mb-4">
            <div class="card server-card ${statusClass}" data-server-id="${server.serverId}">
                <!-- Header do Card -->
                <div class="card-header d-flex justify-content-between align-items-center">
                    <div class="d-flex align-items-center">
                        <span class="status-indicator status-${statusClass}"></span>
                        <div>
                            <strong class="d-block">${server.serverName}</strong>
                            <code class="small text-muted">${server.ipAddress}</code>
                        </div>
                    </div>
                    <div class="server-actions">
                        <div class="dropdown">
                            <button class="btn btn-sm btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown">
                                <i class="fas fa-ellipsis-v"></i>
                            </button>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="#" onclick="showQuickDetails(${server.serverId})">
                                    <i class="fas fa-eye me-2"></i> Ver Detalhes
                                </a></li>
                                <li><a class="dropdown-item" href="#" onclick="refreshServer(${server.serverId})">
                                    <i class="fas fa-sync-alt me-2"></i> Atualizar
                                </a></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><a class="dropdown-item" href="/servers/${server.serverId}/edit">
                                    <i class="fas fa-edit me-2"></i> Editar
                                </a></li>
                                <li><a class="dropdown-item text-danger" href="/servers/${server.serverId}/delete">
                                    <i class="fas fa-trash me-2"></i> Excluir
                                </a></li>
                            </ul>
                        </div>
                    </div>
                </div>
                
                <!-- Corpo do Card -->
                <div class="card-body">
                    <!-- Status e Informações Básicas -->
                    <div class="row mb-3">
                        <div class="col-6">
                            <small class="text-muted d-block">Status</small>
                            <span class="badge bg-${getStatusBadgeColor(server.status)}">
                                <i class="${statusIcon} me-1"></i>
                                ${statusText}
                            </span>
                        </div>
                        <div class="col-6">
                            <small class="text-muted d-block">Tipo</small>
                            <span class="text-dark">${server.type || 'Não definido'}</span>
                        </div>
                    </div>
                    
                    ${server.location ? `
                    <div class="row mb-3">
                        <div class="col-12">
                            <small class="text-muted d-block">Localização</small>
                            <span class="text-dark">
                                <i class="fas fa-map-marker-alt me-1 text-muted"></i>
                                ${server.location}
                            </span>
                        </div>
                    </div>
                    ` : ''}
                    
                    ${server.description ? `
                    <div class="row mb-3">
                        <div class="col-12">
                            <small class="text-muted d-block">Descrição</small>
                            <span class="text-dark small">${server.description}</span>
                        </div>
                    </div>
                    ` : ''}
                    
                    <div class="metrics-container">
                        ${isPending ? createMetricsLoadingState() : (server.online ? createServerMetrics(server) : createOfflineInfo(server))}
                    </div>
                </div>
                
                <!-- Footer do Card -->
                <div class="card-footer">
                    <div class="d-flex justify-content-between align-items-center">
                        <small class="last-check-text">
                            <i class="far fa-clock me-1"></i>
                            ${server.lastCheck ? formatRelativeTime(server.lastCheck) : 'Nunca verificado'}
                        </small>
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-primary btn-sm" onclick="showQuickDetails(${server.serverId})" title="Ver Detalhes">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-outline-success btn-sm refresh-server-btn" onclick="refreshServer(${server.serverId})" title="Atualizar">
                                <i class="fas fa-sync-alt"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Cria seção de métricas para servidores online
function createServerMetrics(server) {
    return `
        <div class="row g-2">
            ${server.uptime ? `
            <div class="col-12 mb-2">
                <small class="text-muted d-block">Uptime</small>
                <span class="badge uptime-badge">${server.uptime}</span>
            </div>
            ` : ''}
            
            <div class="col-6">
                <small class="text-muted d-block">CPU Load</small>
                ${server.cpuLoad1Min !== null ? 
                    `<span class="badge metric-badge ${getCpuBadgeColor(server.cpuLoad1Min)}">${server.cpuLoad1Min.toFixed(1)}%</span>` : 
                    '<span class="text-muted">-</span>'
                }
            </div>
            
            <div class="col-6">
                <small class="text-muted d-block">Memória</small>
                ${server.memoryUsagePercent !== null ? 
                    `<span class="badge metric-badge ${getMemoryBadgeColor(server.memoryUsagePercent)}">${server.memoryUsagePercent.toFixed(1)}%</span>` : 
                    '<span class="text-muted">-</span>'
                }
            </div>
            
            ${server.diskUsagePercent !== null ? `
            <div class="col-6">
                <small class="text-muted d-block">Disco</small>
                <span class="badge metric-badge ${getDiskBadgeColor(server.diskUsagePercent)}">${server.diskUsagePercent.toFixed(1)}%</span>
            </div>
            ` : ''}
            

        </div>
    `;
}

// Cria seção para servidores offline
function createOfflineInfo(server) {
    return `
        <div class="text-center py-3">
            <i class="fas fa-exclamation-triangle fa-2x text-warning mb-2"></i>
            <div class="text-muted">Servidor indisponível</div>
            ${server.errorMessage ? `
                <div class="alert alert-warning mt-2 small">
                    <strong>Erro:</strong> ${server.errorMessage}
                </div>
            ` : ''}
        </div>
    `;
}

// Mostra detalhes rápidos
async function showQuickDetails(serverId) {
    try {
        const response = await fetch(`/api/monitoring/servers/${serverId}`);
        const server = await response.json();
        
        const content = `
            <div class="row">
                <div class="col-md-6">
                    <h6>Informações Básicas</h6>
                    <table class="table table-sm">
                        <tr><td><strong>Nome:</strong></td><td>${server.serverName}</td></tr>
                        <tr><td><strong>IP:</strong></td><td><code>${server.ipAddress}</code></td></tr>
                        <tr><td><strong>Status:</strong></td><td>
                            <span class="badge bg-${getStatusBadgeColor(server.status)}">
                                ${getStatusText(server.status)}
                            </span>
                        </td></tr>
                        <tr><td><strong>Tipo:</strong></td><td>${server.type || '-'}</td></tr>
                        <tr><td><strong>Localização:</strong></td><td>${server.location || '-'}</td></tr>
                    </table>
                </div>
                <div class="col-md-6">
                    <h6>Métricas</h6>
                    <table class="table table-sm">
                        <tr><td><strong>Uptime:</strong></td><td>${server.uptime || '-'}</td></tr>
                        <tr><td><strong>CPU Load:</strong></td><td>${server.cpuLoad1Min !== null ? server.cpuLoad1Min.toFixed(1) + '%' : '-'}</td></tr>
                        <tr><td><strong>Memória:</strong></td><td>${server.memoryUsagePercent !== null ? server.memoryUsagePercent.toFixed(1) + '%' : '-'}</td></tr>
                        <tr><td><strong>Disco:</strong></td><td>${server.diskUsagePercent !== null ? server.diskUsagePercent.toFixed(1) + '%' : '-'}</td></tr>
                        <tr><td><strong>Interfaces:</strong></td><td>${server.interfaceCount || '-'}</td></tr>
                    </table>
                </div>
            </div>
            
            ${server.errorMessage ? `
                <div class="alert alert-warning mt-3">
                    <strong>Último Erro:</strong> ${server.errorMessage}
                </div>
            ` : ''}
        `;
        
        document.getElementById('quick-details-content').innerHTML = content;
        document.getElementById('view-full-details').href = `/servers/${serverId}`;
        
        new bootstrap.Modal(document.getElementById('quickDetailsModal')).show();
        
    } catch (error) {
        console.error('Erro ao carregar detalhes do servidor:', error);
        alert('Erro ao carregar detalhes do servidor');
    }
}

// Atualiza servidor específico
async function refreshServer(serverId) {
    try {
        const card = document.querySelector(`[data-server-id="${serverId}"]`);
        const refreshBtns = card.querySelectorAll('.fa-sync-alt');
        
        refreshBtns.forEach(btn => btn.classList.add('fa-spin'));
        
        const response = await fetch(`/api/monitoring/servers/${serverId}/refresh`, {
            method: 'POST'
        });
        
        if (response.ok) {
            // Recarrega a lista após um pequeno delay
            setTimeout(() => {
                loadServersList(currentPage);
            }, 1500);
        } else {
            throw new Error('Erro na resposta do servidor');
        }
        
    } catch (error) {
        console.error('Erro ao atualizar servidor:', error);
        alert('Erro ao atualizar servidor');
        
        // Remove animação em caso de erro
        const card = document.querySelector(`[data-server-id="${serverId}"]`);
        if (card) {
            const refreshBtns = card.querySelectorAll('.fa-sync-alt');
            refreshBtns.forEach(btn => btn.classList.remove('fa-spin'));
        }
    }
}

// Limpa filtros
function clearFilters() {
    document.getElementById('search').value = '';
    document.getElementById('status-filter').value = '';
    document.getElementById('type-filter').value = '';
    document.getElementById('location-filter').value = '';
    loadServersList(1);
}

// Exporta para CSV
function exportToCSV() {
    const headers = ['Nome', 'IP', 'Status', 'Tipo', 'Localização', 'CPU %', 'Memória %', 'Última Verificação'];
    const csvContent = [
        headers.join(','),
        ...serversData.map(server => [
            server.serverName,
            server.ipAddress,
            server.status,
            server.type || '',
            server.location || '',
            server.cpuLoad1Min !== null ? server.cpuLoad1Min.toFixed(1) : '',
            server.memoryUsagePercent !== null ? server.memoryUsagePercent.toFixed(1) : '',
            server.lastCheck || ''
        ].join(','))
    ].join('\n');
    
    downloadFile('servidores.csv', csvContent, 'text/csv');
}

// Exporta para JSON
function exportToJSON() {
    const jsonContent = JSON.stringify(serversData, null, 2);
    downloadFile('servidores.json', jsonContent, 'application/json');
}

// Atualiza informações de paginação
function updatePaginationInfo(data) {
    totalPages = data.totalPages;
    const showingFrom = data.number * data.size + 1;
    const showingTo = Math.min((data.number + 1) * data.size, data.totalElements);
    
    document.getElementById('showing-from').textContent = showingFrom;
    document.getElementById('showing-to').textContent = showingTo;
    document.getElementById('total-records').textContent = data.totalElements;
    
    updatePagination(data.number + 1, data.totalPages);
}

// Atualiza paginação
function updatePagination(current, total) {
    const pagination = document.getElementById('pagination');
    let html = '';
    
    // Botão anterior
    html += `
        <li class="page-item ${current === 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="loadServersList(${current - 1})">Anterior</a>
        </li>
    `;
    
    // Páginas
    const start = Math.max(1, current - 2);
    const end = Math.min(total, current + 2);
    
    for (let i = start; i <= end; i++) {
        html += `
            <li class="page-item ${i === current ? 'active' : ''}">
                <a class="page-link" href="#" onclick="loadServersList(${i})">${i}</a>
            </li>
        `;
    }
    
    // Botão próximo
    html += `
        <li class="page-item ${current === total ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="loadServersList(${current + 1})">Próximo</a>
        </li>
    `;
    
    pagination.innerHTML = html;
}

// Mostra erro nos cards
function showErrorInCards(message) {
    const container = document.getElementById('servers-cards-container');
    container.innerHTML = `
        <div class="col-12 text-center py-5">
            <i class="fas fa-exclamation-triangle fa-3x text-danger mb-3"></i>
            <h5 class="text-danger">${message}</h5>
            <p class="text-muted">Tente atualizar a página ou verifique sua conexão.</p>
            <button class="btn btn-outline-primary" onclick="loadServersList()">
                <i class="fas fa-sync-alt me-1"></i> Tentar Novamente
            </button>
        </div>
    `;
}

// Funções utilitárias
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function formatRelativeTime(dateTimeStr) {
    const date = new Date(dateTimeStr);
    const now = new Date();
    const diff = Math.abs(now - date);
    
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    
    if (minutes < 1) return 'Agora mesmo';
    if (minutes < 60) return `${minutes}min atrás`;
    if (hours < 24) return `${hours}h atrás`;
    return `${days}d atrás`;
}

function downloadFile(filename, content, mimeType) {
    const blob = new Blob([content], { type: mimeType });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// Funções de status (compartilhadas)
function getStatusIcon(status) {
    const icons = {
        'ONLINE': 'fas fa-check-circle',
        'OFFLINE': 'fas fa-times-circle',
        'WARNING': 'fas fa-exclamation-triangle',
        'UNKNOWN': 'fas fa-question-circle',
        'PENDING': 'fas fa-hourglass-half'
    };
    return icons[status] || icons['UNKNOWN'];
}

// === FUNÇÕES PARA CARREGAMENTO DE MÉTRICAS ===

// Cria estado de carregamento para métricas
function createMetricsLoadingState() {
    return `
        <div class="text-center py-3 metrics-loading">
            <div class="spinner-border spinner-border-sm text-primary mb-2" role="status">
                <span class="visually-hidden">Carregando...</span>
            </div>
            <div class="text-muted small">Coletando métricas...</div>
        </div>
    `;
}

// Mostra estado de carregamento para todas as métricas
function showMetricsLoadingState() {
    const cards = document.querySelectorAll('.server-card[data-server-id]');
    cards.forEach(card => {
        const metricsContainer = card.querySelector('.metrics-container');
        if (metricsContainer && !metricsContainer.querySelector('.metrics-loading')) {
            // Só mostra loading se não tiver métricas ainda
            const hasMetrics = metricsContainer.querySelector('.badge.metric-badge');
            if (!hasMetrics) {
                metricsContainer.innerHTML = createMetricsLoadingState();
            }
        }
    });
}

// Atualiza cards com métricas coletadas
function updateCardsWithMetrics(serversWithMetrics) {
    serversWithMetrics.forEach(server => {
        const card = document.querySelector(`.server-card[data-server-id="${server.serverId}"]`);
        if (card) {
            // Atualiza status se necessário
            updateCardStatus(card, server);
            
            // Atualiza métricas
            const metricsContainer = card.querySelector('.metrics-container');
            if (metricsContainer) {
                metricsContainer.innerHTML = server.online ? 
                    createServerMetrics(server) : 
                    createOfflineInfo(server);
            }
            
            // Atualiza timestamp
            const lastCheckText = card.querySelector('.last-check-text');
            if (lastCheckText && server.lastCheck) {
                lastCheckText.innerHTML = `
                    <i class="far fa-clock me-1"></i>
                    ${formatRelativeTime(server.lastCheck)}
                `;
            }
        }
    });
}

// Atualiza status do card
function updateCardStatus(card, server) {
    const statusBadge = card.querySelector('.badge');
    const statusIndicator = card.querySelector('.status-indicator');
    
    if (statusBadge) {
        const statusIcon = getStatusIcon(server.status);
        const statusText = getStatusText(server.status);
        const statusColor = getStatusBadgeColor(server.status);
        
        statusBadge.className = `badge bg-${statusColor}`;
        statusBadge.innerHTML = `<i class="${statusIcon} me-1"></i>${statusText}`;
    }
    
    if (statusIndicator) {
        statusIndicator.className = `status-indicator status-${server.status.toLowerCase()}`;
    }
    
    // Atualiza classe do card
    card.className = card.className.replace(/\b(online|offline|warning|unknown|pending)\b/g, server.status.toLowerCase());
    card.classList.add('server-card', server.status.toLowerCase());
}

// Mostra erro na coleta de métricas
function showMetricsError(errorMessage) {
    const cards = document.querySelectorAll('.server-card .metrics-loading');
    cards.forEach(loadingDiv => {
        loadingDiv.parentElement.innerHTML = `
            <div class="text-center py-3 text-warning">
                <i class="fas fa-exclamation-triangle mb-2"></i>
                <div class="small">Erro ao carregar métricas</div>
                <div class="text-muted" style="font-size: 0.8em;">${errorMessage}</div>
            </div>
        `;
    });
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