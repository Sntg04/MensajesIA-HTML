/**
 * js/dashboard.js
 * Maneja la lógica del panel de control.
 */

// Se ejecuta cuando el contenido del DOM está completamente cargado
document.addEventListener('DOMContentLoaded', () => {
    // Verificar si hay un token. Si no, redirigir al login.
    const token = getToken();
    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    // Personalizar el saludo y mostrar/ocultar elementos según el rol
    const username = localStorage.getItem('username');
    const userRole = localStorage.getItem('userRole');

    document.getElementById('welcomeMessage').textContent = `Bienvenido, ${username}`;
    document.getElementById('logoutButton').addEventListener('click', logout);
    
    // --- CORRECCIÓN DE RUTAS EN TODAS LAS LLAMADAS FETCH ---

    // Si el usuario es admin, puede ver y gestionar usuarios.
    if (userRole === 'admin') {
        document.getElementById('adminSection').style.display = 'block';
        cargarUsuarios();
    }
    
    // Cargar las estadísticas de mensajes para todos los roles
    cargarEstadisticas();

    // Configurar el formulario de subida de archivos
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', handleFileUpload);
    }
});

/**
 * Carga la lista de usuarios desde la API. Solo para admins.
 */
async function cargarUsuarios() {
    const userList = document.getElementById('userList');
    const errorMessage = document.getElementById('userError');
    errorMessage.textContent = '';
    userList.innerHTML = 'Cargando...';

    try {
        const response = await fetch('/api/usuarios', { // URL Corregida
            headers: { 'Authorization': `Bearer ${getToken()}` }
        });

        if (!response.ok) {
            throw new Error(`Error HTTP ${response.status}: ${await response.text()}`);
        }

        const usuarios = await response.json();
        userList.innerHTML = ''; // Limpiar el "Cargando..."
        
        usuarios.forEach(usuario => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${usuario.id}</td>
                <td>${usuario.username}</td>
                <td>${usuario.nombreCompleto || 'N/A'}</td>
                <td>${usuario.rol}</td>
                <td>${usuario.activo ? 'Sí' : 'No'}</td>
                <td>${new Date(usuario.fechaCreacion).toLocaleDateString()}</td>
                <td>
                    <button class="edit-btn" onclick="editarUsuario(${usuario.id})">Editar</button>
                    <button class="delete-btn" onclick="desactivarUsuario(${usuario.id}, '${usuario.username}')">Desactivar</button>
                </td>
            `;
            userList.appendChild(row);
        });

    } catch (error) {
        console.error('Error al cargar usuarios:', error);
        errorMessage.textContent = 'Error al cargar la lista de usuarios.';
        userList.innerHTML = '';
    }
}

/**
 * Carga las estadísticas de mensajes desde la API.
 */
async function cargarEstadisticas() {
    const statsContainer = document.getElementById('messageStats');
    statsContainer.textContent = 'Cargando estadísticas...';

    try {
        const response = await fetch('/api/mensajes/stats', { // URL Corregida
            headers: { 'Authorization': `Bearer ${getToken()}` }
        });

        if (!response.ok) {
            throw new Error(`Error HTTP ${response.status}`);
        }

        const estadisticas = await response.json();
        statsContainer.innerHTML = `
            <p><strong>Total de Mensajes:</strong> ${estadisticas.totalMensajes}</p>
            <p><strong>Promedio de Confianza (SPAM):</strong> ${(estadisticas.confianzaPromedioSpam * 100).toFixed(2)}%</p>
            <p><strong>Promedio de Confianza (NO SPAM):</strong> ${(estadisticas.confianzaPromedioNoSpam * 100).toFixed(2)}%</p>
        `;
    } catch (error) {
        console.error('Error al cargar estadísticas:', error);
        statsContainer.textContent = 'Error al cargar estadísticas.';
    }
}

/**
 * Maneja la subida del archivo de mensajes.
 */
async function handleFileUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    const uploadMessage = document.getElementById('uploadMessage');

    if (fileInput.files.length === 0) {
        uploadMessage.textContent = 'Por favor, selecciona un archivo.';
        uploadMessage.className = 'error';
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);

    uploadMessage.textContent = 'Subiendo y procesando archivo...';
    uploadMessage.className = 'info';

    try {
        const response = await fetch('/api/mensajes/upload', { // URL Corregida
            method: 'POST',
            headers: { 'Authorization': `Bearer ${getToken()}` },
            body: formData
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || `Error HTTP ${response.status}`);
        }

        uploadMessage.textContent = `Archivo procesado exitosamente. ${result.mensajesGuardados} mensajes fueron guardados.`;
        uploadMessage.className = 'success';
        
        // Recargar estadísticas después de una subida exitosa
        cargarEstadisticas();

    } catch (error) {
        console.error('Error en la subida de archivo:', error);
        uploadMessage.textContent = `Error: ${error.message}`;
        uploadMessage.className = 'error';
    }
}

// Funciones de utilidad que podrían ser llamadas por los botones
function editarUsuario(id) {
    // Lógica para abrir un modal o redirigir a una página de edición
    alert(`Funcionalidad de editar para usuario ID: ${id} no implementada.`);
}

function desactivarUsuario(id, username) {
    if (confirm(`¿Estás seguro de que quieres desactivar al usuario "${username}"?`)) {
        // Lógica para llamar a la API y desactivar
        alert(`Funcionalidad de desactivar para usuario ID: ${id} no implementada.`);
    }
}

// Funciones de autenticación (ya presentes en auth.js, pero necesarias aquí también)
function getToken() {
    return localStorage.getItem('jwtToken');
}

function logout() {
    localStorage.clear();
    alert('Sesión cerrada.');
    window.location.href = 'index.html';
}
