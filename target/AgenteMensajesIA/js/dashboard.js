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
    const userRole = localStorage.getItem('userRole');
    if (userRole === 'admin') {
        switchView('usuarios');
        cargarUsuarios();
    } else {
        document.querySelector('.nav-link[data-target="usuarios"]').style.display = 'none';
        switchView('estadisticas');
    }
    cargarEstadisticas();
}

function setupEventListeners() {
    document.querySelector('.sidebar-nav').addEventListener('click', e => {
        if (e.target.matches('.nav-link')) {
            e.preventDefault();
            switchView(e.target.dataset.target);
        }
    });

    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'index.html';
    });

    document.getElementById('show-create-user-modal').addEventListener('click', () => showUserModal());
    document.getElementById('cancel-user-modal').addEventListener('click', hideUserModal);
    document.getElementById('user-form').addEventListener('submit', handleUserFormSubmit);
    document.getElementById('userList').addEventListener('click', handleUserTableActions);
}

function switchView(targetId) {
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    document.getElementById(targetId).classList.add('active');
    document.querySelector(`.nav-link[data-target="${targetId}"]`).classList.add('active');
}

// --- LÓGICA DEL MODAL DE USUARIOS ---

function showUserModal(user = null) {
    const form = document.getElementById('user-form');
    form.reset();
    document.getElementById('form-error').textContent = '';
    
    if (user) {
        document.getElementById('modal-title').textContent = 'Editar Usuario';
        document.getElementById('user-id').value = user.id;
        document.getElementById('username').value = user.username;
        document.getElementById('nombreCompleto').value = user.nombreCompleto;
        document.getElementById('rol').value = user.rol;
        document.getElementById('password').placeholder = 'Dejar en blanco para no cambiar';
        document.getElementById('username').disabled = true;
    } else {
        document.getElementById('modal-title').textContent = 'Crear Usuario';
        document.getElementById('user-id').value = '';
        document.getElementById('password').placeholder = '';
        document.getElementById('username').disabled = false;
    }
    document.getElementById('user-modal').style.display = 'flex';
}

function hideUserModal() {
    document.getElementById('user-modal').style.display = 'none';
}

async function handleUserFormSubmit(event) {
    event.preventDefault();
    const id = document.getElementById('user-id').value;
    const isEditing = !!id;
    
    const userData = {
        username: document.getElementById('username').value,
        nombreCompleto: document.getElementById('nombreCompleto').value,
        rol: document.getElementById('rol').value,
        passwordHash: document.getElementById('password').value,
    };
    
    // En modo edición, si la contraseña está vacía, no la enviamos para no sobreescribirla
    if (isEditing && !userData.passwordHash) {
        delete userData.passwordHash;
    } else if (!isEditing && !userData.passwordHash) {
         document.getElementById('form-error').textContent = 'La contraseña es requerida para nuevos usuarios.';
         return;
    }
    
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
    const button = event.target.closest('button');
    if (!button) return;

    const userId = button.dataset.id;
    if (button.matches('.btn-edit')) {
        const user = await fetchAPI(`/api/usuarios/${userId}`);
        showUserModal(user);
    }
    if (button.matches('.btn-deactivate, .btn-activate')) {
        const isActive = button.classList.contains('btn-deactivate');
        if (confirm(`¿Seguro que quieres ${isActive ? 'desactivar' : 'activar'} este usuario?`)) {
            try {
                const user = await fetchAPI(`/api/usuarios/${userId}`);
                await fetchAPI(`/api/usuarios/${userId}`, {
                    method: 'PUT',
                    body: JSON.stringify({ ...user, activo: !isActive })
                });
                cargarUsuarios();
            } catch (error) {
                alert(`Error: ${error.message}`);
            }
        }
    }
}

// --- LÓGICA DE API ---

async function fetchAPI(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
            'Content-Type': 'application/json'
        }
    };
    const response = await fetch(url, { ...defaultOptions, ...options });
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ error: 'Error desconocido en el servidor.' }));
        throw new Error(errorData.error || `Error HTTP ${response.status}`);
    }
    return response.status === 204 ? { success: true } : response.json();
}

async function cargarUsuarios() {
    const userList = document.getElementById('userList');
    userList.innerHTML = '<tr><td colspan="7">Cargando...</td></tr>';
    try {
        const usuarios = await fetchAPI('/api/usuarios');
        userList.innerHTML = '';
        usuarios.forEach(u => {
            const fecha = new Date(u.fechaCreacion).toLocaleDateString('es-ES');
            userList.innerHTML += `
                <tr>
                    <td>${u.id}</td><td>${u.username}</td><td>${u.nombreCompleto||'N/A'}</td>
                    <td>${u.rol}</td><td>${u.activo ? 'Sí' : 'No'}</td><td>${fecha}</td>
                    <td>
                        <button class="btn-action btn-edit" data-id="${u.id}">Editar</button>
                        <button class="btn-action ${u.activo ? 'btn-deactivate' : 'btn-activate'}" data-id="${u.id}">${u.activo ? 'Desactivar' : 'Activar'}</button>
                    </td>
                </tr>`;
        });
    } catch (error) {
        document.getElementById('userError').textContent = `Error: ${error.message}`;
    }
}

// ... Las funciones cargarEstadisticas y handleFileUpload pueden permanecer como en la versión anterior ...
// Se incluyen aquí por completitud.
async function cargarEstadisticas() {
    const statsContainer = document.getElementById('messageStats');
    statsContainer.innerHTML = 'Cargando...';
    try {
        const stats = await fetchAPI('/api/mensajes/stats');
        statsContainer.innerHTML = `
            <p><strong>Total de Mensajes:</strong> ${stats.totalMensajes || 0}</p>
            <p><strong>Promedio Confianza (SPAM):</strong> ${((stats.confianzaPromedioSpam || 0) * 100).toFixed(2)}%</p>
            <p><strong>Promedio Confianza (NO SPAM):</strong> ${((stats.confianzaPromedioNoSpam || 0) * 100).toFixed(2)}%</p>
        `;
    } catch (error) {
        statsContainer.innerHTML = `<p class="error-message">Error al cargar estadísticas.</p>`;
    }
}

async function handleFileUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    const uploadMessage = document.getElementById('uploadMessage');
    if (fileInput.files.length === 0) { uploadMessage.textContent = 'Selecciona un archivo.'; return; }
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    uploadMessage.textContent = 'Procesando...';
    try {
        const response = await fetch('/api/mensajes/upload', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('jwtToken')}` },
            body: formData
        });
        const result = await response.json();
        if (!response.ok) throw new Error(result.error);
        uploadMessage.textContent = `Éxito: ${result.mensajesGuardados} mensajes guardados.`;
        cargarEstadisticas();
    } catch (error) {
        uploadMessage.textContent = `Error: ${error.message}`;
    }
}
