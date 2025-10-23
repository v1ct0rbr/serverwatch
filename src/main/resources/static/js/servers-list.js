document.addEventListener('DOMContentLoaded', function () {
    // Alternar entre visualizações
    document.getElementById('tableView').addEventListener('change', function () {
        document.getElementById('table-view').classList.remove('d-none');
        document.getElementById('card-view').classList.add('d-none');
    });

    document.querySelectorAll('.current_status').forEach(function (element) {
        const ipAddress = element.getAttribute('data-ip');
        fetch(`/api/monitoring/ping?ipAddress=${encodeURIComponent(ipAddress)}`)
            .then(response => response.json())
            .then(data => {
                element.innerHTML = '<span class="badge bg-success"><i class="fas fa-circle me-1"></i>Online</span>';
            })
            .catch(error => {
                element.innerHTML = '<span class="badge bg-danger"><i class="fas fa-circle me-1"></i>Offline</span>';
            });
    });

    document.getElementById('cardView').addEventListener('change', function () {
        document.getElementById('table-view').classList.add('d-none');
        document.getElementById('card-view').classList.remove('d-none');
    });

    // Função para confirmar exclusão

});

function confirmDelete(button) {
    const serverId = button.getAttribute('data-id');
    const serverName = button.getAttribute('data-name');

    document.getElementById('serverName').textContent = serverName;
    document.getElementById('deleteForm').action = '/servers/' + serverId + '/delete';


    const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
    modal.show();
}

