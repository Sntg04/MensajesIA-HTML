document.addEventListener('DOMContentLoaded', () => {
    if (!localStorage.getItem('jwtToken')) { window.location.href = 'index.html'; return; }
    setupUI();
    setupEventListeners();
});

function setupUI() {
    document.getElementById('welcomeMessage').textContent = `Bienvenido, ${localStorage.getItem('username')}`;
    const userRole = localStorage.getItem('userRole');
    if (userRole === 'admin') {
        switchView('usuarios');
        cargarUsuarios();
    } else {
        document.querySelector('.nav-link[data-target="usuarios"]').style.display = 'none';
        switchView('mensajes');
    }
    cargarMensajes();
    cargarEstadisticas();
}

function setupEventListeners() {
    document.querySelector('.sidebar-nav').addEventListener('click', e => { if (e.target.matches('.nav-link')) { e.preventDefault(); switchView(e.target.dataset.target); } });
    document.getElementById('logoutButton').addEventListener('click', () => { localStorage.clear(); window.location.href = 'index.html'; });
    document.getElementById('export-excel-btn').addEventListener('click', exportarMensajes);
    document.getElementById('show-create-user-modal').addEventListener('click', () => showUserModal());
    document.getElementById('cancel-user-modal').addEventListener('click', hideUserModal);
    document.getElementById('user-form').addEventListener('submit', handleUserFormSubmit);
    document.getElementById('userList').addEventListener('click', handleUserTableActions);
    document.getElementById('uploadForm').addEventListener('submit', handleFileUpload);
}

function switchView(targetId) {
    document.querySelectorAll('.content-section.active').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-link.active').forEach(l => l.classList.remove('active'));
    document.getElementById(targetId).classList.add('active');
    document.querySelector(`.nav-link[data-target="${targetId}"]`).classList.add('active');
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
    if (response.status === 204) return { success: true };
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1) {
        return response.json();
    }
    return response.blob();
}

function showUserModal(user = null) {
    const form = document.getElementById('user-form'); form.reset();
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

    const userData = {
        nombreCompleto: document.getElementById('nombreCompleto').value,
        rol: document.getElementById('rol').value
    };
    
    if (!isEditing) {
        userData.username = document.getElementById('username').value;
    }
    if (password) {
        userData.passwordHash = password;
    }

    const url = isEditing ? `/api/usuarios/${id}` : '/api/usuarios';
    const method = isEditing ? 'PUT' : 'POST';

    try {
        await fetchAPI(url, { method, body: JSON.stringify(userData) });
        hideUserModal();
        cargarUsuarios();
    } catch (error) { document.getElementById('form-error').textContent = `Error: ${error.message}`; }
}

async function handleUserTableActions(event) {
    const button = event.target.closest('button.btn-action');
    if (!button) return;
    const userId = button.dataset.id;

    if (button.matches('.btn-edit')) {
        try {
            const user = await fetchAPI(`/api/usuarios/${userId}`);
            showUserModal(user);
        } catch (error) { alert(`Error al cargar datos del usuario: ${error.message}`); }
        return;
    }

    if (button.matches('.btn-deactivate, .btn-activate')) {
        const wantsToDeactivate = button.classList.contains('btn-deactivate');
        if (confirm(`¿Seguro que quieres ${wantsToDeactivate ? 'desactivar' : 'activar'} este usuario?`)) {
            try {
                // Solo enviamos el campo que queremos cambiar
                await fetchAPI(`/api/usuarios/${userId}`, { 
                    method: 'PUT', 
                    body: JSON.stringify({ activo: !wantsToDeactivate }) 
                });
                cargarUsuarios();
            } catch (error) { alert(`Error: ${error.message}`); }
        }
    }
}

async function cargarUsuarios() {
    const userList = document.getElementById('userList');
    userList.innerHTML = '<tr><td colspan="7">Cargando...</td></tr>';
    try {
        const usuarios = await fetchAPI('/api/usuarios');
        userList.innerHTML = '';
        usuarios.forEach(u => {
            const fecha = new Date(u.fechaCreacion).toLocaleDateString('es-ES');
            userList.innerHTML += `<tr><td>${u.id}</td><td>${u.username}</td><td>${u.nombreCompleto||'N/A'}</td><td>${u.rol}</td>
                <td><span class="status ${u.activo ? 'status-active' : 'status-inactive'}">${u.activo ? 'Sí' : 'No'}</span></td><td>${fecha}</td>
                <td><button class="btn-action btn-edit" data-id="${u.id}">Editar</button>
                    <button class="btn-action ${u.activo ? 'btn-deactivate' : 'btn-activate'}" data-id="${u.id}">${u.activo ? 'Desactivar' : 'Activar'}</button>
                </td></tr>`;
        });
    } catch (error) { document.getElementById('userError').textContent = `Error al cargar usuarios: ${error.message}`; }
}

async function cargarMensajes() {
    const messageList = document.getElementById('messageList');
    messageList.innerHTML = '<tr><td colspan="7">Cargando...</td></tr>'; // Ahora son 7 columnas
    try {
        const mensajes = await fetchAPI('/api/mensajes');
        messageList.innerHTML = '';
        if(mensajes.length === 0){ 
            messageList.innerHTML = '<tr><td colspan="7">No hay mensajes para mostrar. Sube un archivo.</td></tr>'; 
            return; 
        }
        mensajes.forEach(m => {
            // Formatear la fecha del mensaje que viene del Excel
            let fechaMensaje = 'N/A';
            if (m.fechaHoraMensaje) {
                fechaMensaje = new Date(m.fechaHoraMensaje).toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'medium' });
            }
            
            messageList.innerHTML += `
                <tr class="${m.clasificacion === 'Alerta' ? 'row-alert' : ''}">
                    <td>${m.id}</td>
                    <td>${m.nombreAsesor || 'N/A'}</td>
                    <td>${m.aplicacion || 'N/A'}</td>
                    <td>${m.texto}</td>
                    <td>${m.clasificacion}</td>
                    <td>${m.observacion || 'N/A'}</td>
                    <td>${fechaMensaje}</td>
                </tr>`;
        });
    } catch (error) { 
        document.getElementById('messageError').textContent = `Error al cargar mensajes: ${error.message}`; 
    }
}

async function exportarMensajes() {
    const button = document.getElementById('export-excel-btn');
    button.textContent = 'Generando...'; button.disabled = true;
    try {
        const blob = await fetchAPI('/api/mensajes/export');
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none'; a.href = url;
        a.download = `Reporte_Mensajes_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(a); a.click(); window.URL.revokeObjectURL(url); a.remove();
    } catch (error) { alert(`Error al exportar: ${error.message}`); }
    finally { button.textContent = 'Exportar a Excel'; button.disabled = false; }
}

async function cargarEstadisticas() {
    const statsContainer = document.getElementById('messageStats');
    statsContainer.innerHTML = 'Cargando...';
    try {
        const stats = await fetchAPI('/api/mensajes/stats');
        statsContainer.innerHTML = `<p><strong>Total de Mensajes:</strong> ${stats.totalMensajes || 0}</p><p><strong>Confianza SPAM:</strong> ${((stats.confianzaPromedioSpam || 0) * 100).toFixed(2)}%</p><p><strong>Confianza NO SPAM:</strong> ${((stats.confianzaPromedioNoSpam || 0) * 100).toFixed(2)}%</p>`;
    } catch (error) { statsContainer.innerHTML = `<p class="error-message">Error al cargar estadísticas.</p>`; }
}

async function handleFileUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    const uploadMessage = document.getElementById('uploadMessage');
    const submitButton = event.target.querySelector('button');
    if (fileInput.files.length === 0) { uploadMessage.textContent = 'Por favor, selecciona un archivo.'; return; }
    
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    uploadMessage.textContent = 'Procesando archivo...';
    submitButton.disabled = true;

    try {
        const result = await fetchAPI('/api/mensajes/upload', { method: 'POST', body: formData });
        uploadMessage.textContent = `Éxito: ${result.mensajesGuardados} mensajes guardados del lote ${result.loteId}.`;
        fileInput.value = '';
        cargarMensajes();
        cargarEstadisticas();
    } catch (error) { uploadMessage.textContent = `Error: ${error.message}`; }
    finally { submitButton.disabled = false; }
}