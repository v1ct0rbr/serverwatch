/**
 * ServerWatch - Main JavaScript File
 * Funções principais da aplicação
 */

// Inicialização quando o DOM estiver carregado
document.addEventListener('DOMContentLoaded', function() {
    console.log('ServerWatch iniciado!');
    
    // Inicializar componentes
    initializeComponents();
    
    // Auto-refresh dos dados (opcional)
    // setupAutoRefresh();
});

/**
 * Inicializa componentes da interface
 */
function initializeComponents() {
    // Inicializar tooltips do Bootstrap
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Inicializar popovers do Bootstrap
    var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Adicionar animações aos cards
    addCardAnimations();
    
    // Configurar navegação ativa
    highlightActiveNavigation();
}

/**
 * Adiciona animações aos cards
 */
function addCardAnimations() {
    const cards = document.querySelectorAll('.card');
    cards.forEach((card, index) => {
        // Adicionar delay para animação escalonada
        card.style.animationDelay = `${index * 0.1}s`;
        card.classList.add('fade-in');
    });
}

/**
 * Destaca o item de navegação ativo
 */
function highlightActiveNavigation() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.navbar-nav .nav-link');
    
    navLinks.forEach(link => {
        const href = link.getAttribute('href');
        if (href && currentPath.includes(href) && href !== '/') {
            link.classList.add('active');
        } else if (href === '/' && currentPath === '/') {
            link.classList.add('active');
        }
    });
}

/**
 * Configura atualização automática dos dados
 * @param {number} interval - Intervalo em millisegundos (padrão: 30000 = 30s)
 */
function setupAutoRefresh(interval = 30000) {
    setInterval(() => {
        refreshDashboardData();
    }, interval);
}

/**
 * Atualiza os dados do dashboard
 */
function refreshDashboardData() {
    console.log('Atualizando dados do dashboard...');
    
    // Aqui você pode fazer chamadas AJAX para atualizar os dados
    // Exemplo usando fetch API:
    /*
    fetch('/api/dashboard/status')
        .then(response => response.json())
        .then(data => {
            updateDashboardCards(data);
        })
        .catch(error => {
            console.error('Erro ao atualizar dados:', error);
            showNotification('Erro ao atualizar dados', 'error');
        });
    */
}

/**
 * Atualiza os cards do dashboard com novos dados
 * @param {Object} data - Dados recebidos da API
 */
function updateDashboardCards(data) {
    // Exemplo de atualização dos cards
    const totalServers = document.querySelector('[data-metric="total-servers"]');
    const onlineServers = document.querySelector('[data-metric="online-servers"]');
    const offlineServers = document.querySelector('[data-metric="offline-servers"]');
    const alerts = document.querySelector('[data-metric="alerts"]');
    
    if (totalServers && data.totalServers !== undefined) {
        totalServers.textContent = data.totalServers;
    }
    
    if (onlineServers && data.onlineServers !== undefined) {
        onlineServers.textContent = data.onlineServers;
    }
    
    if (offlineServers && data.offlineServers !== undefined) {
        offlineServers.textContent = data.offlineServers;
    }
    
    if (alerts && data.alerts !== undefined) {
        alerts.textContent = data.alerts;
    }
}

/**
 * Exibe notificação para o usuário
 * @param {string} message - Mensagem a ser exibida
 * @param {string} type - Tipo da notificação (success, error, warning, info)
 */
function showNotification(message, type = 'info') {
    // Criar elemento da notificação
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
    notification.style.top = '20px';
    notification.style.right = '20px';
    notification.style.zIndex = '9999';
    notification.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    // Adicionar ao body
    document.body.appendChild(notification);
    
    // Remover automaticamente após 5 segundos
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}

/**
 * Formata números para exibição
 * @param {number} number - Número a ser formatado
 * @returns {string} - Número formatado
 */
function formatNumber(number) {
    return new Intl.NumberFormat('pt-BR').format(number);
}

/**
 * Formata data/hora para exibição
 * @param {Date|string} date - Data a ser formatada
 * @returns {string} - Data formatada
 */
function formatDateTime(date) {
    const dateObj = date instanceof Date ? date : new Date(date);
    return dateObj.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
}

/**
 * Valida formulários
 * @param {HTMLFormElement} form - Formulário a ser validado
 * @returns {boolean} - True se válido, false caso contrário
 */
function validateForm(form) {
    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.classList.add('is-invalid');
            isValid = false;
        } else {
            field.classList.remove('is-invalid');
            field.classList.add('is-valid');
        }
    });
    
    return isValid;
}

/**
 * Limpa validação do formulário
 * @param {HTMLFormElement} form - Formulário para limpar validação
 */
function clearFormValidation(form) {
    const fields = form.querySelectorAll('.is-valid, .is-invalid');
    fields.forEach(field => {
        field.classList.remove('is-valid', 'is-invalid');
    });
}

/**
 * Utilitários para AJAX
 */
const ServerWatch = {
    /**
     * Faz requisição GET
     * @param {string} url - URL para requisição
     * @returns {Promise} - Promise com resposta
     */
    get: function(url) {
        return fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        }).then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        });
    },
    
    /**
     * Faz requisição POST
     * @param {string} url - URL para requisição
     * @param {Object} data - Dados para enviar
     * @returns {Promise} - Promise com resposta
     */
    post: function(url, data) {
        return fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        }).then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        });
    }
};

// Exportar para uso global
window.ServerWatch = ServerWatch;