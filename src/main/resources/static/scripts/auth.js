const STORAGE_KEY = 'auth_token'; // Ключ для хранения токена в localStorage

// Функция для получения токена из localStorage
function getAuthToken() {
    return localStorage.getItem(STORAGE_KEY);
}

// Функция для сохранения токена в localStorage
function setAuthToken(token) {
    localStorage.setItem(STORAGE_KEY, token);
}

// Функция для удаления токена из localStorage
function removeAuthToken() {
    localStorage.removeItem(STORAGE_KEY);
}

// Функция для декодирования JWT токена (для проверки срока действия)
function decodeToken(token) {
    const payload = token.split('.')[1];
    const decoded = JSON.parse(atob(payload));
    console.log(decoded);
    return decoded;
}

// Функция для проверки, не истек ли срок действия токена
function isTokenExpired(token) {
    const decoded = decodeToken(token);
    const now = Date.now() / 1000; // Текущее время в секундах
    return decoded.exp < now;
}

// Функция для добавления токена в заголовки запроса
async function addAuthHeader(requestOptions) {
    let token = getAuthToken();

    if (!token) {
        throw new Error('No auth token found');
    }

    // Проверка, истек ли токен
    if (isTokenExpired(token)) {
        await removeAuthToken();
    }

    // Добавляем токен в заголовки запроса
    requestOptions.headers = {
        ...requestOptions.headers,
        'Authorization': `Bearer ${token}`
    };

    return requestOptions;
}

// Функция для выполнения запросов с авторизацией
async function fetchWithAuth(url, requestOptions = {}) {
    try {
        const optionsWithAuth = await addAuthHeader(requestOptions);
        return await fetch(url, optionsWithAuth);
    } catch (error) {
        console.error('Error with authorized fetch:', error);
        throw error;
    }
}

// Экспортируем функции
export {getAuthToken, setAuthToken, removeAuthToken, fetchWithAuth};