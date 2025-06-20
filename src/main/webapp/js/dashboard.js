/**
 * js/dashboard.js
 * Maneja la lógica del panel de control.
 */
document.addEventListener('DOMContentLoaded', () => {
    console.log("Dashboard DOM cargado. Verificando autenticación...");
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        console.error("No se encontró token. Redirigiendo a login.");
        window.location.href = 'index.html';
        return;
    }

    const username = localStorage.getItem('username');
    const userRole = localStorage.getItem('userRole');

    if (document.getElementById('welcomeMessage')) {
        document.getElementById('welcomeMessage').textContent = `Bienvenido, ${username}`;
    }
    if (document.getElementById('logoutButton')) {
        document.getElementById('logoutButton').addEventListener('click', logout);
    }
    
    console.log(`Usuario: ${username}, Rol: ${userRole}`);

    if (userRole === 'admin') {
        console.log("Usuario es admin. Mostrando sección de administración.");
        const adminSection = document.getElementById('adminSection');
        if(adminSection) {
            adminSection.style.display = 'block';
            cargarUsuarios();
        } else {
            console.error("No se encontró el elemento #adminSection en el HTML.");
        }
    }
    
    cargarEstadisticas();

    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', handleFileUpload);
    }
});

async function cargarUsuarios() {
    console.log("Intentando cargar usuarios...");
    const userList = document.getElementById('userList');
    const errorMessage = document.getElementById('userError');
    if (!userList || !errorMessage) {
        console.error("Faltan elementos HTML #userList o #userError");
        return;
    }

    errorMessage.textContent = '';
    userList.innerHTML = '<tr><td colspan="7">Cargando...</td></tr>';

    try {
        const response = await fetch('/api/usuarios', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('jwtToken')}` }
        });

        if (!response.ok) {
            throw new Error(`Error HTTP ${response.status}: ${await response.text()}`);
        }

        const usuarios = await response.json();
        console.log("Usuarios recibidos:", usuarios);
        userList.innerHTML = '';
        
        if (usuarios.length === 0) {
            userList.innerHTML = '<tr><td colspan="7">No se encontraron usuarios.</td></tr>';
            return;
        }

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
                    <button class="edit-btn" data-id="${usuario.id}">Editar</button>
                    <button class="delete-btn" data-id="${usuario.id}" data-username="${usuario.username}">Desactivar</button>
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

async function cargarEstadisticas() {
    console.log("Intentando cargar estadísticas...");
    const statsContainer = document.getElementById('messageStats');
    if(!statsContainer) {
        console.error("Falta elemento HTML #messageStats");
        return;
    }
    statsContainer.textContent = 'Cargando estadísticas...';

    try {
        const response = await fetch('/api/mensajes/stats', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('jwtToken')}` }
        });

        if (!response.ok) {
            throw new Error(`Error HTTP ${response.status}`);
        }

        const estadisticas = await response.json();
        console.log("Estadísticas recibidas:", estadisticas);
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

async function handleFileUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    const uploadMessage = document.getElementById('uploadMessage');
    
    if (fileInput.files.length === 0) {
        uploadMessage.textContent = 'Por favor, selecciona un archivo.';
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    uploadMessage.textContent = 'Subiendo y procesando...';

    try {
        const response = await fetch('/api/mensajes/upload', {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('jwtToken')}` },
            body: formData
        });

        const result = await response.json();
        if (!response.ok) throw new Error(result.error || `Error HTTP ${response.status}`);

        uploadMessage.textContent = `Archivo procesado. ${result.mensajesGuardados} mensajes guardados.`;
        cargarEstadisticas();
        if (localStorage.getItem('userRole') === 'admin') cargarUsuarios();
    } catch (error) {
        uploadMessage.textContent = `Error: ${error.message}`;
    }
}

function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}
