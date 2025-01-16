document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/auth', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('token', data.token);
            window.location.href = '/panel';
        } else {
            const error = await response.json();
            document.getElementById('error-message').innerText = error.message || 'Ошибка авторизации';
        }
    } catch (error) {
        document.getElementById('error-message').innerText = 'Сервер недоступен';
    }
});