// =================================================================================
// SCRIPT PRINCIPAL DEL DASHBOARD - js/dashboard.js (Versión Final y Funcional)
// =================================================================================

// --- 1. ESTADO GLOBAL DE LA APLICACIÓN ---
const appState = {
    userCache: [],
    currentMessages: [],
    currentLoteId: null
};

// --- 2. PUNTO DE ENTRADA: Se ejecuta cuando la página está lista ---
document.addEventListener('DOMContentLoaded', () => {
    const token = getToken();
    if (!token) {
        alert('Acceso no autorizado. Se requiere iniciar sesión.');
        window.location.href = 'index.html';
        return;
    }
    const username = localStorage.getItem('username');
    document.getElementById('username-display').textContent = username;
    document.getElementById('logoutButton').addEventListener('click', logout);
    attachModalEventListeners();
    const userRole = localStorage.getItem('userRole');
    setupSidebarAndNavigation(userRole);
    document.getElementById('main-content').addEventListener('click', handleMainContentClicks);
});

// --- 3. MANEJO DE EVENTOS CENTRALIZADO ---
function attachModalEventListeners() {
    const modal = document.getElementById('userModal');
    document.getElementById('closeModalBtn').addEventListener('click', () => { modal.style.display = 'none'; });
    window.addEventListener('click', (event) => { if (event.target == modal) { modal.style.display = 'none'; } });
    document.getElementById('userForm').addEventListener('submit', handleUserFormSubmit);
}

function handleMainContentClicks(event) {
    const button = event.target.closest('button');
    if (!button) return;
    const action = button.id || button.dataset.action;
    const userId = button.dataset.userId;
    switch (action) {
        case 'showCreateUserFormBtn': openUserModalForCreate(); break;
        case 'edit': handleEditClick(userId); break;
        case 'deactivate': handleDeactivateClick(userId); break;
        case 'uploadButton': event.preventDefault(); document.getElementById('uploadForm').requestSubmit(); break;
        case 'exportFullReportBtn': if (appState.currentLoteId) exportFullReport(appState.currentLoteId); break;
        case 'filterTodos': renderMessagesTable(appState.currentMessages); break;
        case 'filterAlertas': renderMessagesTable(appState.currentMessages.filter(m => m.clasificacion === 'Alerta')); break;
        case 'filterBuenos': renderMessagesTable(appState.currentMessages.filter(m => m.clasificacion === 'Bueno')); break;
    }
}

// --- 4. NAVEGACIÓN Y RENDERIZADO DE VISTAS ---
function setupSidebarAndNavigation(role) {
    const sidebarContainer = document.getElementById('sidebar-container');
    if (role === 'admin') {
        sidebarContainer.innerHTML = getAdminSidebar();
        document.getElementById('nav-admin-dashboard').addEventListener('click', (e) => { e.preventDefault(); renderWelcomeView('admin'); });
        document.getElementById('nav-admin-users').addEventListener('click', (e) => { e.preventDefault(); renderUsersView(); });
    } else if (role === 'calidad') {
        sidebarContainer.innerHTML = getCalidadSidebar();
        document.getElementById('nav-calidad-inicio').addEventListener('click', (e) => { e.preventDefault(); renderWelcomeView('calidad'); });
        document.getElementById('nav-calidad-revisar').addEventListener('click', (e) => { e.preventDefault(); renderRevisarMensajesView(); });
    } else {
        alert('Rol no reconocido.');
        logout();
    }
    renderWelcomeView(role);
}

function renderWelcomeView(role) {
    const mainContent = document.getElementById('main-content');
    const message = role === 'admin' 
        ? '<h3>¡Bienvenido al Panel de Administración!</h3><p>Selecciona una opción del menú de la izquierda para comenzar a trabajar.</p>'
        : '<h3>¡Bienvenido al Panel de Calidad!</h3><p>Desde aquí podrás analizar los mensajes y gestionar las alertas.</p>';
    mainContent.innerHTML = `<div class="content-section">${message}</div>`;
}

function renderUsersView() {
    const mainContent = document.getElementById('main-content');
    mainContent.innerHTML = `
        <div id="usuarios-content" class="content-section">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h3>Gestión de Usuarios</h3>
                <button id="showCreateUserFormBtn" class="btn btn-create">Crear Nuevo Usuario</button>
            </div>
            <div id="userListTableContainer"><p>Cargando usuarios...</p></div>
            <div id="viewError" class="error"></div>
        </div>`;
    fetchUsers();
}

function renderRevisarMensajesView() {
    const mainContent = document.getElementById('main-content');
    mainContent.innerHTML = `
        <div class="content-section">
            <h3>Analizar Archivo de Mensajes</h3>
            <p>Selecciona un archivo Excel (.xlsx) para iniciar el análisis.</p>
            <div class="upload-form">
                <form id="uploadForm">
                    <input type="file" id="excelFile" name="file" accept=".xlsx" required>
                    <label for="excelFile" class="custom-file-upload">Elegir archivo</label>
                    <span id="fileNameDisplay">Ningún archivo seleccionado</span>
                    <button type="submit" id="uploadButton" class="btn btn-create" style="margin-left: auto;">Subir y Analizar</button>
                </form>
            </div>
            <div id="uploadStatus"></div>
        </div>
        <div id="resultsContainer" class="content-section hidden" style="margin-top: 30px;">
            <div style="display: flex; justify-content: space-between; align-items: center;">
                <h3>Resultados del Análisis</h3>
                <button id="exportFullReportBtn" class="btn btn-edit" style="display: none;">Exportar Reporte</button>
            </div>
            <div id="statsBar" class="stats-bar"></div>
            <div class="filter-buttons">
                <button id="filterTodos" class="btn">Todos</button>
                <button id="filterAlertas" class="btn">Alertas</button>
                <button id="filterBuenos" class="btn">Buenos</button>
            </div>
            <div id="resultsTableContainer"></div>
        </div>`;
    document.getElementById('uploadForm').addEventListener('submit', handleUpload);
    document.getElementById('excelFile').addEventListener('change', (e) => {
        const fileNameDisplay = document.getElementById('fileNameDisplay');
        if (fileNameDisplay) {
            fileNameDisplay.textContent = e.target.files.length > 0 ? e.target.files[0].name : 'Ningún archivo seleccionado';
        }
    });
}

// --- 5. LÓGICA DE GESTIÓN DE USUARIOS ---
async function fetchUsers() {
    try {
        const response = await fetch('/AgenteMensajesIA/api/usuarios', { headers: { 'Authorization': `Bearer ${getToken()}` } });
        if (!response.ok) throw new Error(`Error ${response.status}`);
        const users = await response.json();
        appState.userCache = users;
        renderUsersTable(users);
    } catch (error) {
        const viewError = document.getElementById('viewError');
        if (viewError) viewError.textContent = "Error al cargar la lista de usuarios.";
    }
}

function renderUsersTable(users) {
    const container = document.getElementById('userListTableContainer');
    if (!container) return;
    let tableHtml = '<table><thead><tr><th>ID</th><th>Username</th><th>Nombre</th><th>Rol</th><th>Activo</th><th>Acciones</th></tr></thead><tbody>';
    if (users && users.length > 0) {
        users.forEach(user => {
            tableHtml += `<tr><td>${user.id}</td><td>${escapeHtml(user.username)}</td><td>${escapeHtml(user.nombreCompleto || '')}</td><td>${escapeHtml(user.rol)}</td><td>${user.activo ? 'Sí' : 'No'}</td><td><button class="btn btn-edit" data-action="edit" data-user-id="${user.id}">Editar</button><button class="btn btn-deactivate" data-action="deactivate" data-user-id="${user.id}">Desactivar</button></td></tr>`;
        });
    } else {
        tableHtml += '<tr><td colspan="6" style="text-align:center;">No se encontraron usuarios.</td></tr>';
    }
    tableHtml += '</tbody></table>';
    container.innerHTML = tableHtml;
}

function openUserModalForCreate() {
    const form = document.getElementById('userForm');
    form.reset();
    document.getElementById('formTitle').textContent = 'Crear Nuevo Usuario';
    document.getElementById('userId').value = '';
    document.getElementById('usernameInput').disabled = false;
    document.getElementById('passwordInput').placeholder = "Contraseña requerida";
    document.getElementById('activoInput').style.display = 'none';
    document.getElementById('formError').textContent = '';
    document.getElementById('userModal').style.display = 'block';
}

function openUserModalForEdit(user) {
    const form = document.getElementById('userForm');
    form.reset();
    document.getElementById('formTitle').textContent = 'Editar Usuario';
    document.getElementById('userId').value = user.id;
    document.getElementById('usernameInput').value = user.username;
    document.getElementById('usernameInput').disabled = true;
    document.getElementById('nombreCompletoInput').value = user.nombreCompleto;
    document.getElementById('rolInput').value = user.rol;
    document.getElementById('activoCheckbox').checked = user.activo;
    document.getElementById('activoInput').style.display = 'flex';
    document.getElementById('passwordInput').placeholder = "Dejar en blanco para no cambiar";
    document.getElementById('formError').textContent = '';
    document.getElementById('userModal').style.display = 'block';
}

async function handleUserFormSubmit(event) {
    event.preventDefault();
    const userId = document.getElementById('userId').value;
    const password = document.getElementById('passwordInput').value;
    const userData = { username: document.getElementById('usernameInput').value, nombreCompleto: document.getElementById('nombreCompletoInput').value, rol: document.getElementById('rolInput').value, activo: document.getElementById('activoCheckbox').checked };
    if (password) { userData.passwordHash = password; }
    try {
        const url = userId ? `/AgenteMensajesIA/api/usuarios/${userId}` : '/AgenteMensajesIA/api/usuarios';
        const method = userId ? 'PUT' : 'POST';
        const response = await fetch(url, { method: method, headers: { 'Authorization': `Bearer ${getToken()}`, 'Content-Type': 'application/json' }, body: JSON.stringify(userData) });
        const result = await response.json();
        if (!response.ok) throw new Error(result.error || 'Error en el servidor.');
        alert(`Usuario "${result.username}" ${userId ? "actualizado" : "creado"} con éxito.`);
        document.getElementById('userModal').style.display = 'none';
        fetchUsers();
    } catch (error) {
        document.getElementById('formError').textContent = `Error: ${error.message}`;
    }
}

function handleEditClick(userId) {
    const userToEdit = appState.userCache.find(user => user.id === parseInt(userId, 10));
    if (userToEdit) { openUserModalForEdit(userToEdit); } else { alert("Error: No se pudo encontrar el usuario para editar."); }
}

async function handleDeactivateClick(userId) {
    const userToDeactivate = appState.userCache.find(user => user.id === parseInt(userId, 10));
    if (!userToDeactivate) return;
    if (confirm(`¿Estás seguro de que quieres desactivar al usuario "${userToDeactivate.username}"?`)) {
        try {
            const response = await fetch(`/AgenteMensajesIA/api/usuarios/${userId}/desactivar`, { method: 'DELETE', headers: { 'Authorization': `Bearer ${getToken()}` } });
            const result = await response.json();
            if (!response.ok) throw new Error(result.error || 'Error en el servidor.');
            alert(result.mensaje);
            fetchUsers();
        } catch (error) {
            alert(`Error al desactivar usuario: ${error.message}`);
        }
    }
}

// --- 6. LÓGICA DE "REVISAR MENSAJES" ---
async function handleUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById("excelFile");
    if (fileInput.files.length === 0) { alert("Por favor, selecciona un archivo."); return; }
    const uploadButton = document.getElementById("uploadButton");
    const uploadStatusDiv = document.getElementById("uploadStatus");
    uploadButton.disabled = true; uploadButton.textContent = "Procesando..."; uploadStatusDiv.style.display = "none";
    const formData = new FormData(); formData.append("file", fileInput.files[0]);
    try {
        const response = await fetch("/AgenteMensajesIA/api/mensajes/upload", { method: "POST", headers: { Authorization: `Bearer ${getToken()}` }, body: formData });
        if (!response.ok) throw new Error("Error en la respuesta del servidor.");
        const data = await response.json();
        appState.currentMessages = data.mensajes || []; appState.currentLoteId = data.loteId;
        uploadStatusDiv.className = "success"; uploadStatusDiv.innerHTML = `¡Éxito! Se procesaron ${appState.currentMessages.length} mensajes.`; uploadStatusDiv.style.display = "block";
        if (appState.currentLoteId) { document.getElementById("exportFullReportBtn").style.display = "inline-block"; }
        updateStatistics(appState.currentMessages);
        renderMessagesTable(appState.currentMessages);
        document.getElementById("resultsContainer").classList.remove("hidden");
    } catch (error) {
        uploadStatusDiv.className = "error"; uploadStatusDiv.textContent = "Error al procesar el archivo."; uploadStatusDiv.style.display = "block";
    } finally {
        uploadButton.disabled = false; uploadButton.textContent = "Subir y Analizar";
    }
}

async function exportFullReport(loteId) {
    const exportButton = document.getElementById("exportFullReportBtn");
    const originalText = exportButton.textContent;
    exportButton.textContent = "Generando..."; exportButton.disabled = true;
    try {
        const response = await fetch(`/AgenteMensajesIA/api/mensajes/lote/exportar?lote=${loteId}`, { method: "GET", headers: { Authorization: `Bearer ${getToken()}` } });
        if (!response.ok) throw new Error("Error al generar el reporte.");
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.style.display = "none"; a.href = downloadUrl; a.download = `reporte_completo_${loteId.substring(0, 8)}.xlsx`;
        document.body.appendChild(a); a.click(); a.remove(); window.URL.revokeObjectURL(downloadUrl);
    } catch (error) {
        alert("No se pudo descargar el reporte.");
    } finally {
        exportButton.textContent = originalText; exportButton.disabled = false;
    }
}

function updateStatistics(messages) {
    const total = messages.length;
    const alertas = messages.filter((m) => m.clasificacion === "Alerta").length;
    const buenos = total - alertas;
    const porcentajeAlertas = total > 0 ? (((total - buenos) / total) * 100).toFixed(1) : 0;
    document.getElementById("statsBar").innerHTML = `<div class="stat-card"><h4>Total</h4><p>${total}</p></div><div class="stat-card"><h4>Buenos</h4><p>${buenos}</p></div><div class="stat-card"><h4>Alertas</h4><p>${alertas}</p></div><div class="stat-card"><h4>% Alertas</h4><p>${porcentajeAlertas}%</p></div>`;
}

/**
 * **FUNCIÓN ACTUALIZADA**
 * Dibuja la tabla de mensajes, formateando la hora a HH:MM.
 * @param {Array} messages - El array de mensajes a renderizar.
 */
function renderMessagesTable(messages) {
    const container = document.getElementById("resultsTableContainer");
    if (!container) return;
    
    // Encabezados de la tabla
    let tableHtml = '<table><thead><tr><th>Fecha</th><th>Hora</th><th>Asesor</th><th>Aplicación</th><th>Mensaje</th><th>Clasificación</th><th>Sugerencia</th></tr></thead><tbody>';
    
    if (messages && messages.length > 0) {
        messages.forEach((msg) => {
            // **LA CORRECCIÓN ESTÁ AQUÍ**
            // Formatea la hora si existe, si no, muestra "N/A"
            const horaFormateada = msg.horaMensaje ? msg.horaMensaje.substring(0, 5) : "N/A";

            tableHtml += `<tr>
                <td>${escapeHtml(msg.fechaMensaje || "N/A")}</td>
                <td>${escapeHtml(horaFormateada)}</td>
                <td>${escapeHtml(msg.nombreAsesor)}</td>
                <td>${escapeHtml(msg.aplicacion)}</td>
                <td>${escapeHtml(msg.textoOriginal)}</td>
                <td>${escapeHtml(msg.clasificacion)}</td>
                <td>${escapeHtml(msg.textoReescrito || "")}</td>
            </tr>`;
        });
    } else {
        tableHtml += '<tr><td colspan="7" style="text-align:center;">No hay mensajes para mostrar.</td></tr>';
    }
    tableHtml += "</tbody></table>";
    container.innerHTML = tableHtml;
}


// --- 7. FUNCIONES DE UTILIDAD Y HTML ---
function escapeHtml(unsafe) { return unsafe ? unsafe.toString().replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;") : ""; }
function getAdminSidebar() { return `<h2>Menú Admin</h2><ul><li><a href="#" id="nav-admin-dashboard">Inicio</a></li><li><a href="#" id="nav-admin-users">Gestionar Usuarios</a></li></ul>`; }
function getCalidadSidebar() { return `<h2>Menú Calidad</h2><ul><li><a href="#" id="nav-calidad-inicio">Inicio</a></li><li><a href="#" id="nav-calidad-revisar">Revisar Mensajes</a></li></ul>`; }
