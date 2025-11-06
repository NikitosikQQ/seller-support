import {checkTokenExpirationAndGet} from "./panel.js";

// === Получение всех цветов ===
export async function fetchColors(needTable) {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/colors', {
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

        const colors = await response.json();
        if(needTable) {
            renderColorTable(colors);
        }
    } catch (error) {
        console.error('Ошибка при получении цветов:', error);
    }
}

// === Добавление нового цвета ===
export async function fetchCreateColor(newColor) {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/colors', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(newColor)
        });

        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        } else {
            alert('Цвет успешно добавлен');
            fetchColors(true);
        }
    } catch (error) {
        console.error('Ошибка при добавлении цвета:', error);
        alert('Не удалось добавить цвет');
    }
}

// === Обновление цвета ===
export async function fetchUpdateColor(updatedColor) {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/colors', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedColor)
        });

        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        } else {
            alert('Цвет успешно обновлен');
            fetchColors(true);
        }
    } catch (error) {
        console.error('Ошибка при обновлении цвета:', error);
        alert('Не удалось обновить цвет');
    }
}

// === Удаление цвета ===
export async function fetchDeleteColor(id) {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/colors', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ id })
        });

        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        } else {
            alert('Цвет успешно удален');
            fetchColors(true);
        }
    } catch (error) {
        console.error('Ошибка при удалении цвета:', error);
        alert('Не удалось удалить цвет');
    }
}

// === Отображение таблицы цветов ===
export function renderColorTable(colors) {
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
    header.textContent = 'Цвета';
    headerContainer.appendChild(header);

    const createButton = document.createElement('button');
    createButton.textContent = 'Создать';
    createButton.classList.add('edit-button');
    createButton.addEventListener('click', openCreateColorModal);
    headerContainer.appendChild(createButton);

    container.appendChild(headerContainer);

    const table = document.createElement('table');
    table.classList.add('user-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Номер цвета', 'Название цвета', 'Действия'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    colors.forEach(color => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${color.number}</td>
            <td>${color.name}</td>
            <td>
                <button class="edit-button">Изменить</button>
                <button class="delete-button">Удалить</button>
            </td>
        `;
        tbody.appendChild(row);

        const editButton = row.querySelector('.edit-button');
        editButton.addEventListener('click', () => openEditColorModal(color));

        const deleteButton = row.querySelector('.delete-button');
        deleteButton.addEventListener('click', () => fetchDeleteColor(color.id));
    });
    table.appendChild(tbody);

    container.appendChild(table);
}

// === Модальное окно создания цвета ===
export function openCreateColorModal() {
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
    headerForm.textContent = 'Создание цвета';

    const numberLabel = document.createElement('label');
    numberLabel.textContent = 'Номер цвета';
    const numberInput = document.createElement('input');
    numberInput.type = 'number';
    numberInput.required = true;
    numberInput.classList.add('input-not-role');

    const nameLabel = document.createElement('label');
    nameLabel.textContent = 'Название цвета';
    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.required = true;
    nameInput.classList.add('input-not-role');

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
        const newColor = {
            number: parseInt(numberInput.value),
            name: nameInput.value.trim(),
        };
        await fetchCreateColor(newColor);
        modal.remove();
    });

    form.appendChild(headerForm);
    form.appendChild(numberLabel);
    form.appendChild(numberInput);
    form.appendChild(nameLabel);
    form.appendChild(nameInput);
    form.appendChild(buttonGroup);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);
}

// === Модальное окно редактирования цвета ===
export function openEditColorModal(color) {
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
    headerForm.textContent = 'Редактирование цвета';

    const numberLabel = document.createElement('label');
    numberLabel.textContent = 'Номер цвета';
    const numberInput = document.createElement('input');
    numberInput.type = 'number';
    numberInput.value = color.number;
    numberInput.classList.add('input-not-role');

    const nameLabel = document.createElement('label');
    nameLabel.textContent = 'Название цвета';
    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.value = color.name;
    nameInput.classList.add('input-not-role');

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
        const updatedColor = {
            id: color.id,
            number: parseInt(numberInput.value),
            name: nameInput.value.trim()
        };
        await fetchUpdateColor(updatedColor);
        modal.remove();
    });

    form.appendChild(headerForm);
    form.appendChild(numberLabel);
    form.appendChild(numberInput);
    form.appendChild(nameLabel);
    form.appendChild(nameInput);
    form.appendChild(buttonGroup);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);
}
