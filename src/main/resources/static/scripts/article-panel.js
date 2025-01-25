import {checkTokenExpirationAndGet} from "./panel.js";
import {fetchMaterials} from "./material-panel.js";

export async function fetchArticles() {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/articles', {
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
        const articles = await response.json();
        renderArticlesTable(articles);
    } catch (error) {
        console.error('Ошибка при получении артикулов:', error);
    }
}

export async function fetchDeleteArticle(name) {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/articles', {
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
            alert('Артикул успешно удален');
            fetchArticles();
        }
    } catch (error) {
        console.error('Ошибка при удалении артикулов:', error);
    }
}

export function renderArticlesTable(articles) {
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
    header.textContent = 'Информация по артикулам';
    headerContainer.appendChild(header);

    const createButton = document.createElement('button');
    createButton.textContent = 'Создать';
    createButton.classList.add('edit-button');
    createButton.addEventListener('click', openCreateArticleModal);
    headerContainer.appendChild(createButton);

    container.appendChild(headerContainer);

    // Создаем таблицу
    const table = document.createElement('table');
    table.classList.add('shop-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Наименование', 'Тип', 'Количество', 'Наименование материала', 'Действия'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);
    const tbody = document.createElement('tbody');
    articles.forEach(article => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${article.name}</td>
            <td>${article.type}</td>
            <td>${article.quantityPerSku}</td>
            <td>${article.materialName}</td>
            <td>
                <button class="edit-button">Изменить</button>
                <button class="delete-button">Удалить</button>
            </td>
        `;
        tbody.appendChild(row);

        const editButton = row.querySelector('.edit-button');
        editButton.addEventListener('click', () => openEditArticleModal(article));

        const deleteButton = row.querySelector('.delete-button');
        deleteButton.addEventListener('click', () => fetchDeleteArticle(article.name));
    });
    table.appendChild(tbody);

    container.appendChild(table);
}

export function openCreateArticleModal() {
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

    const articleNameLabel = document.createElement('label');
    articleNameLabel.textContent = 'Наименование';
    const articleNameInput = document.createElement('input');
    articleNameInput.type = 'text';
    articleNameInput.required = true;
    articleNameInput.classList.add('input-not-role');

    const articleTypeLabel = document.createElement('label');
    articleTypeLabel.textContent = 'Тип';
    const articleTypeInput = document.createElement('input');
    articleTypeInput.type = 'text';
    articleTypeInput.required = true;
    articleTypeInput.classList.add('input-not-role');

    const quantityPerSkuLabel = document.createElement('label');
    quantityPerSkuLabel.textContent = 'Количество товара за SKU';
    const quantityPerSkuInput = document.createElement('input');
    quantityPerSkuInput.type = 'number';
    quantityPerSkuInput.required = true;
    quantityPerSkuInput.classList.add('input-not-role');

    const materialNameLabel = document.createElement('label');
    materialNameLabel.textContent = 'Материал';
    const materialDropdown = document.createElement('select');
    materialDropdown.classList.add('material-dropdown'); // Класс для стилей
    materialDropdown.name = 'material';
    materialDropdown.required = true;

// Заполняем выпадающий список материалами
    fetchMaterials(false).then((materials) => {
        materials.forEach((material) => {
            const option = document.createElement('option');
            option.value = material.name;
            option.textContent = material.name; // Отображаемое имя материала
            materialDropdown.appendChild(option);
        });
    }).catch((error) => {
        console.error('Ошибка при загрузке материалов:', error);
        alert('Не удалось загрузить список материалов.');
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

        const selectedMaterial = materialDropdown.value; // Получаем выбранный материал

        if (!selectedMaterial) {
            alert('Пожалуйста, выберите материал.');
            return;
        }

        const newArticle = {
            name: articleNameInput.value.trim(),
            type: articleTypeInput.value,
            quantityPerSku: quantityPerSkuInput.value,
            materialName: selectedMaterial
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/articles', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newArticle),
            });

            if (!response.ok) {
                const data = await response.json();
                alert('Ошибка: ' + data.message);
            } else {
                alert('Артикул успешно создан');
                modal.remove();
                fetchArticles();
            }
        } catch (error) {
            console.error('Ошибка при создании пользователя:', error);
            alert('Не удалось создать пользователя');
        }
    });

    form.appendChild(headerForm);
    form.appendChild(articleNameLabel);
    form.appendChild(articleNameInput);
    form.appendChild(articleTypeLabel);
    form.appendChild(articleTypeInput);
    form.appendChild(quantityPerSkuLabel);
    form.appendChild(quantityPerSkuInput);
    form.appendChild(materialNameLabel);
    form.appendChild(materialDropdown);
    form.appendChild(saveButton);
    form.appendChild(cancelButton);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}

export function openEditArticleModal(article) {
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

    const articleNameLabel = document.createElement('label');
    articleNameLabel.textContent = 'Наименование';
    const articleNameInput = document.createElement('input');
    articleNameInput.type = 'text';
    articleNameInput.required = true;
    articleNameInput.value = article.name
    articleNameInput.classList.add('input-not-role');

    const articleTypeLabel = document.createElement('label');
    articleTypeLabel.textContent = 'Тип';
    const articleTypeInput = document.createElement('input');
    articleTypeInput.type = 'text';
    articleTypeInput.required = true;
    articleTypeInput.value = article.type
    articleTypeInput.classList.add('input-not-role');

    const quantityPerSkuLabel = document.createElement('label');
    quantityPerSkuLabel.textContent = 'Количество товара за SKU';
    const quantityPerSkuInput = document.createElement('input');
    quantityPerSkuInput.type = 'number';
    quantityPerSkuInput.required = true;
    quantityPerSkuInput.value = article.quantityPerSku
    quantityPerSkuInput.classList.add('input-not-role');

    const materialNameLabel = document.createElement('label');
    materialNameLabel.textContent = 'Материал';
    const materialDropdown = document.createElement('select');
    materialDropdown.classList.add('material-dropdown'); // Класс для стилей
    materialDropdown.name = 'material';
    materialDropdown.required = true;

    // Заполняем выпадающий список материалами
    fetchMaterials(false).then((materials) => {
        materials.forEach((material) => {
            const option = document.createElement('option');
            option.value = material.name;
            option.textContent = material.name;
            if (material.name === article.materialName) {
                option.selected = true;
            }
            materialDropdown.appendChild(option);
        });
    }).catch((error) => {
        console.error('Ошибка при загрузке материалов:', error);
        alert('Не удалось загрузить список материалов.');
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

        const selectedMaterial = materialDropdown.value; // Получаем выбранный материал

        if (!selectedMaterial) {
            alert('Пожалуйста, выберите материал.');
            return;
        }

        const updatedArticle = {
            currentName: article.name,
            updatedName: articleNameInput.value === article.name ? null :articleNameInput.value.trim(),
            type: articleTypeInput.value,
            quantityPerSku: quantityPerSkuInput.value,
            materialName: selectedMaterial
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/articles', {
                method: 'PUT',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(updatedArticle),
            });

            if (!response.ok) {
                const data = await response.json();
                alert('Ошибка: ' + data.message);
            } else {
                alert('Артикул успешно обновлен');
                modal.remove();
                fetchArticles();
            }
        } catch (error) {
            console.error('Ошибка при обновлении артикула:', error);
        }
    });

    form.appendChild(headerForm);
    form.appendChild(articleNameLabel);
    form.appendChild(articleNameInput);
    form.appendChild(articleTypeLabel);
    form.appendChild(articleTypeInput);
    form.appendChild(quantityPerSkuLabel);
    form.appendChild(quantityPerSkuInput);
    form.appendChild(materialNameLabel);
    form.appendChild(materialDropdown);
    form.appendChild(saveButton);
    form.appendChild(cancelButton);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}