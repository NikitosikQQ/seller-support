import {fetchUsers, fetchDeleteUser} from './users-panel.js';


document.addEventListener('DOMContentLoaded', async () => {
    var token = checkTokenExpirationAndGet();

    // Извлечение ролей из токена
    const roles = getRolesFromToken(token);
    console.log('Роли:', roles);

    // Проверка роли и отображение соответствующих разделов
    if (roles.includes('ROLE_ADMIN')) {
        showAdminMenu();
    } else if (roles.includes('ROLE_USER')) {
        showUserMenu();
    } else {
        alert('У вас нет доступа');
        window.location.href = '/';
    }

    // Слушаем клики по разделам меню
    document.getElementById('users-section').addEventListener('click', () => loadSection('admin'));
    document.getElementById('reports-section').addEventListener('click', () => loadSection('user'));

    document.getElementById('logout-btn').addEventListener('click', () => {
        localStorage.removeItem('token');
        alert('Вы вышли из системы');
        window.location.href = '/';
    });
});

// Функция для загрузки контента в зависимости от раздела
async function loadSection(section) {
    if (section === 'admin') {
        await fetchUsers();
    } else if (section === 'user') {
        openAssignmentReports()
    }
}

// Функция для отображения меню для админа
function showAdminMenu() {
    document.getElementById('users-section').style.display = 'block';
    document.getElementById('reports-section').style.display = 'block';
    openAssignmentReports();

}

// Функция для отображения меню для обычного пользователя
function showUserMenu() {
    document.getElementById('users-section').style.display = 'none';  // Скрыть раздел "Пользователи"
    document.getElementById('reports-section').style.display = 'block'; // Показать только "Отчеты"
    openAssignmentReports();
}

function openAssignmentReports() {
    const container = document.getElementById('main-container');

    container.innerHTML = '';

    const header = document.createElement('h3');
    header.textContent = 'Генерация отчетов';  // Заголовок
    container.appendChild(header);

    const content = document.createElement('p');
    content.textContent = "ОТЧЕТЫ ЕЕЕ";  // Текст
    container.appendChild(content);
}

// Функция для декодирования токена и получения ролей
function getRolesFromToken(token) {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload));
    return decoded.roles || [];
}

// Функция для декодирования токена и проверки его срока действия
export function checkTokenExpirationAndGet() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('Вы не авторизованы!');
        window.location.href = '/';
        return;
    }

    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload));
    const exp = decoded.exp * 1000; // Преобразование в миллисекунды
    if (Date.now() > exp) {
        alert('Срок действия вашей сессии истек. Авторизуйтесь снова.');
        localStorage.removeItem('token');
        window.location.href = '/';
    }
    return token;
}



