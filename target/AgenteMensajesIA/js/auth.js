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
        const response = await fetch('/api/auth/login', { // URL Corregida
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.error || `Error HTTP ${response.status}`);
        }

        localStorage.setItem('jwtToken', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('userRole', data.role);

        window.location.href = 'dashboard.html';

    } catch (error) {
        console.error('Error en el login:', error);
        messageDiv.textContent = error.message;
        messageDiv.className = 'error-message';
    }
}