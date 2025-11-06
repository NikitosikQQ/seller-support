import {checkTokenExpirationAndGet} from "./panel.js";

// === Получение всех рабочих мест ===
export async function fetchWorkplaces() {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/workplaces', {
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

        const workplaces = await response.json();
        renderWorkplaceTable(workplaces);
    } catch (error) {
        console.error('Ошибка при получении рабочих мест:', error);
    }
}

// === Обновление ставки рабочего места ===
export async function fetchUpdateWorkplaceRate(updatedWorkplace) {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/workplaces', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updatedWorkplace)
        });

        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        } else {
            alert('Ставка успешно обновлена');
            fetchWorkplaces();
        }
    } catch (error) {
        console.error('Ошибка при обновлении ставки рабочего места:', error);
        alert('Не удалось обновить ставку');
    }
}

// === Отображение таблицы рабочих мест ===
export function renderWorkplaceTable(workplaces) {
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
    header.textContent = 'Рабочие места';
    headerContainer.appendChild(header);

    // Без кнопки "Создать", только просмотр и изменение
    container.appendChild(headerContainer);

    const table = document.createElement('table');
    table.classList.add('user-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Название', 'Ставка', 'Действия'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    workplaces.forEach(workplace => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${workplace.workplace}</td>
            <td>${workplace.rate}</td>
            <td>
                <button class="edit-button edit-rate">Изменить ставку</button>
                <button class="edit-button edit-materials">Изменить коэффициенты по материалам</button>
            </td>
        `;
        tbody.appendChild(row);

        const editButton = row.querySelector('.edit-rate');
        editButton.addEventListener('click', () => openEditWorkplaceModal(workplace));

        const editMaterialsButton = row.querySelector('.edit-materials');
        editMaterialsButton.addEventListener('click', () => openEditWorkplaceMaterialModal(workplace));
    });
    table.appendChild(tbody);

    container.appendChild(table);
}

// === Модальное окно изменения ставки ===
export function openEditWorkplaceModal(workplace) {
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
    headerForm.textContent = `Изменение ставки — ${workplace.workplace}`;

    const nameLabel = document.createElement('label');
    nameLabel.textContent = 'Название рабочего места';
    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.value = workplace.workplace;
    nameInput.disabled = true;
    nameInput.classList.add('input-not-role');

    const rateLabel = document.createElement('label');
    rateLabel.textContent = 'Ставка';
    const rateInput = document.createElement('input');
    rateInput.type = 'number';
    rateInput.min = '0';
    rateInput.step = '0.01';
    rateInput.value = workplace.rate;
    rateInput.required = true;
    rateInput.classList.add('input-not-role');

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
        const updatedWorkplace = {
            id: workplace.id,
            rate: parseFloat(rateInput.value)
        };
        await fetchUpdateWorkplaceRate(updatedWorkplace);
        modal.remove();
    });

    form.appendChild(headerForm);
    form.appendChild(nameLabel);
    form.appendChild(nameInput);
    form.appendChild(rateLabel);
    form.appendChild(rateInput);
    form.appendChild(buttonGroup);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);
}


// === Модальное окно изменения ставки по материалам ===
let deletedMaterials = [];
let currentWorkplace = '';

export function openEditWorkplaceMaterialModal(workplace) {
    currentWorkplace = workplace.workplace;
    deletedMaterials = [];

    const modal = document.createElement('div');
    modal.classList.add('modal');

    const modalContent = document.createElement('div');
    modalContent.classList.add('modal-content');

    const closeButton = document.createElement('span');
    closeButton.textContent = '×';
    closeButton.classList.add('close-button');
    closeButton.addEventListener('click', () => modal.remove());

    const header = document.createElement('h3');
    header.textContent = `Коэффициенты по материалам — ${workplace.workplace}`;

    const container = document.createElement('div');
    container.classList.add('material-coefficient-container');

    const addButton = document.createElement('button');
    addButton.textContent = '+ добавить коэффициент к материалу';
    addButton.className = 'add-supply-button';
    addButton.addEventListener('click', () => addMaterialRow(container));

    modalContent.append(closeButton, header, addButton, container);

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.className = 'edit-button';
    saveButton.addEventListener('click', () => saveMaterialCoefficients(modal, container));

    modalContent.appendChild(saveButton);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    const token = checkTokenExpirationAndGet();

    // Загрузка существующих коэффициентов
    fetch(`/api/v1/workplaces/coefficient?workplace=${currentWorkplace}`, {
        headers: {'Authorization': `Bearer ${token}`}
    })
        .then(res => res.json())
        .then(coefMap => {
            const existingMaterials = Object.keys(coefMap).map(name => ({
                name,
                coefficient: coefMap[name]
            }));
            addMaterialRow(container, existingMaterials);
        })
        .catch(err => console.error(err));
}

// функция добавления строки
function addMaterialRow(container, materials = [{}]) {
    materials.forEach(material => {
        const row = document.createElement('div');
        row.className = 'material-row';
        row.style.display = 'flex';
        row.style.gap = '10px';
        row.style.marginBottom = '5px';

        const select = document.createElement('select');
        select.className = 'input-not-role';
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = 'Выберите материал';
        select.appendChild(defaultOption);

        const token = checkTokenExpirationAndGet();
        fetch('/api/v1/materials', {headers: {'Authorization': `Bearer ${token}`}})
            .then(res => res.json())
            .then(materialsList => {
                materialsList.forEach(m => {
                    const opt = document.createElement('option');
                    opt.value = m.name;
                    opt.textContent = m.name;
                    if (m.name === material.name) opt.selected = true;
                    select.appendChild(opt);
                });
            });

        const input = document.createElement('input');
        input.type = 'number';
        input.min = '0';
        input.step = '0.01';
        input.value = (material.coefficient !== undefined && material.coefficient !== null)
            ? material.coefficient
            : 1.0;
        input.className = 'input-not-role';

        const removeButton = document.createElement('button');
        removeButton.type = 'button';
        removeButton.textContent = '×';
        removeButton.className = 'remove-supply-button';
        removeButton.addEventListener('click', () => {
            if (select.value) deletedMaterials.push(select.value);
            row.remove();
        });

        row.append(select, input, removeButton);
        container.appendChild(row);
    });
}

// функция сохранения
function saveMaterialCoefficients(modal, container) {
    const token = checkTokenExpirationAndGet();
    const requestBody = [];

    document.querySelectorAll('.material-row').forEach(row => {
        const material = row.querySelector('select').value;
        const coefficient = parseFloat(row.querySelector('input').value);

        if (material) {
            requestBody.push({
                workplace: currentWorkplace,
                materialName: material,
                coefficient
            });
        }
    });

    deletedMaterials.forEach(material => {
        requestBody.push({
            workplace: currentWorkplace,
            materialName: material,
            coefficient: 1.0
        });
    });

    fetch('/api/v1/workplaces/coefficient', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(requestBody)
    })
        .then(res => {
            if (res.ok) {
                alert('Коэффициенты успешно сохранены');
                modal.remove();
            } else {
                res.json().then(data => alert('Ошибка: ' + data.message));
            }
        })
        .catch(err => console.error(err));
}
