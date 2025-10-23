document.addEventListener('DOMContentLoaded', function () {
    loadMonitoringData();
});




async function loadMonitoringData() {
    try {
        console.log('Carregando dados de monitoramento via cache...');
        const summaryResponse = await fetch('/api/monitoring/summary');

        const summary = await summaryResponse.json();
        console.log('Resumo:', summary);
        updateSummaryCards(summary);
    } catch (error) {
        console.error('Erro ao carregar dados de monitoramento:', error);
        showError('Erro ao carregar dados de monitoramento');
    }
}

function updateSummaryCards(summary) {
    document.getElementById('total-servers').textContent = summary.totalServers;
    document.getElementById('online-servers').textContent = summary.onlineServers;
    document.getElementById('offline-servers').textContent = summary.offlineServers;
    document.getElementById('warning-servers').textContent = summary.warningServers;

    const registeredServersInfo = document.getElementById('registered_servers_info');
    const registeredServersInfoLink = document.getElementById('registered_servers_info_link');
    const serversOverviewSection = document.getElementById('servers-overview-section');
    if (summary.totalServers === 0) {
        serversOverviewSection.style.display = 'none';
        registeredServersInfo.textContent = 'Nenhum servidor configurado ainda';
        registeredServersInfoLink.textContent = 'Clique aqui para adicionar seu primeiro servidor';
    } else {
        registeredServersInfo.textContent = 'Servidores registrados: ' + summary.totalServers;
        serversOverviewSection.style.display = 'block';
    }
}