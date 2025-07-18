import {fetchUsers} from './users-panel.js';
import {fetchShops} from './shops-panel.js';
import {openModal} from './reports-panel.js'
import {fetchArticles} from "./article-panel.js";
import {fetchMaterials} from "./material-panel.js";
import {fetchComments} from "./comment-panel.js";


document.addEventListener('DOMContentLoaded', async () => {
    var token = checkTokenExpirationAndGet();

    // Извлечение ролей из токена
    const roles = getRolesFromToken(token);
    console.log('Роли:', roles);

    // Проверка роли и отображение соответствующих разделов
    if (roles.includes('ROLE_ADMIN')) {
        showAdminMenu();
    } else if (roles.includes('ROLE_MANAGER')) {
        showManagerMenu();
    } else if (roles.includes('ROLE_USER')) {
        showUserMenu();
    } else {
        alert('У вас нет доступа');
        window.location.href = '/';
    }

    // Слушаем клики по разделам меню
    document.getElementById('users-section').addEventListener('click', () => loadSection('admin'));
    document.getElementById('shops-section').addEventListener('click', () => loadSection('shops'));
    document.getElementById('reports-section').addEventListener('click', () => loadSection('user'));
    document.getElementById('articles-section').addEventListener('click', () => loadSection('articles'));
    document.getElementById('materials-section').addEventListener('click', () => loadSection('materials'));
    document.getElementById('comments-section').addEventListener('click', () => loadSection('comments'));

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
        openReportsMenu()
    } else if (section === 'shops') {
        await fetchShops();
    } else if (section === 'articles') {
        await fetchArticles(true);
    } else if (section === 'materials') {
        await fetchMaterials(true);
    } else if (section === 'comments') {
        await fetchComments(true);
    }
}

// Функция для отображения меню для админа
function showAdminMenu() {
    document.getElementById('users-section').style.display = 'block';
    document.getElementById('reports-section').style.display = 'block';
    document.getElementById('shops-section').style.display = 'block';
    document.getElementById('articles-section').style.display = 'block';
    document.getElementById('materials-section').style.display = 'block'
    document.getElementById('comments-section').style.display = 'block';
    openReportsMenu();

}

// Функция для отображения меню для обычного пользователя
function showUserMenu() {
    document.getElementById('users-section').style.display = 'none';  // Скрыть раздел "Пользователи"
    document.getElementById('shops-section').style.display = 'none'; // Скрыть раздел "Магазины"
    document.getElementById('articles-section').style.display = 'none'; // Скрыть раздел артикулы
    document.getElementById('materials-section').style.display = 'none'; // Скрыть раздел артикулы
    document.getElementById('reports-section').style.display = 'block'; // Показать только "Отчеты"
    document.getElementById('comments-section').style.display = 'none';
    openReportsMenu();
}

// Функция для отображения меню для менеджера
function showManagerMenu() {
    document.getElementById('users-section').style.display = 'none';
    document.getElementById('shops-section').style.display = 'block';
    document.getElementById('articles-section').style.display = 'block';
    document.getElementById('materials-section').style.display = 'block';
    document.getElementById('reports-section').style.display = 'block';
    document.getElementById('comments-section').style.display = 'block';
    openReportsMenu();
}


function openReportsMenu() {
    const container = document.getElementById('main-container');

    container.innerHTML = '';

    const header = document.createElement('h3');
    header.textContent = 'Генерация отчетов';  // Заголовок
    container.appendChild(header);

    const button = document.createElement('button');
    button.textContent = 'Отправления на отгрузке';
    button.className = 'create-report-button'
    button.addEventListener('click', () => openModal());
    container.appendChild(button);
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



