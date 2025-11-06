import {checkTokenExpirationAndGet} from "./panel.js";

export async function fetchMaterials(needTable) {
    var token = checkTokenExpirationAndGet();
    const response = await fetch('/api/v1/materials', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    if (!response.ok) {
        const data = response.json
        alert('Ошибка: ' + data.message);
        return null;
    }
    const materials = await response.json();
    if (needTable) {
        renderMaterialTable(materials)
    }
    return materials;
}

export async function fetchSortingPostingBy() {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/materials/sorting-params', {
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
        return await response.json();
    } catch (error) {
        console.error('Ошибка при получении материалов:', error);
    }
}

export async function fetchDeleteMaterial(name) {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/materials', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({name})
        });
        if (!response.ok) {
            const data = response.json
            alert('Ошибка: ' + data.message);
        } else {
            alert('Материал успешно удален')
            fetchMaterials(true)
        }
    } catch (error) {
        console.error('Ошибка при получении материалов:', error);
    }
}

export function renderMaterialTable(materials) {
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
    header.textContent = 'Информация по материалам';
    headerContainer.appendChild(header);

    const createButton = document.createElement('button');
    createButton.textContent = 'Создать';
    createButton.classList.add('edit-button');
    createButton.addEventListener('click', openCreateMaterialModal);
    headerContainer.appendChild(createButton);

    container.appendChild(headerContainer);

    const table = document.createElement('table');
    table.classList.add('shop-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Наименование', 'Разделитель в отчете', 'Сортировка по', "Используется в шаблоне ЧПУ", 'Действия'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);
    const tbody = document.createElement('tbody');
    materials.forEach(material => {
        var useInChpuTemplate = material.useInChpuTemplate ? "Да" : "Нет";
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${material.name}</td>
            <td>${material.separatorName || ''}</td>
            <td>${material.sortingPostingBy}</td>
            <td>${useInChpuTemplate}</td>
            <td>
                <button class="edit-button">Изменить</button>
                <button class="delete-button">Удалить</button>
            </td>
        `;
        tbody.appendChild(row);

        const editButton = row.querySelector('.edit-button');
        editButton.addEventListener('click', () => openEditMaterialModal(material));

        const deleteButton = row.querySelector('.delete-button');
        deleteButton.addEventListener('click', () => fetchDeleteMaterial(material.name));
    });
    table.appendChild(tbody);

    container.appendChild(table);
}

// ---------------------- МОДАЛКА СОЗДАНИЯ ----------------------

export function openCreateMaterialModal() {
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
    headerForm.textContent = 'Создание артикула';

    const materialNameLabel = document.createElement('label');
    materialNameLabel.textContent = 'Наименование';
    const materialNameInput = document.createElement('input');
    materialNameInput.type = 'text';
    materialNameInput.required = true;
    materialNameInput.classList.add('input-not-role');

    const separatorNameLabel = document.createElement('label');
    separatorNameLabel.textContent = 'Текст-разделитель в отчетах';
    const separatorNameInput = document.createElement('input');
    separatorNameInput.type = 'text';
    separatorNameInput.classList.add('input-not-role');

    const sortingNameLabel = document.createElement('label');
    sortingNameLabel.textContent = 'Сортировка по';
    const sortingDropdown = document.createElement('select');
    sortingDropdown.classList.add('material-dropdown');
    sortingDropdown.name = 'material';
    sortingDropdown.required = true;

    // поле employeeRateCoefficient полностью удалено 👇

    const isOnlyPackagingLabel = document.createElement('label');
    isOnlyPackagingLabel.textContent = 'Только упаковывается';
    const isOnlyPackagingCheckbox = document.createElement('input');
    isOnlyPackagingCheckbox.type = 'checkbox';
    isOnlyPackagingCheckbox.classList.add('check-box-chpu');

    const useInChpuLabel = document.createElement('label');
    useInChpuLabel.textContent = 'Использовать в раскрое ЧПУ';
    const useInChpuCheckbox = document.createElement('input');
    useInChpuCheckbox.type = 'checkbox';
    useInChpuCheckbox.classList.add('check-box-chpu');

    fetchSortingPostingBy().then((sortings) => {
        sortings.forEach((sorting) => {
            const option = document.createElement('option');
            option.value = sorting;
            option.textContent = sorting;
            sortingDropdown.appendChild(option);
        });
    });

    const buttonGroup = document.createElement('div');
    buttonGroup.classList.add('button-group');

    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Отменить';
    cancelButton.classList.add('delete-button');
    cancelButton.type = 'button';
    cancelButton.addEventListener('click', () => modal.remove());

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.classList.add('edit-button');
    saveButton.type = 'submit';

    buttonGroup.appendChild(saveButton);
    buttonGroup.appendChild(cancelButton);

    const chpuFieldsContainer = document.createElement('div');
    chpuFieldsContainer.style.display = 'none';

    const chpuNameLabel = document.createElement('label');
    chpuNameLabel.textContent = 'Наименование материала ЧПУ';
    const chpuNameInput = document.createElement('input');
    chpuNameInput.type = 'text';
    chpuNameInput.classList.add('input-not-role');

    const chpuArticleLabel = document.createElement('label');
    chpuArticleLabel.textContent = 'Номер артикула ЧПУ';
    const chpuArticleInput = document.createElement('input');
    chpuArticleInput.type = 'text';
    chpuArticleInput.classList.add('input-not-role');

    chpuFieldsContainer.appendChild(chpuNameLabel);
    chpuFieldsContainer.appendChild(chpuNameInput);
    chpuFieldsContainer.appendChild(chpuArticleLabel);
    chpuFieldsContainer.appendChild(chpuArticleInput);

    useInChpuCheckbox.addEventListener('change', () => {
        chpuFieldsContainer.style.display = useInChpuCheckbox.checked ? 'block' : 'none';
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const selectedSorting = sortingDropdown.value;

        const newMaterial = {
            name: materialNameInput.value.trim(),
            separatorName: separatorNameInput.value || null,
            sortingPostingBy: selectedSorting,
            useInChpuTemplate: useInChpuCheckbox.checked,
            chpuMaterialName: chpuNameInput.value.trim() || null,
            chpuArticleNumber: chpuArticleInput.value.trim() || null,
            isOnlyPackaging: isOnlyPackagingCheckbox.checked
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/materials', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newMaterial),
            });

            if (!response.ok) {
                const data = await response.json();
                alert('Ошибка: ' + data.message);
            } else {
                alert('Материал успешно создан');
                modal.remove();
                fetchMaterials(true);
            }
        } catch (error) {
            console.error('Ошибка при создании материала:', error);
        }
    });

    form.appendChild(headerForm);
    form.appendChild(materialNameLabel);
    form.appendChild(materialNameInput);
    form.appendChild(separatorNameLabel);
    form.appendChild(separatorNameInput);
    form.appendChild(sortingNameLabel);
    form.appendChild(sortingDropdown);
    form.appendChild(useInChpuCheckbox);
    form.appendChild(useInChpuLabel);
    form.appendChild(isOnlyPackagingCheckbox);
    form.appendChild(isOnlyPackagingLabel);
    form.appendChild(chpuFieldsContainer);
    form.appendChild(buttonGroup);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}

// ---------------------- МОДАЛКА РЕДАКТИРОВАНИЯ ----------------------

export function openEditMaterialModal(material) {
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
    headerForm.textContent = 'Редактирование артикула';

    const materialNameLabel = document.createElement('label');
    materialNameLabel.textContent = 'Наименование';
    const materialNameInput = document.createElement('input');
    materialNameInput.type = 'text';
    materialNameInput.required = true;
    materialNameInput.value = material.name;
    materialNameInput.classList.add('input-not-role');

    const materialSeparatorNameLabel = document.createElement('label');
    materialSeparatorNameLabel.textContent = 'Текст-разделитель в отчете';
    const materialSeparatorNameInput = document.createElement('input');
    materialSeparatorNameInput.type = 'text';
    materialSeparatorNameInput.value = material.separatorName || '';
    materialSeparatorNameInput.classList.add('input-not-role');

    const sortingPostingByLabel = document.createElement('label');
    sortingPostingByLabel.textContent = 'Сортировка по';
    const sortingDropdown = document.createElement('select');
    sortingDropdown.classList.add('material-dropdown');
    sortingDropdown.name = 'material';
    sortingDropdown.required = true;

    const useInChpuLabel = document.createElement('label');
    useInChpuLabel.textContent = 'Использовать в раскрое ЧПУ';
    const useInChpuCheckbox = document.createElement('input');
    useInChpuCheckbox.type = 'checkbox';
    useInChpuCheckbox.classList.add('check-box-chpu');
    useInChpuCheckbox.checked = material.useInChpuTemplate;

    // новое поле isOnlyPackaging
    const isOnlyPackagingLabel = document.createElement('label');
    isOnlyPackagingLabel.textContent = 'Только упаковывается';
    const isOnlyPackagingCheckbox = document.createElement('input');
    isOnlyPackagingCheckbox.type = 'checkbox';
    isOnlyPackagingCheckbox.classList.add('check-box-chpu');
    isOnlyPackagingCheckbox.checked = material.isOnlyPackaging ?? false;

    fetchSortingPostingBy().then((sortings) => {
        sortings.forEach((sorting) => {
            const option = document.createElement('option');
            option.value = sorting;
            option.textContent = sorting;
            if (sorting === material.sortingPostingBy) option.selected = true;
            sortingDropdown.appendChild(option);
        });
    }).catch((error) => {
        console.error('Ошибка при загрузке сортировок:', error);
        alert('Не удалось загрузить список сортировок.');
    });

    const chpuFieldsContainer = document.createElement('div');
    chpuFieldsContainer.style.display = useInChpuCheckbox.checked ? 'block' : 'none';

    const chpuNameLabel = document.createElement('label');
    chpuNameLabel.textContent = 'Наименование материала ЧПУ';
    const chpuNameInput = document.createElement('input');
    chpuNameInput.type = 'text';
    chpuNameInput.value = material.chpuMaterialName || '';
    chpuNameInput.classList.add('input-not-role');

    const chpuArticleLabel = document.createElement('label');
    chpuArticleLabel.textContent = 'Номер артикула ЧПУ';
    const chpuArticleInput = document.createElement('input');
    chpuArticleInput.type = 'text';
    chpuArticleInput.value = material.chpuArticleNumber || '';
    chpuArticleInput.classList.add('input-not-role');

    chpuFieldsContainer.appendChild(chpuNameLabel);
    chpuFieldsContainer.appendChild(chpuNameInput);
    chpuFieldsContainer.appendChild(chpuArticleLabel);
    chpuFieldsContainer.appendChild(chpuArticleInput);

    useInChpuCheckbox.addEventListener('change', () => {
        chpuFieldsContainer.style.display = useInChpuCheckbox.checked ? 'block' : 'none';
    });

    const buttonGroup = document.createElement('div');
    buttonGroup.classList.add('button-group');

    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Отменить';
    cancelButton.classList.add('delete-button');
    cancelButton.type = 'button';
    cancelButton.addEventListener('click', () => modal.remove());

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.classList.add('edit-button');
    saveButton.type = 'submit';

    buttonGroup.appendChild(saveButton);
    buttonGroup.appendChild(cancelButton);

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const selectedSort = sortingDropdown.value;

        const updatedMaterial = {
            currentName: material.name,
            updatedName: materialNameInput.value === material.name ? null : materialNameInput.value.trim(),
            separatorName: materialSeparatorNameInput.value === '' ? null : materialSeparatorNameInput.value,
            sortingPostingBy: selectedSort,
            useInChpuTemplate: useInChpuCheckbox.checked,
            chpuMaterialName: chpuNameInput.value.trim() || null,
            chpuArticleNumber: chpuArticleInput.value.trim() || null,
            isOnlyPackaging: isOnlyPackagingCheckbox.checked
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/materials', {
                method: 'PUT',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updatedMaterial),
            });

            if (!response.ok) {
                const data = await response.json();
                alert('Ошибка: ' + data.message);
            } else {
                alert('Материал успешно обновлен');
                modal.remove();
                fetchMaterials(true);
            }
        } catch (error) {
            console.error('Ошибка при обновлении материала:', error);
        }
    });

    form.appendChild(headerForm);
    form.appendChild(materialNameLabel);
    form.appendChild(materialNameInput);
    form.appendChild(materialSeparatorNameLabel);
    form.appendChild(materialSeparatorNameInput);
    form.appendChild(sortingPostingByLabel);
    form.appendChild(sortingDropdown);
    form.appendChild(useInChpuCheckbox);
    form.appendChild(useInChpuLabel);
    form.appendChild(isOnlyPackagingCheckbox);
    form.appendChild(isOnlyPackagingLabel);
    form.appendChild(chpuFieldsContainer);
    form.appendChild(buttonGroup);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}