import {checkTokenExpirationAndGet} from "./panel.js";

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

export async function fetchEditUser(oldUsername, updatedData) {
    const token = checkTokenExpirationAndGet();
    try {
        let updatedUsername = updatedData.updatedUsername
        if(updatedUsername === oldUsername) {
            updatedUsername = null
        }

        const response = await fetch('/api/v1/admin/users', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: oldUsername,
                updatedUsername: updatedUsername,
                updatedRoles: updatedData.updatedRoles || null
            })
        });
        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        } else {
            fetchUsers(); // Перезагружаем список пользователей после обновления
        }
    } catch (error) {
        console.error('Ошибка при обновлении пользователя:', error);
        alert('Не удалось обновить пользователя');
    }
}

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
                <button class="edit-button">Изменить</button>
                <button class="delete-button">Удалить</button>
            </td>
        `;
        tbody.appendChild(row);

        const editButton = row.querySelector('.edit-button');
        editButton.addEventListener('click', () => openEditUserModal(user));

        const deleteButton = row.querySelector('.delete-button');
        deleteButton.addEventListener('click', () => fetchDeleteUser(user.username));
    });
    table.appendChild(tbody);

    container.appendChild(table);
}
export function openCreateUserModal() {
    const modal = document.createElement('div');
    modal.classList.add('modal');

    const modalContent = document.createElement('div');
    modalContent.classList.add('modal-content');

    const closeButton = document.createElement('span');
    closeButton.textContent = '×';
    closeButton.classList.add('close-button');
    closeButton.addEventListener('click', () => modal.remove());

    const form = document.createElement('form');
    const headerForm = document.createElement('h3');
    headerForm.textContent = 'Создание пользователя';

    const usernameLabel = document.createElement('label');
    usernameLabel.textContent = 'Имя пользователя';
    const usernameInput = document.createElement('input');
    usernameInput.type = 'text';
    usernameInput.required = true;
    usernameInput.classList.add('input-not-role');

    const passwordLabel = document.createElement('label');
    passwordLabel.textContent = 'Пароль';
    const passwordInput = document.createElement('input');
    passwordInput.type = 'password';
    passwordInput.required = true;
    passwordInput.classList.add('input-not-role');

    const rolesLabel = document.createElement('label');
    rolesLabel.textContent = 'Роли';
    const rolesContainer = document.createElement('div');
    rolesContainer.classList.add('checkbox-group'); // Добавляем класс для группы чекбоксов

    fetchRoles().then((roles) => {
        roles.forEach((role) => {
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = `role-${role}`;
            checkbox.value = role;

            const label = document.createElement('label');
            label.htmlFor = `role-${role}`;
            label.textContent = role;
            label.classList.add('checkbox-label'); // Добавляем класс для метки чекбокса

            const roleItem = document.createElement('div');
            roleItem.classList.add('role-item');
            roleItem.appendChild(checkbox);
            roleItem.appendChild(label);

            rolesContainer.appendChild(roleItem);
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

    const buttonGroup = document.createElement('div');
    buttonGroup.classList.add('button-group');
    buttonGroup.appendChild(saveButton);
    buttonGroup.appendChild(cancelButton);

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const selectedRoles = Array.from(
            rolesContainer.querySelectorAll('input[type="checkbox"]:checked')
        ).map((checkbox) => checkbox.value);

        const newUser = {
            username: usernameInput.value,
            password: passwordInput.value,
            roles: selectedRoles,
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/admin/users', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newUser),
            });

            if (!response.ok) {
                const data = await response.json();
                alert('Ошибка: ' + data.message);
            } else {
                alert('Пользователь успешно создан');
                modal.remove();
                fetchUsers();
            }
        } catch (error) {
            console.error('Ошибка при создании пользователя:', error);
            alert('Не удалось создать пользователя');
        }
    });

    form.appendChild(headerForm);
    form.appendChild(usernameLabel);
    form.appendChild(usernameInput);
    form.appendChild(passwordLabel);
    form.appendChild(passwordInput);
    form.appendChild(rolesLabel);
    form.appendChild(rolesContainer);
    form.appendChild(buttonGroup);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}


export function openEditUserModal(user) {
    const modal = document.createElement('div');
    modal.classList.add('modal');

    const modalContent = document.createElement('div');
    modalContent.classList.add('modal-content');

    const closeButton = document.createElement('span');
    closeButton.textContent = '×';
    closeButton.classList.add('close-button');
    closeButton.addEventListener('click', () => modal.remove());

    const form = document.createElement('form');
    const headerForm = document.createElement('h3');
    headerForm.textContent = 'Редактирование пользователя';

    const usernameLabel = document.createElement('label');
    usernameLabel.textContent = 'Имя пользователя';
    const usernameInput = document.createElement('input');
    usernameInput.type = 'text';
    usernameInput.value = user.username;
    usernameInput.classList.add('input-not-role');

    const rolesLabel = document.createElement('label');
    rolesLabel.textContent = 'Роли';
    const rolesContainer = document.createElement('div');
    rolesContainer.classList.add('checkbox-group');

    fetchRoles().then(roles => {
        roles.forEach(role => {
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.id = `role-${role}`;
            checkbox.value = role;
            if (user.roles.includes(role)) {
                checkbox.checked = true;
            }

            const label = document.createElement('label');
            label.htmlFor = `role-${role}`;
            label.textContent = role;
            label.classList.add('checkbox-label');

            const roleItem = document.createElement('div');
            roleItem.classList.add('role-item');
            roleItem.appendChild(checkbox);
            roleItem.appendChild(label);

            rolesContainer.appendChild(roleItem);
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

    const buttonGroup = document.createElement('div');
    buttonGroup.classList.add('button-group');
    buttonGroup.appendChild(saveButton);
    buttonGroup.appendChild(cancelButton);

    form.addEventListener('submit', (event) => {
        event.preventDefault();

        const selectedRoles = Array.from(rolesContainer.querySelectorAll('input[type="checkbox"]:checked')).map(
            checkbox => checkbox.value
        );

        const updatedData = {
            updatedUsername: usernameInput.value.trim() || user.username,
            updatedRoles: selectedRoles
        };

        fetchEditUser(user.username, updatedData);
        modal.remove();
    });

    form.appendChild(headerForm);
    form.appendChild(usernameLabel);
    form.appendChild(usernameInput);
    form.appendChild(rolesLabel);
    form.appendChild(rolesContainer);
    form.appendChild(buttonGroup);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}

