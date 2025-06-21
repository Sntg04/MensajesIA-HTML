document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = 'index.html';
        return;
    }
    
    setupUI();
    setupEventListeners();
});

function setupUI() {
    document.getElementById('welcomeMessage').textContent = `Bienvenido, ${localStorage.getItem('username')}`;
    // Cargar la vista inicial (sección de usuarios para el admin)
    const userRole = localStorage.getItem('userRole');
    if (userRole === 'admin') {
        switchView('usuarios');
        cargarUsuarios();
    } else {
        switchView('estadisticas');
    }
    cargarEstadisticas();
}

// ---- MANEJO DE VISTAS Y EVENTOS ----

function setupEventListeners() {
    // Navegación del menú
    document.querySelector('.sidebar-nav').addEventListener('click', (event) => {
        if (event.target.matches('.nav-link')) {
            event.preventDefault();
            const targetId = event.target.getAttribute('data-target');
            switchView(targetId);
        }
    });

    // Logout
    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'index.html';
    });

    // Formulario de carga de archivo
    document.getElementById('uploadForm').addEventListener('submit', handleFileUpload);
    
    // Delegación de eventos para botones en la tabla de usuarios
    document.getElementById('userList').addEventListener('click', handleUserActions);
}

function switchView(targetId) {
    // Ocultar todas las secciones
    document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));
    // Desactivar todos los links
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

    // Mostrar la sección y activar el link correspondiente
    document.getElementById(targetId).classList.add('active');
    document.querySelector(`.nav-link[data-target="${targetId}"]`).classList.add('active');
}

// ---- LÓGICA DE LA API ----

async function fetchAPI(url, options = {}) {
    const defaultOptions = {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
            'Content-Type': 'application/json'
        }
    };
    const response = await fetch(url, { ...defaultOptions, ...options });
    if (!response.ok) {
        const errorData = await response.json().catch(() => ({ error: 'Error desconocido' }));
        throw new Error(errorData.error || `Error HTTP ${response.status}`);
    }
    // Si la respuesta no tiene cuerpo (ej. en un DELETE), devolvemos el status
    return response.status === 204 ? { success: true } : response.json();
}

async function cargarUsuarios() {
    const userList = document.getElementById('userList');
    userList.innerHTML = '<tr><td colspan="7">Cargando...</td></tr>';
    try {
        const usuarios = await fetchAPI('/api/usuarios');
        userList.innerHTML = '';
        usuarios.forEach(usuario => {
            const fecha = new Date(usuario.fechaCreacion).toLocaleDateString('es-ES');
            userList.innerHTML += `
                <tr>
                    <td>${usuario.id}</td>
                    <td>${usuario.username}</td>
                    <td>${usuario.nombreCompleto || 'N/A'}</td>
                    <td>${usuario.rol}</td>
                    <td>${usuario.activo ? 'Sí' : 'No'}</td>
                    <td>${fecha}</td>
                    <td>
                        <button class="edit-btn" data-id="${usuario.id}">Editar</button>
                        <button class="delete-btn" data-id="${usuario.id}" data-username="${usuario.username}">
                            ${usuario.activo ? 'Desactivar' : 'Activar'}
                        </button>
                    </td>
                </tr>`;
        });
    } catch (error) {
        document.getElementById('userError').textContent = `Error: ${error.message}`;
    }
}

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
        statsContainer.innerHTML = `<p class="error-message">Error al cargar estadísticas: ${error.message}</p>`;
    }
}

async function handleFileUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    const uploadMessage = document.getElementById('uploadMessage');
    
    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    uploadMessage.textContent = 'Procesando...';

    try {
        // Para FormData no se debe setear Content-Type, el browser lo hace
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

async function handleUserActions(event) {
    const target = event.target;
    const userId = target.dataset.id;

    if (target.matches('.delete-btn')) {
        const username = target.dataset.username;
        const isActive = target.textContent.trim() === 'Desactivar';
        const actionText = isActive ? 'desactivar' : 'activar';

        if (confirm(`¿Estás seguro de que quieres ${actionText} al usuario "${username}"?`)) {
            try {
                // Tu API ya tiene un endpoint para desactivar/activar cambiando el estado
                // Usaremos el endpoint de actualizar para cambiar el estado 'activo'
                const user = await fetchAPI(`/api/usuarios/${userId}`);
                await fetchAPI(`/api/usuarios/${userId}`, {
                    method: 'PUT',
                    body: JSON.stringify({ ...user, activo: !isActive })
                });
                alert(`Usuario ${actionText} con éxito.`);
                cargarUsuarios(); // Recargar la lista
            } catch (error) {
                alert(`Error al ${actionText} el usuario: ${error.message}`);
            }
        }
    }

    if (target.matches('.edit-btn')) {
        alert(`La funcionalidad de editar para el usuario ID: ${userId} aún no está implementada.`);
    }
}

