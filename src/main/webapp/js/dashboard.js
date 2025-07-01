let currentLoteId = null;
let currentPage = 0;
let currentAsesorFilter = '';

document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('jwtToken')) {
        window.location.href = 'index.html';
        return;
    }
    setupUI();
    setupEventListeners();
});

function setupUI() {
    document.getElementById('welcomeMessage').textContent = `Bienvenido, ${localStorage.getItem('username')}`;
    if (localStorage.getItem('sidebarState') === 'collapsed') {
        document.querySelector('.dashboard-container').classList.add('sidebar-collapsed');
    }
    currentLoteId = localStorage.getItem('currentLoteId');
    const userRole = localStorage.getItem('userRole');
    let initialView = 'mensajes';
    if (userRole === 'admin') {
        initialView = 'usuarios';
        cargarUsuarios();
    }
    showTargetView(initialView);
    cargarMensajes(0);
    cargarEstadisticas();
    cargarFiltroAsesores();
}

function setupEventListeners() {
    document.querySelector('.sidebar-nav').addEventListener('click', e => { if (e.target.matches('.nav-link')) { e.preventDefault(); switchView(e.target.dataset.target); } });
    document.getElementById('logoutButton').addEventListener('click', () => { localStorage.clear(); window.location.href = 'index.html'; });
    document.getElementById('sidebar-toggle').addEventListener('click', handleSidebarToggle);
    document.getElementById('export-excel-btn').addEventListener('click', exportarMensajes);
    document.getElementById('show-create-user-modal').addEventListener('click', () => showUserModal());
    document.getElementById('cancel-user-modal').addEventListener('click', hideUserModal);
    document.getElementById('user-form').addEventListener('submit', handleUserFormSubmit);
    document.getElementById('userList').addEventListener('click', handleUserTableActions);
    document.getElementById('uploadForm').addEventListener('submit', handleFileUpload);
    document.getElementById('pagination-container').addEventListener('click', handlePaginationClick);
    document.getElementById('asesor-filter').addEventListener('change', handleAsesorFilterChange);
}

function handleSidebarToggle() {
    const container = document.querySelector('.dashboard-container');
    container.classList.toggle('sidebar-collapsed');
    localStorage.setItem('sidebarState', container.classList.contains('sidebar-collapsed') ? 'collapsed' : 'expanded');
}

function showTargetView(targetId) {
    document.querySelectorAll('.content-section.active').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-link.active').forEach(l => l.classList.remove('active'));
    document.getElementById(targetId).classList.add('active');
    document.querySelector(`.nav-link[data-target="${targetId}"]`).classList.add('active');
}

function switchView(targetId) {
    showTargetView(targetId);
    if (targetId === 'mensajes') {
        localStorage.removeItem('currentLoteId');
        currentLoteId = null;
        currentAsesorFilter = '';
        document.getElementById('asesor-filter').value = '';
        cargarMensajes(0);
    }
    if (targetId === 'estadisticas') {
        cargarEstadisticas();
        cargarEstadisticasAsesor(); // Cargar la nueva tabla al ver las estadísticas
    }
}

async function fetchAPI(url, options = {}) {
    const defaultHeaders = { 'Authorization': `Bearer ${localStorage.getItem('jwtToken')}` };
    if (!(options.body instanceof FormData)) {
        defaultHeaders['Content-Type'] = 'application/json';
    }
    const config = { ...options, headers: { ...defaultHeaders, ...options.headers } };
    const response = await fetch(url, config);
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ error: 'Error del servidor sin detalles.' }));
        throw new Error(errorData.error || `Error HTTP ${response.status}`);
    }
    if (response.status === 204 || response.headers.get("content-length") === "0") return { success: true };
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return response.json();
    }
    return response.blob();
}

// --- SECCIÓN: GESTIÓN DE USUARIOS ---
function showUserModal(user = null) {
    const form = document.getElementById('user-form');
    form.reset();
    document.getElementById('form-error').textContent = '';
    const isEditing = !!user;
    document.getElementById('modal-title').textContent = isEditing ? 'Editar Usuario' : 'Crear Usuario';
    document.getElementById('user-id').value = isEditing ? user.id : '';
    document.getElementById('username').value = isEditing ? user.username : '';
    document.getElementById('nombreCompleto').value = isEditing ? user.nombreCompleto || '' : '';
    document.getElementById('rol').value = isEditing ? user.rol : 'calidad';
    document.getElementById('password').placeholder = isEditing ? 'Dejar en blanco para no cambiar' : 'Contraseña requerida';
    document.getElementById('username').disabled = isEditing;
    document.getElementById('user-modal').style.display = 'flex';
}
function hideUserModal() { document.getElementById('user-modal').style.display = 'none'; }
async function handleUserFormSubmit(event) {
    event.preventDefault();
    const id = document.getElementById('user-id').value;
    const isEditing = !!id;
    const password = document.getElementById('password').value;
    const userData = { nombreCompleto: document.getElementById('nombreCompleto').value, rol: document.getElementById('rol').value };
    if (!isEditing) { userData.username = document.getElementById('username').value; }
    if (password) { userData.passwordHash = password; }
    const url = isEditing ? `/api/usuarios/${id}` : '/api/usuarios';
    const method = isEditing ? 'PUT' : 'POST';
    try {
        await fetchAPI(url, { method, body: JSON.stringify(userData) });
        hideUserModal();
        cargarUsuarios();
    } catch (error) {
        document.getElementById('form-error').textContent = `Error: ${error.message}`;
    }
}
async function handleUserTableActions(event) {
    const button = event.target.closest('button.btn-action');
    if (!button) return;
    const userId = button.dataset.id;
    if (button.classList.contains('btn-edit')) {
        try { const user = await fetchAPI(`/api/usuarios/${userId}`); if (user) showUserModal(user); }
        catch (error) { alert(`Error al cargar datos del usuario: ${error.message}`); }
        return;
    }
    if (button.classList.contains('btn-deactivate')) {
        if (confirm('¿Seguro que quieres DESACTIVAR este usuario?')) {
            try { await fetchAPI(`/api/usuarios/${userId}/desactivar`, { method: 'DELETE' }); cargarUsuarios(); }
            catch (error) { alert(`Error: ${error.message}`); }
        }
        return;
    }
    if (button.classList.contains('btn-activate')) {
        if (confirm('¿Seguro que quieres ACTIVAR este usuario?')) {
            try { await fetchAPI(`/api/usuarios/${userId}`, { method: 'PUT', body: JSON.stringify({ activo: true }) }); cargarUsuarios(); }
            catch (error) { alert(`Error: ${error.message}`); }
        }
    }
}
async function cargarUsuarios() {
    const userList = document.getElementById('userList');
    userList.innerHTML = '<tr><td colspan="7">Cargando...</td></tr>';
    try {
        const usuarios = await fetchAPI('/api/usuarios');
        userList.innerHTML = '';
        if (!usuarios || usuarios.length === 0) {
            userList.innerHTML = '<tr><td colspan="7">No hay usuarios para mostrar.</td></tr>';
            return;
        }
        usuarios.forEach(u => {
            const fecha = new Date(u.fechaCreacion).toLocaleDateString('es-ES');
            userList.innerHTML += `<tr><td>${u.id}</td><td>${u.username}</td><td>${u.nombreCompleto || 'N/A'}</td><td>${u.rol}</td><td>${u.activo ? 'Sí' : 'No'}</td><td>${fecha}</td><td><button class="btn-action btn-edit" data-id="${u.id}">Editar</button>${u.activo ? `<button class="btn-action btn-deactivate" data-id="${u.id}">Desactivar</button>` : `<button class="btn-action btn-activate" data-id="${u.id}">Activar</button>`}</td></tr>`;
        });
    } catch (error) {
        userList.innerHTML = `<tr><td colspan="7" class="error-message">Error al cargar usuarios: ${error.message}</td></tr>`;
    }
}

// --- SECCIÓN: GESTIÓN DE MENSAJES ---

async function cargarFiltroAsesores() {
    const select = document.getElementById('asesor-filter');
    try {
        const asesores = await fetchAPI('/api/mensajes/asesores');
        select.innerHTML = '<option value="">Todos los Asesores</option>';
        asesores.forEach(asesor => {
            const option = document.createElement('option');
            option.value = asesor;
            option.textContent = asesor;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error al cargar la lista de asesores:", error);
    }
}

function handleAsesorFilterChange(event) {
    currentAsesorFilter = event.target.value;
    currentLoteId = null; 
    localStorage.removeItem('currentLoteId');
    cargarMensajes(0);
}

async function cargarMensajes(page = 0) {
    currentPage = page;
    const messageList = document.getElementById('messageList');
    messageList.innerHTML = `<tr><td colspan="7">Cargando página ${page + 1}...</td></tr>`;
    const params = new URLSearchParams({ page, size: 10 });
    let url;
    if (currentLoteId) {
        url = `/api/mensajes/lote/${currentLoteId}?${params.toString()}`;
    } else {
        if (currentAsesorFilter) {
            params.append('asesor', currentAsesorFilter);
        }
        url = `/api/mensajes?${params.toString()}`;
    }
    try {
        const paginatedData = await fetchAPI(url);
        const mensajes = paginatedData.content;
        messageList.innerHTML = '';
        if (!mensajes || mensajes.length === 0) {
            let emptyMessage = 'No hay mensajes. Sube un archivo.';
            if (currentLoteId) emptyMessage = `Mostrando solo mensajes del último lote.`;
            if (currentAsesorFilter) emptyMessage = `No se encontraron mensajes para el asesor: ${currentAsesorFilter}.`;
            messageList.innerHTML = `<tr><td colspan="7">${emptyMessage}</td></tr>`;
            renderizarPaginacion(0, 0);
            return;
        }
        mensajes.forEach(m => {
            let fechaMensaje = m.fechaHoraMensaje ? new Date(m.fechaHoraMensaje).toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'medium' }) : 'N/A';
            messageList.innerHTML += `<tr class="${m.clasificacion === 'Alerta' ? 'row-alert' : ''}"><td>${m.id}</td><td>${m.nombreAsesor || 'N/A'}</td><td>${m.aplicacion || 'N/A'}</td><td>${m.texto}</td><td>${m.clasificacion}</td><td><pre>${m.observacion || 'N/A'}</pre></td><td>${fechaMensaje}</td></tr>`;
        });
        renderizarPaginacion(paginatedData.totalPages, paginatedData.currentPage);
    } catch (error) {
        messageList.innerHTML = `<tr><td colspan="7" class="error-message">Error al cargar mensajes: ${error.message}</td></tr>`;
    }
}

function renderizarPaginacion(totalPages, currentPage) {
    const container = document.getElementById('pagination-container');
    container.innerHTML = '';
    if (totalPages <= 1) return;
    const prevButton = document.createElement('button');
    prevButton.innerHTML = '&laquo;';
    prevButton.title = 'Página Anterior';
    prevButton.dataset.page = currentPage - 1;
    prevButton.disabled = currentPage === 0;
    container.appendChild(prevButton);
    const info = document.createElement('span');
    info.className = 'info-text';
    info.textContent = `Página ${currentPage + 1} de ${totalPages}`;
    container.appendChild(info);
    const nextButton = document.createElement('button');
    nextButton.innerHTML = '&raquo;';
    nextButton.title = 'Página Siguiente';
    nextButton.dataset.page = currentPage + 1;
    nextButton.disabled = currentPage >= totalPages - 1;
    container.appendChild(nextButton);
}

function handlePaginationClick(event) {
    const button = event.target.closest('button');
    if (!button || button.disabled) return;
    const page = parseInt(button.dataset.page, 10);
    if (!isNaN(page)) {
        cargarMensajes(page);
    }
}

async function exportarMensajes() {
    const button = document.getElementById('export-excel-btn');
    button.textContent = 'Generando...';
    button.disabled = true;
    try {
        const blob = await fetchAPI('/api/mensajes/export');
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = `Reporte_Mensajes_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();
    } catch (error) {
        alert(`Error al exportar: ${error.message}`);
    } finally {
        button.textContent = 'Exportar a Excel';
        button.disabled = false;
    }
}

async function cargarEstadisticas() {
    const statsContainer = document.getElementById('messageStats');
    statsContainer.innerHTML = 'Cargando...';
    try {
        const stats = await fetchAPI('/api/mensajes/stats');
        statsContainer.innerHTML = `<p><strong>Total de Mensajes Global:</strong> ${stats.totalMensajes || 0}</p>`;
    } catch (error) {
        statsContainer.innerHTML = `<p class="error-message">Error al cargar estadísticas generales.</p>`;
    }
}

async function cargarEstadisticasAsesor() {
    const statsList = document.getElementById('asesor-stats-list');
    statsList.innerHTML = '<tr><td colspan="2">Cargando...</td></tr>';
    try {
        const stats = await fetchAPI('/api/mensajes/stats/por-asesor');
        statsList.innerHTML = '';
        if (!stats || stats.length === 0) {
            statsList.innerHTML = '<tr><td colspan="2">No hay datos por asesor para mostrar.</td></tr>';
            return;
        }
        stats.forEach(s => {
            statsList.innerHTML += `<tr>
                <td>${s.nombreAsesor || 'Sin Asignar'}</td>
                <td>${s.totalMensajes}</td>
            </tr>`;
        });
    } catch (error) {
        statsList.innerHTML = `<tr><td colspan="2" class="error-message">Error al cargar estadísticas por asesor.</td></tr>`;
    }
}

async function handleFileUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    const uploadMessage = document.getElementById('uploadMessage');
    const progressContainer = document.getElementById('progress-container');
    const submitButton = event.target.querySelector('button');
    if (fileInput.files.length === 0) {
        uploadMessage.textContent = 'Por favor, selecciona un archivo.';
        return;
    }
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    uploadMessage.textContent = '';
    progressContainer.style.display = 'block';
    submitButton.disabled = true;
    try {
        const result = await fetchAPI('/api/mensajes/upload', { method: 'POST', body: formData });
        uploadMessage.textContent = `${result.mensaje} Verificando estado...`;
        pollLoteStatus(result.loteId);
    } catch (error) {
        uploadMessage.textContent = `Error: ${error.message}`;
        progressContainer.style.display = 'none';
    } finally {
        submitButton.disabled = false;
        fileInput.value = '';
    }
}

function pollLoteStatus(loteId) {
    const uploadMessage = document.getElementById('uploadMessage');
    const progressBar = document.getElementById('progress-bar');
    const progressText = document.getElementById('progress-text');
    const progressContainer = document.getElementById('progress-container');
    const intervalId = setInterval(async () => {
        try {
            const statusResult = await fetchAPI(`/api/mensajes/lotes/${loteId}/status`);
            progressBar.style.width = `${statusResult.progress}%`;
            progressText.textContent = `${statusResult.progress}%`;
            if (statusResult.status === 'COMPLETADO') {
                clearInterval(intervalId);
                uploadMessage.textContent = '¡Procesamiento completado! Actualizando tablas...';
                progressBar.style.backgroundColor = 'var(--success-color)';
                setTimeout(() => {
                    localStorage.setItem('currentLoteId', loteId);
                    currentAsesorFilter = '';
                    document.getElementById('asesor-filter').value = '';
                    showTargetView('mensajes');
                    cargarMensajes(0);
                    cargarEstadisticas();
                    cargarFiltroAsesores();
                    progressContainer.style.display = 'none';
                    progressBar.style.width = '0%';
                    progressBar.style.backgroundColor = 'var(--text-light)';
                    uploadMessage.textContent = 'Tablas actualizadas.';
                }, 2000);
            } else if (statusResult.status === 'FALLIDO') {
                clearInterval(intervalId);
                uploadMessage.textContent = 'Error: El procesamiento del archivo en el servidor ha fallado.';
                progressBar.style.backgroundColor = 'var(--error-color)';
            } else {
                uploadMessage.textContent = `Procesando...`;
            }
        } catch (error) {
            clearInterval(intervalId);
            uploadMessage.textContent = 'Error de conexión al verificar el estado del proceso.';
            progressContainer.style.display = 'none';
        }
    }, 2000);
}