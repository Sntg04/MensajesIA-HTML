document.addEventListener('DOMContentLoaded', () => {
    // Verificación inicial de autenticación
    const token = localStorage.getItem('jwtToken');
    if (!token) {
        window.location.href = 'index.html';
        return;
    }
    
    // Configuración inicial de la UI
    setupUI();
    
    // Carga de datos inicial
    const userRole = localStorage.getItem('userRole');
    if (userRole === 'admin') {
        cargarUsuarios();
    }
    cargarEstadisticas();
    
    // Manejadores de eventos
    setupEventListeners();
});

function setupUI() {
    const username = localStorage.getItem('username');
    document.getElementById('welcomeMessage').textContent = `Bienvenido, ${username}`;
}

function setupEventListeners() {
    // Navegación del menú lateral
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.addEventListener('click', (event) => {
            event.preventDefault();
            
            // Quitar clase activa de todos los links y secciones
            navLinks.forEach(l => l.classList.remove('active'));
            document.querySelectorAll('.content-section').forEach(s => s.classList.remove('active'));

            // Añadir clase activa al link clickeado y a la sección correspondiente
            link.classList.add('active');
            const targetId = link.getAttribute('data-target');
            document.getElementById(targetId).classList.add('active');
        });
    });

    // Logout
    document.getElementById('logoutButton').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = 'index.html';
    });

    // Formulario de carga de archivo
    document.getElementById('uploadForm').addEventListener('submit', handleFileUpload);
}

async function cargarUsuarios() {
    const userList = document.getElementById('userList');
    const errorMessage = document.getElementById('userError');
    if (!userList || !errorMessage) return;

    errorMessage.textContent = '';
    userList.innerHTML = '<tr><td colspan="7">Cargando...</td></tr>';

    try {
        const response = await fetch('/api/usuarios', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('jwtToken')}` }
        });
        if (!response.ok) throw new Error(`Error HTTP ${response.status}`);
        
        const usuarios = await response.json();
        userList.innerHTML = '';
        
        usuarios.forEach(usuario => {
            const fecha = usuario.fechaCreacion ? new Date(usuario.fechaCreacion).toLocaleDateString('es-ES') : 'N/A';
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${usuario.id}</td>
                <td>${usuario.username}</td>
                <td>${usuario.nombreCompleto || 'N/A'}</td>
                <td>${usuario.rol}</td>
                <td>${usuario.activo ? 'Sí' : 'No'}</td>
                <td>${fecha}</td>
                <td>
                    <button class="edit-btn">Editar</button>
                    <button class="delete-btn">Desactivar</button>
                </td>
            `;
            userList.appendChild(row);
        });

    } catch (error) {
        errorMessage.textContent = 'Error al cargar la lista de usuarios.';
    }
}

async function cargarEstadisticas() {
    const statsContainer = document.getElementById('messageStats');
    if (!statsContainer) return;

    statsContainer.innerHTML = 'Cargando...';
    try {
        const response = await fetch('/api/mensajes/stats', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('jwtToken')}` }
        });
        if (!response.ok) throw new Error(`Error HTTP ${response.status}`);

        const estadisticas = await response.json();
        statsContainer.innerHTML = `
            <p><strong>Total de Mensajes:</strong> ${estadisticas.totalMensajes || 0}</p>
            <p><strong>Promedio de Confianza (SPAM):</strong> ${((estadisticas.confianzaPromedioSpam || 0) * 100).toFixed(2)}%</p>
            <p><strong>Promedio de Confianza (NO SPAM):</strong> ${((estadisticas.confianzaPromedioNoSpam || 0) * 100).toFixed(2)}%</p>
        `;
    } catch (error) {
        statsContainer.innerHTML = '<p class="error-message">Error al cargar estadísticas.</p>';
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

        uploadMessage.textContent = `Archivo procesado con éxito. ${result.mensajesGuardados} mensajes fueron guardados.`;
        // Recargar datos después de la subida
        cargarEstadisticas();
        if (localStorage.getItem('userRole') === 'admin') {
            cargarUsuarios();
        }
    } catch (error) {
        uploadMessage.textContent = `Error: ${error.message}`;
    }
}