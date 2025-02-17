import {checkTokenExpirationAndGet} from "./panel.js";

export async function fetchShops() {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/shops', {
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
        const shops = await response.json();
        renderShopsTable(shops);
    } catch (error) {
        console.error('Ошибка при получении магазинов:', error);
    }
}

export async function fetchMarketplaces() {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/shops/marketplaces', {
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
        console.error('Ошибка при получении маркетплейсов:', error);
        return [];
    }
}

export async function fetchEditShop(id, updatedData) {
    const token = checkTokenExpirationAndGet();
    try {

        const response = await fetch('/api/v1/shops', {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                id: id,
                name: updatedData.name,
                palletNumber: updatedData.palletNumber,
                marketplace: updatedData.marketplace,
                apiKey: updatedData.apiKey === 'undefined' ? null : updatedData.apiKey,
                clientId: updatedData.clientId === 'undefined' ? null : updatedData.clientId
            })
        });
        if (!response.ok) {
            const data = await response.json();
            alert('Ошибка: ' + data.message);
        } else {
            fetchShops(); // Перезагружаем список магазинов после обновления
        }
    } catch (error) {
        console.error('Ошибка при обновлении магазина:', error);
        alert('Не удалось обновить магазин');
    }
}

export async function fetchDeleteShop(id) {
    const token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/shops', {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({id})
        });
        if (!response.ok) {
            const data = response.json
            alert('Ошибка: ' + data.message);
        } else {
            alert('Магазин успешно удален');
            fetchShops(); // Перезагружаем список магазинов после удаления
        }
    } catch (error) {
        console.error('Ошибка при удалении магазина:', error);
        alert('Не удалось удалить пользователя');
    }
}

export function renderShopsTable(shops) {
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
    header.textContent = 'Действующие магазины';
    headerContainer.appendChild(header);

    const createButton = document.createElement('button');
    createButton.textContent = 'Создать';
    createButton.classList.add('edit-button');
    createButton.addEventListener('click', openCreateShopModal);
    headerContainer.appendChild(createButton);

    container.appendChild(headerContainer);

    // Создаем таблицу
    const table = document.createElement('table');
    table.classList.add('shop-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Наименование', 'Маркетплейс', 'Номер паллета', 'Действия'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);
    const tbody = document.createElement('tbody');
    shops.forEach(shop => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${shop.name}</td>
            <td>${shop.marketplace}</td>
            <td>${shop.palletNumber}</td>
            <td>
                <button class="edit-button">Изменить</button>
                <button class="delete-button">Удалить</button>
            </td>
        `;
        tbody.appendChild(row);

        const editButton = row.querySelector('.edit-button');
        editButton.addEventListener('click', () => openEditShopModal(shop));

        const deleteButton = row.querySelector('.delete-button');
        deleteButton.addEventListener('click', () => fetchDeleteShop(shop.id));
    });
    table.appendChild(tbody);

    container.appendChild(table);
}

export function openCreateShopModal() {
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
    headerForm.textContent = 'Создание магазина';

    const shopNameLabel = document.createElement('label');
    shopNameLabel.textContent = 'Наименование';
    const shopNameInput = document.createElement('input');
    shopNameInput.type = 'text';
    shopNameInput.required = true;
    shopNameInput.classList.add('input-not-role');

    const palletNumberLabel = document.createElement('label');
    palletNumberLabel.textContent = 'Номер паллета';
    const palletNumberInput = document.createElement('input');
    palletNumberInput.type = 'number';
    palletNumberInput.required = true;
    palletNumberInput.classList.add('input-not-role');

    const marketPlaceLabel = document.createElement('label');
    marketPlaceLabel.textContent = 'Маркетплейс';
    const marketplaceContainer = document.createElement('div');
    marketplaceContainer.classList.add('checkbox-group'); // Добавляем класс для группы чекбоксов

    const apiKeyLabel = document.createElement('label');
    apiKeyLabel.textContent = 'API-ключ';
    const apiKeyInput = document.createElement('input');
    apiKeyInput.type = 'password';
    apiKeyInput.required = true;
    apiKeyInput.classList.add('input-not-role');

    const clientIdLabel = document.createElement('label');
    clientIdLabel.textContent = 'Client ID (только для OZON)';
    const clientIdInput = document.createElement('input');
    clientIdInput.type = 'password';
    clientIdInput.required = false;
    clientIdInput.classList.add('input-not-role');


    fetchMarketplaces().then((marketplaces) => {
        marketplaces.forEach((marketplace) => {
            const radio = document.createElement('input');
            radio.type = 'radio'; // Изменяем тип на radio
            radio.name = 'marketplace'; // Указываем одно имя для группы радиокнопок
            radio.id = `marketplace-${marketplace}`;
            radio.value = marketplace;

            const label = document.createElement('label');
            label.htmlFor = `marketplace-${marketplace}`;
            label.textContent = marketplace;
            label.classList.add('checkbox-label'); // Оставляем класс, если есть стили

            const marketplaceItem = document.createElement('div');
            marketplaceItem.classList.add('marketplace-item');
            marketplaceItem.appendChild(radio);
            marketplaceItem.appendChild(label);

            marketplaceContainer.appendChild(marketplaceItem);
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

        const selectedMarketplace = marketplaceContainer.querySelector('input[type="radio"]:checked')?.value;

        if (!selectedMarketplace) {
            alert('Пожалуйста, выберите маркетплейс.');
            return;
        }

        const newShop = {
            name: shopNameInput.value.trim(),
            palletNumber: palletNumberInput.value,
            apiKey: apiKeyInput.value === 'undefined' ? null : apiKeyInput.value,
            clientId: clientIdInput.value === 'undefined' ? null : clientIdInput.value,
            marketplace: selectedMarketplace, // Передаем выбранный маркетплейс
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/shops', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newShop),
            });

            if (!response.ok) {
                const data = await response.json();
                alert('Ошибка: ' + data.message);
            } else {
                alert('Магазин успешно создан');
                modal.remove();
                fetchShops();
            }
        } catch (error) {
            console.error('Ошибка при создании пользователя:', error);
            alert('Не удалось создать пользователя');
        }
    });

    form.appendChild(headerForm);
    form.appendChild(shopNameLabel);
    form.appendChild(shopNameInput);
    form.appendChild(palletNumberLabel);
    form.appendChild(palletNumberInput);
    form.appendChild(apiKeyLabel);
    form.appendChild(apiKeyInput);
    form.appendChild(clientIdLabel);
    form.appendChild(clientIdInput);
    form.appendChild(marketPlaceLabel);
    form.appendChild(marketplaceContainer);
    form.appendChild(saveButton);
    form.appendChild(cancelButton);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}


export function openEditShopModal(shop) {
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
    headerForm.textContent = 'Редактирование магазина';

    const shopNameLabel = document.createElement('label');
    shopNameLabel.textContent = 'Наименование';
    const shopNameInput = document.createElement('input');
    shopNameInput.type = 'text';
    shopNameInput.required = true;
    shopNameInput.value = shop.name;
    shopNameInput.classList.add('input-not-role');

    const palletNumberLabel = document.createElement('label');
    palletNumberLabel.textContent = 'Номер паллета';
    const palletNumberInput = document.createElement('input');
    palletNumberInput.type = 'number';
    palletNumberInput.required = true;
    palletNumberInput.value = shop.palletNumber;
    palletNumberInput.classList.add('input-not-role');

    const marketPlaceLabel = document.createElement('label');
    marketPlaceLabel.textContent = 'Маркетплейс';
    const marketplaceContainer = document.createElement('div');
    marketplaceContainer.classList.add('checkbox-group'); // Добавляем класс для группы чекбоксов

    const apiKeyLabel = document.createElement('label');
    apiKeyLabel.textContent = 'API-ключ';
    const apiKeyInput = document.createElement('input');
    apiKeyInput.type = 'password';
    apiKeyInput.required = true;
    apiKeyInput.value = shop.apiKey;
    apiKeyInput.classList.add('input-not-role');

    const clientIdLabel = document.createElement('label');
    clientIdLabel.textContent = 'Client ID';
    const clientIdInput = document.createElement('input');
    clientIdInput.type = 'password';
    clientIdInput.required = true;
    clientIdInput.value = shop.clientId;
    clientIdInput.classList.add('input-not-role');

    fetchMarketplaces().then((marketplaces) => {
        marketplaces.forEach((marketplace) => {
            const radio = document.createElement('input');
            radio.type = 'radio'; // Изменяем тип на radio
            radio.name = 'marketplace'; // Указываем одно имя для группы радиокнопок
            radio.id = `marketplace-${marketplace}`;
            radio.value = marketplace;
            if (marketplace === shop.marketplace) {
                radio.checked = 'true';
            }

            const label = document.createElement('label');
            label.htmlFor = `marketplace-${marketplace}`;
            label.textContent = marketplace;
            label.classList.add('checkbox-label'); // Оставляем класс, если есть стили

            const marketplaceItem = document.createElement('div');
            marketplaceItem.classList.add('marketplace-item');
            marketplaceItem.appendChild(radio);
            marketplaceItem.appendChild(label);

            marketplaceContainer.appendChild(marketplaceItem);
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

    form.addEventListener('submit', (event) => {
        event.preventDefault();

        const selectedMarketplace = marketplaceContainer.querySelector('input[type="radio"]:checked')?.value;

        if (!selectedMarketplace) {
            alert('Пожалуйста, выберите маркетплейс.');
            return;
        }

        const updatedData = {
            name: shopNameInput.value.trim(),
            palletNumber: palletNumberInput.value,
            apiKey: apiKeyInput.value,
            clientId: clientIdInput.value,
            marketplace: selectedMarketplace, // Передаем выбранный маркетплейс
        };

        console.log(updatedData)
        fetchEditShop(shop.id, updatedData);
        modal.remove();
    });

    form.appendChild(headerForm);
    form.appendChild(shopNameLabel);
    form.appendChild(shopNameInput);
    form.appendChild(palletNumberLabel);
    form.appendChild(palletNumberInput);
    form.appendChild(apiKeyLabel);
    form.appendChild(apiKeyInput);
    form.appendChild(clientIdLabel);
    form.appendChild(clientIdInput);
    form.appendChild(marketPlaceLabel);
    form.appendChild(marketplaceContainer);
    form.appendChild(saveButton);
    form.appendChild(cancelButton);

    modalContent.appendChild(closeButton);
    modalContent.appendChild(form);
    modal.appendChild(modalContent);

    document.body.appendChild(modal);
}

