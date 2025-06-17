/**
 * js/auth.js
 * Maneja la lógica de autenticación.
 */
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }
});

async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const messageDiv = document.getElementById('errorMessage');
    messageDiv.textContent = '';

    try {
        const response = await fetch('/AgenteMensajesIA/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.error || `Error HTTP ${response.status}`);
        }

        messageDiv.textContent = '¡Login exitoso! Redirigiendo...';
        messageDiv.className = 'success-message';
        localStorage.setItem('jwtToken', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('userRole', data.role);

        setTimeout(() => {
            window.location.href = 'dashboard.html';
        }, 1000);

    } catch (error) {
        console.error('Error en el login:', error);
        messageDiv.textContent = error.message;
        messageDiv.className = 'error-message';
    }
}

// Funciones de utilidad para ser usadas por dashboard.js
function getToken() {
    return localStorage.getItem('jwtToken');
}

function logout() {
    localStorage.clear();
    alert('Sesión cerrada.');
    window.location.href = 'index.html';
}
