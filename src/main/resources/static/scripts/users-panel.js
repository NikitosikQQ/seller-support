import {checkTokenExpirationAndGet} from "./panel.js";

// === Получение пользователей ===
export async function fetchUsers() {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/admin/users', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
            return;
        }
        const users = await response.json();
        renderUserTable(users);
    } catch (error) {
        console.error('Ошибка при получении пользователей:', error);
    }
}

// === Получение ролей ===
export async function fetchRoles() {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/admin/roles', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        }
        return await response.json();
    } catch (error) {
        console.error('Ошибка при получении ролей:', error);
        return [];
    }
}

// === Получение рабочих мест ===
export async function fetchWorkplaces() {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/workplaces/names', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });
        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
            return [];
        }
        return await response.json();
    } catch (error) {
        console.error('Ошибка при получении рабочих мест:', error);
        return [];
    }
}

// === Обновление пользователя ===
export async function fetchEditUser(oldUsername, updatedData) {
    const token = checkTokenExpirationAndGet();
    try {
        let updatedUsername = updatedData.updatedUsername;
        if (updatedUsername === oldUsername) {
            updatedUsername = null;
        }

        const body = {
            username: oldUsername,
            updatedUsername: updatedUsername,
            updatedRoles: updatedData.updatedRoles || null,
            workplaces: updatedData.workplaces === null ? null : updatedData.workplaces || null
        };

        const response = await fetch('/api/v1/admin/users', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        } else {
            fetchUsers();
        }
    } catch (error) {
        console.error('Ошибка при обновлении пользователя:', error);
        alert('Не удалось обновить пользователя');
    }
}

// === Удаление пользователя ===
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
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        } else {
            alert('Пользователь успешно удален');
            fetchUsers();
        }
    } catch (error) {
        console.error('Ошибка при удалении пользователя:', error);
        alert('Не удалось удалить пользователя');
    }
}

// === Отображение таблицы пользователей ===
export function renderUserTable(users) {
    const container = document.getElementById('main-container');
    if (!container) {
        console.error('Контейнер для таблицы не найден.');
        return;
    }

    container.innerHTML = '';

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

    const table = document.createElement('table');
    table.classList.add('user-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Имя пользователя', 'Роли', 'Рабочие места', 'Действия'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    users.forEach(user => {
        const workplacesDisplay = (user.workplaces && user.workplaces.length > 0)
            ? user.workplaces.join(', ')
            : '-';

        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.username}</td>
            <td>${user.roles.join(', ')}</td>
            <td>${workplacesDisplay}</td>
            <td>
                <button class="edit-button">Изменить</button>
                <button class="delete-button">Удалить</button>
            </td>
        `;
        tbody.appendChild(row);

        row.querySelector('.edit-button').addEventListener('click', () => openEditUserModal(user));
        row.querySelector('.delete-button').addEventListener('click', () => fetchDeleteUser(user.username));
    });
    table.appendChild(tbody);
    container.appendChild(table);
}

// === Модалка создания пользователя ===
export async function openCreateUserModal() {
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

    // === Роли ===
    const rolesLabel = document.createElement('label');
    rolesLabel.textContent = 'Роли';
    const rolesContainer = document.createElement('div');
    rolesContainer.classList.add('checkbox-group');

    const workplacesLabel = document.createElement('label');
    workplacesLabel.textContent = 'Рабочие места';
    const workplacesContainer = document.createElement('div');
    workplacesContainer.classList.add('checkbox-group');

    const [roles, workplaces] = await Promise.all([fetchRoles(), fetchWorkplaces()]);

    roles.forEach(role => {
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = `role-${role}`;
        checkbox.value = role;

        const label = document.createElement('label');
        label.htmlFor = `role-${role}`;
        label.textContent = role;
        label.classList.add('checkbox-label');

        const item = document.createElement('div');
        item.classList.add('role-item');
        item.appendChild(checkbox);
        item.appendChild(label);
        rolesContainer.appendChild(item);
    });

    workplaces.forEach(workplace => {
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.id = `workplace-${workplace}`;
        checkbox.value = workplace;

        const label = document.createElement('label');
        label.htmlFor = `workplace-${workplace}`;
        label.textContent = workplace;
        label.classList.add('checkbox-label');

        const item = document.createElement('div');
        item.classList.add('role-item');
        item.appendChild(checkbox);
        item.appendChild(label);
        workplacesContainer.appendChild(item);
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

        const selectedRoles = Array.from(rolesContainer.querySelectorAll('input[type="checkbox"]:checked')).map(c => c.value);
        const selectedWorkplaces = Array.from(workplacesContainer.querySelectorAll('input[type="checkbox"]:checked')).map(c => c.value);

        const newUser = {
            username: usernameInput.value,
            password: passwordInput.value,
            roles: selectedRoles,
            workplaces: selectedWorkplaces.length === 0 ? null : selectedWorkplaces
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/admin/users', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(newUser)
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

    form.append(headerForm, usernameLabel, usernameInput, passwordLabel, passwordInput, rolesLabel, rolesContainer, workplacesLabel, workplacesContainer, buttonGroup);
    modalContent.append(closeButton, form);
    modal.append(modalContent);
    document.body.append(modal);
}

// === Модалка редактирования пользователя ===
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

    // --- Username ---
    const usernameLabel = document.createElement('label');
    usernameLabel.textContent = 'Имя пользователя';
    const usernameInput = document.createElement('input');
    usernameInput.type = 'text';
    usernameInput.value = user.username;
    usernameInput.classList.add('input-not-role');

    // --- Roles ---
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

    // --- Workplaces ---
    const workplacesLabel = document.createElement('label');
    workplacesLabel.textContent = 'Рабочие места';
    const workplacesContainer = document.createElement('div');
    workplacesContainer.classList.add('checkbox-group');

    // Если user.workplaces == null, делаем пустой массив
    const userWorkplaces = user.workplaces ?? [];

    fetch('/api/v1/workplaces/names', {
        headers: { 'Authorization': `Bearer ${checkTokenExpirationAndGet()}` }
    })
        .then(res => res.json())
        .then(workplaces => {
            workplaces.forEach(workplace => {
                const checkbox = document.createElement('input');
                checkbox.type = 'checkbox';
                checkbox.id = `workplace-${workplace}`;
                checkbox.value = workplace;
                if (userWorkplaces.includes(workplace)) {  // теперь безопасно
                    checkbox.checked = true;
                }

                const label = document.createElement('label');
                label.htmlFor = `workplace-${workplace}`;
                label.textContent = workplace;
                label.classList.add('checkbox-label');

                const item = document.createElement('div');
                item.classList.add('role-item');
                item.appendChild(checkbox);
                item.appendChild(label);

                workplacesContainer.appendChild(item);
            });
        });

    // --- Buttons ---
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

    // --- Submit handler ---
    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const selectedRoles = Array.from(
            rolesContainer.querySelectorAll('input[type="checkbox"]:checked')
        ).map(checkbox => checkbox.value);

        const selectedWorkplaces = Array.from(
            workplacesContainer.querySelectorAll('input[type="checkbox"]:checked')
        ).map(checkbox => checkbox.value);

        const updatedData = {
            updatedUsername: usernameInput.value.trim() || user.username,
            updatedRoles: selectedRoles.length ? selectedRoles : [],
            workplaces: selectedWorkplaces.length ? selectedWorkplaces : []
        };

        await fetchEditUser(user.username, updatedData);
        modal.remove();
    });

    // --- Append all ---
    form.appendChild(headerForm);
    form.appendChild(usernameLabel);
    form.appendChild(usernameInput);
    form.appendChild(rolesLabel);
    form.appendChild(rolesContainer);
    form.appendChild(workplacesLabel);
    form.appendChild(workplacesContainer);
    form.appendChild(buttonGroup);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}

