import {setAuthToken} from './auth.js';
import {getUserData} from "./api.js";

document.getElementById('loginForm').addEventListener('submit', async function (event) {
    event.preventDefault(); // Отключить стандартную отправку формы

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    try {
        const response = await fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({username, password}),
        });
        const contentType = response.headers.get('Content-Type');

        if (!response.ok) {
            if (contentType && contentType.includes('application/json')) {
                const errorData = await response.json();
                alert(`${errorData.message}`);
            } else {
                alert('Произошла неизвестная ошибка.');
            }
            return;
        }

        if (contentType && contentType.includes('application/json')) {
            const data = await response.json();
            const token = data.token;
            console.log(token);
            setAuthToken(token);
            console.log('Успешно сохранен токен')
            await getUserData()
        } else {
            const text = await response.text();
            alert('Некорретный ответ от сервера ' + text);
        }

    } catch (error) {
        console.error('Error:', error);
        alert('An error occurred while trying to log in.');
    }
});