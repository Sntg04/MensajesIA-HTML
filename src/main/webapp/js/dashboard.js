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
    // ... (esta función no necesita cambios)
}

function showUserModal(user = null) {
    // ... (esta función no necesita cambios)
}

function hideUserModal() { 
    // ... (esta función no necesita cambios)
}

async function handleUserFormSubmit(event) {
    // ... (esta función no necesita cambios)
}

async function handleUserTableActions(event) {
    // ... (esta función no necesita cambios)
}

async function cargarUsuarios() {
    // ... (esta función no necesita cambios)
}

async function cargarMensajes() {
    // ... (esta función no necesita cambios)
}

async function exportarMensajes() {
    // ... (esta función no necesita cambios)
}

async function cargarEstadisticas() {
    // ... (esta función no necesita cambios)
}

// === FUNCIÓN ACTUALIZADA ===
async function handleFileUpload(event) {
    event.preventDefault();
    const fileInput = document.getElementById('fileInput');
    const uploadMessage = document.getElementById('uploadMessage');
    const submitButton = event.target.querySelector('button');

    if (fileInput.files.length === 0) {
        uploadMessage.textContent = 'Por favor, selecciona un archivo.';
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    uploadMessage.textContent = 'Subiendo archivo...';
    submitButton.disabled = true;

    try {
        const result = await fetchAPI('/api/mensajes/upload', { method: 'POST', body: formData });
        
        uploadMessage.textContent = `${result.mensaje} Verificando estado...`;
        
        // Iniciar el polling para verificar el estado del lote
        pollLoteStatus(result.loteId);

    } catch (error) {
        uploadMessage.textContent = `Error: ${error.message}`;
    } finally {
        submitButton.disabled = false;
        fileInput.value = '';
    }
}

// === NUEVA FUNCIÓN PARA EL POLLING ===
function pollLoteStatus(loteId) {
    const uploadMessage = document.getElementById('uploadMessage');
    let pollCount = 0;
    const maxPolls = 60; // Máximo 5 minutos de polling (60 * 5 segundos)

    const intervalId = setInterval(async () => {
        if (pollCount++ > maxPolls) {
            clearInterval(intervalId);
            uploadMessage.textContent = 'El procesamiento está tardando más de lo esperado. Los resultados aparecerán cuando finalice.';
            return;
        }

        try {
            const statusResult = await fetchAPI(`/api/mensajes/lotes/${loteId}/status`);
            
            if (statusResult.status === 'COMPLETADO') {
                clearInterval(intervalId);
                uploadMessage.textContent = '¡Procesamiento completado! Actualizando tablas...';
                // Usamos un pequeño delay para que el usuario pueda leer el mensaje final
                setTimeout(() => {
                    cargarMensajes();
                    cargarEstadisticas();
                    uploadMessage.textContent = 'Tablas actualizadas.';
                }, 1500);
            } else if (statusResult.status === 'FALLIDO') {
                clearInterval(intervalId);
                uploadMessage.textContent = 'Error: El procesamiento del archivo en el servidor ha fallado.';
            } else {
                // Sigue procesando, actualizamos el mensaje para dar feedback
                uploadMessage.textContent = `Procesando... (verificación ${pollCount})`;
            }
        } catch (error) {
            clearInterval(intervalId);
            uploadMessage.textContent = 'Error de conexión al verificar el estado del proceso.';
        }
    }, 5000); // Preguntar cada 5 segundos
}