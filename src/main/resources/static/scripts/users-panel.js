import {checkTokenExpirationAndGet} from "./panel.js";

export async function fetchDeleteUser(username) {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/admin/users', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({username})
        });
        if (!response.ok) {
            const data = response.json
            alert('Ошибка: ' + data.message);
        } else {
            alert('Пользователь успешно удален');
            fetchUsers(); // Перезагружаем список пользователей после удаления
        }
    } catch (error) {
        console.error('Ошибка при удалении пользователя:', error);
        alert('Не удалось удалить пользователя');
    }
}

export async function fetchUsers() {

    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/admin/users', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            const data = response.json
            alert('Ошибка: ' + data.message);
        }
        const users = await response.json();
        renderUserTable(users);
    } catch (error) {
        console.error('Ошибка при получении пользователей:', error);
    }
}

export async function fetchRoles() {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/admin/roles', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!response.ok) {
            const data = response.json
            alert('Ошибка: ' + data.message);
        }
        return await response.json();
    } catch (error) {
        console.error('Ошибка при получении ролей:', error);
        return [];
    }
}

export function renderUserTable(users) {
    const container = document.getElementById('main-container');
    if (!container) {
        console.error('Контейнер для таблицы не найден.');
        return;
    }

    // Очистка контейнера
    container.innerHTML = '';

    // Создаем заголовок с кнопкой "Создать"
    const headerContainer = document.createElement('div');
    headerContainer.style.display = 'flex';
    headerContainer.style.justifyContent = 'space-between';
    headerContainer.style.alignItems = 'center';

    const header = document.createElement('h3');
    header.textContent = 'Действующие пользователи';
    headerContainer.appendChild(header);

    const createButton = document.createElement('button');
    createButton.textContent = 'Создать';
    createButton.classList.add('edit-button');
    createButton.addEventListener('click', openCreateUserModal);
    headerContainer.appendChild(createButton);

    container.appendChild(headerContainer);

    // Создаем таблицу
    const table = document.createElement('table');
    table.classList.add('user-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Имя пользователя', 'Роли', 'Действия'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    users.forEach(user => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.username}</td>
            <td>${user.roles.join(', ')}</td>
            <td>
                <button class="edit-button" onclick="editUser(${user.username})">Изменить</button>
                <button class="delete-button">Удалить</button>
            </td>
        `;
        tbody.appendChild(row);
    });
    table.appendChild(tbody);

    container.appendChild(table);

    // Добавляем обработчики событий для кнопок "Удалить"
    const deleteButtons = document.querySelectorAll('.delete-button');
    deleteButtons.forEach((button, index) => {
        button.addEventListener('click', () => fetchDeleteUser(users[index].username));
    });
}

export function openCreateUserModal() {
    const modal = document.createElement('div');
    modal.classList.add('modal'); // Добавьте стили для модального окна в CSS

    const modalContent = document.createElement('div');
    modalContent.classList.add('modal-content');

    const closeButton = document.createElement('span');
    closeButton.textContent = '×';
    closeButton.classList.add('close-button');
    closeButton.addEventListener('click', () => modal.remove());

    const form = document.createElement('form');
    const headerForm = document.createElement('h3')
    headerForm.textContent = 'Cоздание пользователя';
    const usernameLabel = document.createElement('label');
    usernameLabel.textContent = 'Имя пользователя';
    const usernameInput = document.createElement('input');
    usernameInput.type = 'text';
    usernameInput.required = true;

    const passwordLabel = document.createElement('label');
    passwordLabel.textContent = 'Пароль';
    const passwordInput = document.createElement('input');
    passwordInput.type = 'password';
    passwordInput.required = true;

    const rolesLabel = document.createElement('label');
    rolesLabel.textContent = 'Роли';
    const rolesSelect = document.createElement('select');
    rolesSelect.multiple = true;

    fetchRoles().then(roles => {
        roles.forEach(role => {
            const option = document.createElement('option');
            option.value = role;
            option.textContent = role;
            rolesSelect.appendChild(option);
        });
    });

    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Отменить';
    cancelButton.classList.add('delete-button');
    cancelButton.type = 'button';
    cancelButton.addEventListener('click', () => modal.remove());

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.classList.add('edit-button');
    saveButton.type = 'submit';

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const newUser = {
            username: usernameInput.value,
            password: passwordInput.value,
            roles: Array.from(rolesSelect.selectedOptions).map(option => option.value)
        };

        try {
            var token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/admin/users', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(newUser)
            });

            if (!response.ok) {
                alert('Ошибка: ' + data.message);
            }

            alert('Пользователь успешно создан');
            modal.remove();
            fetchUsers(); // Перезагрузим таблицу пользователей
        } catch (error) {
            console.error('Ошибка при создании пользователя:', error);
            alert('Не удалось создать пользователя');
        }
    });

    form.appendChild(headerForm)
    form.appendChild(usernameLabel);
    form.appendChild(usernameInput);
    form.appendChild(passwordLabel);
    form.appendChild(passwordInput);
    form.appendChild(rolesLabel);
    form.appendChild(rolesSelect);
    form.appendChild(saveButton);
    form.appendChild(cancelButton);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}

