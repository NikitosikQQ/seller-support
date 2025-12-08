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

let deletedMaterials = [];
let currentWorkplace = '';

// === Открытие модалки ===
export function openEditWorkplaceMaterialModal(workplace) {
    currentWorkplace = workplace.workplace;
    deletedMaterials = [];

    const modal = document.createElement('div');
    modal.classList.add('modal-coefficients');

    const modalContent = document.createElement('div');
    modalContent.classList.add('modal-content-coefficients');

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

    // ⚡ Добавляем блокировку кнопки
    addButton.addEventListener('click', () => {
        addButton.disabled = true; // блокируем
        addMaterialRateCoefficientRow(container, addButton);
    });

    modalContent.append(closeButton, header, addButton, container);

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.className = 'edit-button';
    saveButton.addEventListener('click', () => saveMaterialCoefficients(modal, container, addButton));

    modalContent.appendChild(saveButton);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    // Загрузка существующих коэффициентов
    const token = checkTokenExpirationAndGet();
    fetch(`/api/v1/workplaces/coefficients?workplace=${currentWorkplace}`, {
        headers: {'Authorization': `Bearer ${token}`}
    })
        .then(res => res.json())
        .then(data => {
            const materials = data.map(item => ({
                id: item.id,
                materialName: item.materialName,
                coefficient: item.coefficient,
                minAreaInMeters: item.minArea,
                maxAreaInMeters: item.maxArea
            }));
            addMaterialRateCoefficientRow(container, materials, addButton);
        })
        .catch(err => console.error(err));
}


// === Добавление строки коэффициента ===
function addMaterialRateCoefficientRow(container, materialsRate = [{}], addButton) {
    // Если передан не массив — оборачиваем в массив
    if (!Array.isArray(materialsRate)) {
        materialsRate = [materialsRate];
    }

    materialsRate.forEach(rate => {
        const row = document.createElement('div');
        row.className = 'material-row';
        row.style.display = 'flex';
        row.style.gap = '10px';
        row.style.marginBottom = '5px';
        if (rate.id) row.dataset.id = rate.id;

        // Select материала
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
                    if (m.name === rate.materialName) opt.selected = true;
                    select.appendChild(opt);
                });
            });

        // Min и Max инпуты
        const minInput = document.createElement('input');
        minInput.type = 'number';
        minInput.min = '0';
        minInput.step = '0.01';
        minInput.value = (rate.minAreaInMeters || rate.minAreaInMeters === 0) ? rate.minAreaInMeters : 0;
        minInput.className = 'input-not-role min-area-input';
        minInput.style.display = 'none';

        const maxInput = document.createElement('input');
        maxInput.type = 'number';
        maxInput.min = '0';
        maxInput.step = '0.01';
        maxInput.value = (rate.maxAreaInMeters || rate.maxAreaInMeters === 0) ? rate.maxAreaInMeters : 0;
        maxInput.className = 'input-not-role max-area-input';
        maxInput.style.display = 'none';

        // Див для отображения area
        const areaDisplay = document.createElement('div');
        areaDisplay.className = 'area-display';
        areaDisplay.textContent = (minInput.value == 0 && maxInput.value == 0)
            ? 'по умолчанию'
            : `${minInput.value} - ${maxInput.value} м²`;
        areaDisplay.style.cursor = 'pointer';

        areaDisplay.addEventListener('mouseenter', () => {
            areaDisplay.textContent = 'изменить';
        });
        areaDisplay.addEventListener('mouseleave', () => {
            areaDisplay.textContent = (minInput.value == 0 && maxInput.value == 0)
                ? 'по умолчанию'
                : `${minInput.value} - ${maxInput.value} м²`;
        });
        areaDisplay.addEventListener('click', () => {
            areaDisplay.style.display = 'none';
            minInput.style.display = 'inline-block';
            maxInput.style.display = 'inline-block';
        });

        // Coefficient
        const input = document.createElement('input');
        input.type = 'number';
        input.min = '0';
        input.step = '0.01';
        input.value = (rate.coefficient !== undefined && rate.coefficient !== null) ? rate.coefficient : 1.0;
        input.className = 'input-not-role coefficient-input';

        // Удаление строки
        const removeButton = document.createElement('button');
        removeButton.type = 'button';
        removeButton.textContent = '×';
        removeButton.className = 'remove-supply-button';
        removeButton.addEventListener('click', () => {
            if (row.dataset.id) deletedMaterials.push(row.dataset.id);
            row.remove();
            if (addButton) addButton.disabled = false;
        });

        row.append(select, areaDisplay, minInput, maxInput, input, removeButton);
        container.appendChild(row);

    });
}

async function saveMaterialCoefficients(modal, container, addButton) {
    const token = checkTokenExpirationAndGet();
    const updateList = [];
    let hasEmptyMaterial = false;

    // --- Формируем списки для PATCH и POST ---
    const rows = container.querySelectorAll('.material-row');

    // Сохраняем только новые строки в createList
    const createList = [];
    rows.forEach(row => {
        const select = row.querySelector('select');
        const materialName = select.value;
        const coefficient = parseFloat(row.querySelector('.coefficient-input').value);
        const minArea = parseFloat(row.querySelector('.min-area-input').value) || 0;
        const maxArea = parseFloat(row.querySelector('.max-area-input').value) || 0;

        if (!materialName) {
            hasEmptyMaterial = true;
            return;
        }

        const rowData = {
            workplace: currentWorkplace,
            materialName,
            coefficient,
            minAreaInMeters: minArea,
            maxAreaInMeters: maxArea
        };

        if (row.dataset.id) {
            console.log("строка на обновление:", row, rowData)
            updateList.push({ id: row.dataset.id, ...rowData });
        } else {
            console.log("строка на создание:", row, rowData)
            createList.push({ row, payload: rowData }); // Сохраняем ссылку на row
        }
    });

    if (hasEmptyMaterial) {
        alert('Пожалуйста, выберите материал для всех строк.');
        return;
    }

    try {
        // --- Обновление существующих коэффициентов ---
        if (updateList.length > 0) {
            const resPatch = await fetch('/api/v1/workplaces/coefficients', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(updateList)
            });
            if (!resPatch.ok) {
                const data = await resPatch.json();
                throw new Error('Ошибка при обновлении коэффициентов: ' + data.message);
            }
        }

        // --- DELETE удалённых коэффициентов ---
        if (deletedMaterials.length > 0) {
            const body = deletedMaterials.map(id => ({ id }));
            const resDelete = await fetch('/api/v1/workplaces/coefficients', {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(body)
            });
            if (!resDelete.ok) {
                const data = await resDelete.json();
                throw new Error('Ошибка при удалении коэффициентов: ' + data.message);
            }
            deletedMaterials = [];
        }

        // --- Создание новых коэффициентов (по одному объекту) ---
        for (const item of createList) {
            console.log("строка на сохранение:", item.payload);
            const res = await fetch('/api/v1/workplaces/coefficients', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(item.payload)
            });

            if (!res.ok) {
                const data = await res.json();
                throw new Error('Ошибка при создании коэффициента: ' + data.message);
            }

            const data = await res.json();
            console.log("пришел ответ на сохранение: ", data)
            if (data) {
                item.row.dataset.id = data; // Сохраняем UUID на самой строке,чтобы повторно не отправлять на сохранение
            }
        }

        alert('Сохранение прошло успешно!');
        if (addButton) addButton.disabled = false;

    } catch (err) {
        alert(err.message);
    }
}