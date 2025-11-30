import {checkTokenExpirationAndGet} from "./panel.js";
import {findShopsByMarketplaceName} from "./reports-panel.js";


let currentPage = 0;
let pageSize = 20;
let totalPages = 0;
let isLoading = false;
let hasMore = true;
let lastSearchRequest = {};
let ordersCache = [];
let observer = null;

/* ------------------- ОТКРЫТИЕ ПАНЕЛИ ------------------- */
export async function openOrdersPanel() {
    const container = document.getElementById('main-container');
    container.innerHTML = '';

    // Заголовок + кнопка выгрузки
    const headerWrapper = document.createElement('div');
    headerWrapper.style.display = 'flex';
    headerWrapper.style.alignItems = 'center';
    headerWrapper.style.justifyContent = 'space-between';
    headerWrapper.style.marginBottom = '15px';

    const header = document.createElement('h3');
    header.textContent = 'Заказы';

    const exportButton = document.createElement('button');
    exportButton.textContent = 'Выгрузить отчёт';
    exportButton.classList.add('order-export-button');
    exportButton.disabled = true; // включим после поиска

    // --- Кнопка "Загрузить заказы"
    const importButton = document.createElement('button');
    importButton.textContent = 'Импортировать заказы';
    importButton.classList.add('import-orders-button');
    importButton.addEventListener('click', openImportOrdersModal);

    // --- Обёртка для кнопок
    const buttonsWrapper = document.createElement('div');
    buttonsWrapper.style.display = 'flex';
    buttonsWrapper.style.gap = '10px';
    buttonsWrapper.append(exportButton, importButton);

    headerWrapper.append(header, buttonsWrapper);
    container.appendChild(headerWrapper);

    // Фильтры
    const filtersContainer = await createFiltersSection(exportButton);
    container.appendChild(filtersContainer);

    // Таблица заказов
    const tableContainer = document.createElement('div');
    tableContainer.id = 'orders-table-container';
    container.appendChild(tableContainer);
}

/* ------------------- ФИЛЬТРЫ ------------------- */
async function createFiltersSection(exportButton) {
    const filters = document.createElement('div');
    filters.classList.add('order-filters-section');

    // --- INPUTS ---
    const numberInput = createInput('Номер заказа');
    const shopInput = createInput('Магазин');
    const lengthInput = createInput('Размер 1 меньше чем (мм)');
    const widthInput = createInput('Размер 2 меньше чем (мм)');
    const thicknessInput = createInput('Толщина равна (мм)');

    const fromDateInput = createDateInput('Дата начала обработки (от)');
    const toDateInput = createDateInput('Дата окончания обработки (до)');

    const token = checkTokenExpirationAndGet();
    const colors = await fetchJson('/api/v1/colors', token);
    const materials = await fetchJson('/api/v1/materials', token);
    const marketplaces = await fetchJson('/api/v1/shops/marketplaces', token);

    // --- SELECTS ---
    const colorMap = new Map(colors.map(c => [c.name, c.number]));
    const colorSelect = createSingleSelect('Все цвета', colors.map(c => c.name));

    const materialSelect = createSingleSelect('Все материалы', materials.map(m => m.name));
    const excludeMaterialSelect = createMultiSelect('Исключить материалы', materials.map(m => m.name));
    const statusSelect = createMultiSelect('Статусы', ['CANCELLED','CREATED','PILA','CHPU','KROMKA','UPAKOVKA','DONE','BRAK']);
    const marketplaceSelect = createMultiSelect('Маркетплейсы', marketplaces);

    // --- Ряды для CSS ---
    const inputsRow = document.createElement('div');
    inputsRow.classList.add('inputs-row');
    inputsRow.append(numberInput, shopInput, lengthInput, widthInput, thicknessInput);

    const dateRow = document.createElement('div');
    dateRow.classList.add('inputs-row');
    dateRow.append(fromDateInput, toDateInput);

    const selectsRow = document.createElement('div');
    selectsRow.classList.add('selects-row');
    selectsRow.append(
        colorSelect.element,
        materialSelect.element,
        excludeMaterialSelect.element,
        statusSelect.element,
        marketplaceSelect.element
    );

    // --- Кнопка поиска ---
    const searchButton = document.createElement('button');
    searchButton.textContent = 'Поиск';
    searchButton.classList.add('order-search-button');

    searchButton.addEventListener('click', () => {
        currentPage = 0;
        hasMore = true;
        ordersCache = [];
        if (observer) observer.disconnect();

        // Получаем значения из инпутов
        const fromRaw = fromDateInput.querySelector('input').value;
        const toRaw = toDateInput.querySelector('input').value;
        const fromInProcessAt = formatLocalDateTime(fromRaw) || null;
        const toInProcessAt = formatLocalDateTime(toRaw) || null;

        // получаем выбранное имя цвета и находим его номер
        const selectedColorName = colorSelect.choices.getValue(true);
        const colorNumber = selectedColorName ? colorMap.get(selectedColorName) : null;

        performSearch({
            number: numberInput.querySelector('input').value || null,
            shopName: shopInput.querySelector('input').value || null,
            length: lengthInput.querySelector('input').value || null,
            width: widthInput.querySelector('input').value || null,
            thickness: thicknessInput.querySelector('input').value || null,
            fromInProcessAt,
            toInProcessAt,
            colorNumber,
            materialName: materialSelect.choices.getValue(true) || null,
            excludeMaterialNames: excludeMaterialSelect.choices.getValue(true),
            statuses: statusSelect.choices.getValue(true),
            marketplaces: marketplaceSelect.choices.getValue(true)
        }, exportButton);
    });

    filters.append(inputsRow, dateRow, selectsRow, searchButton);

    exportButton.addEventListener('click', () => {
        exportOrders(lastSearchRequest, exportButton);
    });

    return filters;
}



/* ------------------- ВСПОМОГАТЕЛЬНЫЕ ------------------- */
async function fetchJson(url, token) {
    try {
        const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
        return await res.json();
    } catch (e) {
        console.error('Ошибка загрузки', url, e);
        return [];
    }
}


function createInput(placeholder) {
    const div = document.createElement('div');
    div.classList.add('filter-item');
    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = placeholder;
    div.appendChild(input);
    return div;
}

// ----------------- мульти селект -----------------
function createMultiSelect(placeholder, options = []) {
    // Новый контейнер
    const wrapper = document.createElement('div');
    wrapper.classList.add('select-wrapper');

    const select = document.createElement('select');
    select.multiple = true;

    options.forEach(optValue => {
        const opt = document.createElement('option');
        opt.value = optValue;
        opt.textContent = optValue;
        select.appendChild(opt);
    });

    wrapper.appendChild(select);

    const choices = new Choices(select, {
        removeItemButton: true,
        placeholder: true,          // отключаем "Press to select"
        placeholderValue: placeholder,
        itemSelectText: '',
        searchEnabled: true
    });

    return { element: wrapper, choices };
}

// ----------------- одиночный селект -----------------
function createSingleSelect(placeholder, options = [], defaultValue = '') {
    // Новый внешний контейнер для селектора
    const wrapper = document.createElement('div');
    wrapper.classList.add('select-wrapper'); // новый класс

    const select = document.createElement('select');

    // Пустой вариант (placeholder)
    const emptyOption = document.createElement('option');
    emptyOption.value = '';
    emptyOption.textContent = placeholder;
    select.appendChild(emptyOption);

    // Добавляем опции
    options.forEach(optValue => {
        const opt = document.createElement('option');
        opt.value = optValue;
        opt.textContent = optValue;
        if (optValue === defaultValue) {
            opt.selected = true;
        }
        select.appendChild(opt);
    });

    wrapper.appendChild(select);

    const choices = new Choices(select, {
        allowHTML: false,
        searchEnabled: true,
        removeItemButton: false,
        placeholder: true,
        itemSelectText: '',
        placeholderValue: placeholder
    });

    return { element: wrapper, choices };
}

/* ------------------- ПОИСК ------------------- */
async function performSearch(filters, exportButton) {
    const tableContainer = document.getElementById('orders-table-container');
    tableContainer.innerHTML = '';
    exportButton.disabled = true;

    try {
        const token = checkTokenExpirationAndGet();
        lastSearchRequest = { ...filters, page: 0, size: 20 };

        const response = await fetch('/api/v1/orders/search', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(lastSearchRequest)
        });

        const data = await response.json();
        ordersCache = Array.isArray(data?.content) ? data.content : [];

        renderOrdersTable(ordersCache);

        currentPage = data.number ?? 0;
        totalPages = data.totalPages ?? 0;
        hasMore = !data.last ?? (ordersCache.length === pageSize);

        exportButton.disabled = ordersCache.length === 0;
        if (ordersCache.length === 0) {
            tableContainer.textContent = 'Заказы не найдены';
            return;
        }

        setupScrollPagination(filters);
    } catch (err) {
        console.error('Ошибка при поиске заказов', err);
        alert('Не удалось загрузить заказы. Проверьте соединение или токен.');
    }
}

/* ------------------- ТАБЛИЦА ------------------- */
function renderOrdersTable(orders) {
    const container = document.getElementById('orders-table-container');
    container.innerHTML = '';

    if (!orders || orders.length === 0) {
        container.textContent = 'Заказы не найдены';
        return;
    }

    const table = document.createElement('table');
    table.classList.add('order-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Номер', 'Статус', 'Артикул', 'Маркетплейс', 'Дата', 'Материал', 'Цвет', 'Количество', 'Сумма'].forEach(h => {
        const th = document.createElement('th');
        th.textContent = h;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    orders.forEach(o => {
        const tr = createOrderRow(o);
        tbody.appendChild(tr);
    });
    table.appendChild(tbody);

    container.appendChild(table);
}

function createOrderRow(o) {
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>${escapeHtml(o.number)}</td>
        <td>${escapeHtml(o.status)}</td>
        <td>${escapeHtml(o.article)}</td>
        <td>${escapeHtml(o.marketplace)}</td>
        <td>${o.inProcessAt ? new Date(o.inProcessAt).toLocaleString() : '-'}</td>
        <td>${escapeHtml(o.materialName) || '-'}</td>
        <td>${escapeHtml(o.color) || '-'}</td>
        <td>${o.quantity ?? '-'}</td>
        <td>${o.totalPrice != null ? Number(o.totalPrice).toFixed(2) : '-'}</td>
    `;
    tr.addEventListener('click', () => openOrderModal(o));
    return tr;
}

function createDateInput(labelText) {
    const wrapper = document.createElement('div');
    wrapper.classList.add('filter-item');

    const input = document.createElement('input');
    input.type = 'text';
    input.placeholder = labelText;
    input.classList.add('date-input');
    wrapper.appendChild(input);

    flatpickr(input, {
        enableTime: true,
        time_24hr: true,
        dateFormat: "Y-m-d H:i",
        locale: "ru",
        allowInput: true,
        minuteIncrement: 1
    });

    return wrapper;
}

/* ------------------- ПАГИНАЦИЯ ------------------- */
function setupScrollPagination(filters) {
    const container = document.getElementById('orders-table-container');
    if (observer) observer.disconnect();

    const sentinel = document.createElement('div');
    sentinel.id = 'scroll-sentinel';
    container.appendChild(sentinel);

    observer = new IntersectionObserver(async entries => {
        if (entries[0].isIntersecting && !isLoading && hasMore) {
            await loadNextPage(filters);
        }
    }, { root: null, rootMargin: '200px', threshold: 0.1 });

    observer.observe(sentinel);
}

async function loadNextPage(filters) {
    if (!hasMore) return;
    isLoading = true;

    try {
        const token = checkTokenExpirationAndGet();
        const request = { ...filters, page: currentPage + 1, size: pageSize };

        const response = await fetch('/api/v1/orders/search', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(request)
        });

        const data = await response.json();
        const newContent = Array.isArray(data?.content) ? data.content : [];

        currentPage = data.number ?? currentPage + 1;
        totalPages = data.totalPages ?? totalPages;
        hasMore = !data.last ?? (newContent.length === pageSize);

        if (newContent.length > 0) {
            ordersCache.push(...newContent);
            appendOrdersToTable(newContent);
        } else {
            hasMore = false;
        }
    } catch (err) {
        console.error('Ошибка при подгрузке следующей страницы', err);
        hasMore = false;
    } finally {
        isLoading = false;
    }
}

function appendOrdersToTable(newOrders) {
    const tbody = document.querySelector('#orders-table-container tbody');
    newOrders.forEach(o => {
        const tr = createOrderRow(o);
        tbody.appendChild(tr);
    });
}

/* ------------------- ВЫГРУЗКА ------------------- */
async function exportOrders(filters, button) {
    const originalText = 'Выгрузить отчёт';
    button.disabled = true;
    button.textContent = '⏳ Генерация...';

    try {
        const token = checkTokenExpirationAndGet();
        const response = await fetch('/api/v1/reports/orders', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(filters)
        });

        if (response.status === 404) {
            alert('Заказы для выгрузки не найдены');
            return;
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        const disposition = response.headers.get('Content-Disposition');
        let filename = 'orders-report.xlsx';
        if (disposition) {
            const match = /filename\*=UTF-8''(.+)$/.exec(disposition) || /filename="(.+)"/.exec(disposition);
            if (match && match[1]) filename = decodeURIComponent(match[1]);
        }

        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);

    } catch (err) {
        console.error('Ошибка при экспорте отчёта', err);
        alert('Не удалось выгрузить отчёт');
    } finally {
        button.textContent = originalText;
        button.disabled = false;
    }
}

/* ------------------- МОДАЛКА ------------------- */
async function openOrderModal(order) {
    const modal = document.createElement('div');
    modal.classList.add('order-modal');

    const modalContent = document.createElement('div');
    modalContent.classList.add('order-modal-content');

    const closeBtn = document.createElement('span');
    closeBtn.classList.add('order-close-button');
    closeBtn.innerHTML = '&times;';
    closeBtn.onclick = () => modal.remove();

    const title = document.createElement('h3');
    title.textContent = `Заказ № ${order.number}`;

    const grid = document.createElement('div');
    grid.classList.add('order-details-grid');

    const fields = [
        ['Статус', order.status],
        ['Магазин', order.shopName],
        ['Маркетплейс', order.marketplace],
        ['Номер паллета', order.palletNumber ?? '-'],
        ['Артикул', order.article ?? '-'],
        ['Количество', order.quantity ?? '-'],
        ['Материал', order.materialName ?? '-'],
        ['Цвет', order.color ?? '-'],
        ['Длина (мм)', order.length ?? '-'],
        ['Ширина (мм)', order.width ?? '-'],
        ['Толщина (мм)', order.thickness ?? '-'],
        ['Площадь (м²)', order.areaInMeters ?? '-'],
        ['Цена за м²', order.pricePerSquareMeter ?? '-'],
        ['Общая сумма', order.totalPrice ?? '-'],
        ['Дата в обработке', order.inProcessAt ? new Date(order.inProcessAt).toLocaleString() : '-'],
        ['Комментарий', order.comment ?? '-'],
    ];

    fields.forEach(([label, value]) => {
        const item = document.createElement('div');
        item.classList.add('order-detail-item');
        item.innerHTML = `<span class="label">${escapeHtml(label)}:</span><span class="value">${escapeHtml(value)}</span>`;
        grid.appendChild(item);
    });

    // --- КНОПКА ИЗМЕНЕНИЯ СТАТУСА ---
    const changeStatusWrapper = document.createElement('div');
    changeStatusWrapper.classList.add('change-status-wrapper');

    const changeStatusBtn = document.createElement('button');
    changeStatusBtn.classList.add('change-status-btn');
    changeStatusBtn.textContent = 'Изменить статус';

    changeStatusWrapper.appendChild(changeStatusBtn);

    const statusEditor = document.createElement('div');
    statusEditor.classList.add('status-editor');

    statusEditor.innerHTML = `
    <label>Новый статус:</label>
    <select id="statusSelect" class="status-select">
        <option value="CANCELLED">CANCELLED</option>
        <option value="CREATED">CREATED</option>
        <option value="PILA">PILA</option>
        <option value="CHPU">CHPU</option>
        <option value="KROMKA">KROMKA</option>
        <option value="UPAKOVKA">UPAKOVKA</option>
        <option value="DONE">DONE</option>
    </select>
    <button class="save-status-btn">Сохранить</button>
`;

    changeStatusBtn.onclick = () => {
        statusEditor.classList.toggle('open');
    };

    const saveBtn = statusEditor.querySelector('.save-status-btn');
    saveBtn.onclick = async () => {
        const newStatus = statusEditor.querySelector('.status-select').value;
        const token = checkTokenExpirationAndGet();

        try {
            const response = await fetch(`/api/v1/orders/status`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    number: order.number,
                    newStatus: newStatus
                })
            });

            if (!response.ok) {
                let msg = "Неизвестная ошибка";

                try {
                    const errorJson = await response.json();
                    msg = errorJson.message || JSON.stringify(errorJson);
                } catch (_) {
                    msg = await response.text(); // Если это не JSON
                }

                alert("Ошибка обновления статуса: " + msg);
                return;
            }

            // Успех ↓
            alert("Статус обновлён!");
            statusEditor.classList.remove('open');

        } catch (e) {
            console.error(e);
            alert("Ошибка сети: " + e.message);
        }
    };

    // Контейнер для истории
    const historySection = document.createElement('div');
    historySection.classList.add('order-history-section');
    historySection.innerHTML = `<h4>История изменений</h4><div class="order-history-loader">Загрузка...</div>`;

    modalContent.append(closeBtn, title, grid, changeStatusWrapper, statusEditor, historySection);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.remove();
    });

    // ---- ЗАПРОС истории ----
    try {
        const token = checkTokenExpirationAndGet();
        const response = await fetch(`/api/v1/orders/${order.number}/history`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Ошибка при загрузке истории');
        }

        const history = await response.json();

        const historyList = document.createElement('div');
        historyList.classList.add('order-history-list');

        if (history.length === 0) {
            historyList.innerHTML = `<div class="order-history-empty">История отсутствует</div>`;
        } else {
            history.forEach(item => {
                const entry = document.createElement('div');
                entry.classList.add('order-history-item');
                entry.innerHTML = `
    <div class="order-history-row">
        <div class="field">
            <div class="label">Статус</div>
            <div class="value status">${escapeHtml(item.status)}</div>
        </div>
        <div class="field">
            <div class="label">Дата</div>
            <div class="value date">${new Date(item.createdAt).toLocaleString()}</div>
        </div>
        <div class="field">
            <div class="label">Автор</div>
            <div class="value author">${escapeHtml(item.author ?? '-')}</div>
        </div>
        <div class="field">
            <div class="label">Рабочее место</div>
            <div class="value workplace">${escapeHtml(item.workplace ?? '-')}</div>
        </div>
    </div>
`;
                historyList.appendChild(entry);
            });
        }

        historySection.innerHTML = `<h4>История изменений</h4>`;
        historySection.appendChild(historyList);
    } catch (error) {
        historySection.innerHTML = `
            <h4>История изменений</h4>
            <div class="order-history-error">Ошибка при загрузке истории</div>
        `;
        console.error(error);
    }
}


/* ------------------- ВСПОМОГАТЕЛЬНЫЕ ------------------- */
function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function formatLocalDateTime(value) {
    if (!value) return null;
    const date = new Date(value);
    const pad = (n) => String(n).padStart(2, '0');

    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());
    const seconds = pad(date.getSeconds());

    // формат строго "YYYY-MM-DDTHH:mm:ss"
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}

/* ------------------- КНОПКА "ЗАГРУЗИТЬ ЗАКАЗЫ" ------------------- */
function addImportOrdersButton() {
    const container = document.getElementById('main-container');
    const existing = document.getElementById('import-orders-button');
    if (existing) return; // если уже есть

    const button = document.createElement('button');
    button.id = 'import-orders-button';
    button.textContent = 'Импортировать заказы';
    button.classList.add('import-orders-button');
    button.style.marginBottom = '15px';

    button.addEventListener('click', openImportOrdersModal);
    container.insertBefore(button, document.getElementById('orders-table-container'));
}


/* ------------------- МОДАЛКА "ЗАГРУЗИТЬ ЗАКАЗЫ" ------------------- */
async function openImportOrdersModal() {
    const modal = document.createElement('div');
    modal.classList.add('order-modal');

    const modalContent = document.createElement('div');
    modalContent.classList.add('order-modal-content');

    const closeBtn = document.createElement('span');
    closeBtn.classList.add('order-close-button');
    closeBtn.innerHTML = '&times;';
    closeBtn.onclick = () => modal.remove();

    const title = document.createElement('h3');
    title.textContent = 'Импорт заказов с маркетплейсов';

    const supplyContainer = document.createElement('div');
    supplyContainer.className = 'supply-container';

    // кнопка "+ добавить WB поставку"
    const addSupplyButton = document.createElement('button');
    addSupplyButton.textContent = '+ добавить WB поставку';
    addSupplyButton.className = 'add-supply-button';

    // ⚙️ Новая кнопка "Добавить дату начала периода для импорта"
    const addDateButton = document.createElement('button');
    addDateButton.textContent = '📅 Добавить дату начала периода для импорта';
    addDateButton.className = 'add-supply-button'; // тот же стиль


    const dateInput = document.createElement('input');
    dateInput.type = 'text';
    dateInput.placeholder = 'Выберите дату начала периода';
    dateInput.classList.add('input-not-role');
    dateInput.style.display = 'none';

    // Flatpickr для выбора даты
    flatpickr(dateInput, {
        enableTime: true,
        time_24hr: true,
        dateFormat: "Y-m-d H:i",
        locale: "ru",
        allowInput: true,
        minuteIncrement: 1
    });

    addDateButton.addEventListener('click', () => {
        dateInput.style.display = dateInput.style.display === 'none' ? 'block' : 'none';
    });

    function addSupplyRow() {
        const supplyRow = document.createElement('div');
        supplyRow.className = 'supply-row';

        const qrInput = document.createElement('input');
        qrInput.type = 'text';
        qrInput.placeholder = 'QR-код поставки';
        qrInput.className = 'input-not-role';
        qrInput.required = true;

        const storeSelect = document.createElement('select');
        storeSelect.className = 'input-not-role';

        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = 'Выберите магазин';
        storeSelect.appendChild(defaultOption);

        findShopsByMarketplaceName('WILDBERRIES')
            .then(shops => {
                if (shops) {
                    shops.forEach(shop => {
                        const option = document.createElement('option');
                        option.textContent = shop.name;
                        storeSelect.appendChild(option);
                    });
                }
            });

        const removeButton = document.createElement('button');
        removeButton.className = 'remove-supply-button';
        removeButton.textContent = '×';
        removeButton.addEventListener('click', () => supplyRow.remove());

        supplyRow.append(qrInput, storeSelect, removeButton);
        supplyContainer.appendChild(supplyRow);
    }

    addSupplyButton.addEventListener('click', addSupplyRow);

    // кнопка "Импортировать заказы"
    const loadButton = document.createElement('button');
    loadButton.textContent = 'Импортировать заказы';
    loadButton.className = 'load-orders-button';

    const loader = document.createElement('div');
    loader.classList.add('loader');
    loader.style.display = 'none';

    const message = document.createElement('div');
    message.classList.add('load-message');

    loadButton.addEventListener('click', async () => {
        const rows = supplyContainer.querySelectorAll('.supply-row');
        const wbSupplyDetails = [];
        for (const row of rows) {
            const qr = row.querySelector('input').value.trim();
            const shop = row.querySelector('select').value.trim();
            if (!qr || !shop) {
                alert('Заполните все поля в каждой строке');
                return;
            }
            wbSupplyDetails.push({ supplyId: qr, shopName: shop });
        }

        // 🕓 Обработка даты (если задана)
        let fromInstant = null;
        const rawDate = dateInput.value.trim();
        if (rawDate) {
            const localDate = new Date(rawDate);
            fromInstant = localDate.toISOString(); // UTC формат
        }

        loader.style.display = 'block';
        loadButton.disabled = true;
        message.textContent = '';

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/orders/import', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    wbSupplyDetails,
                    from: fromInstant
                })
            });

            loader.style.display = 'none';
            loadButton.disabled = false;

            if (response.ok) {
                message.textContent = '✅ Заказы успешно загружены!';
                message.style.color = 'green';

                setTimeout(() => {
                    modal.remove();
                    if (lastSearchRequest && Object.keys(lastSearchRequest).length > 0) {
                        performSearch(lastSearchRequest, document.querySelector('.order-export-button'));
                    }
                }, 2000);
            } else {
                const errText = await response.text();
                message.textContent = '❌ Ошибка при загрузке заказов: ' + errText;
                message.style.color = 'red';
            }

        } catch (err) {
            console.error('Ошибка при загрузке заказов', err);
            loader.style.display = 'none';
            loadButton.disabled = false;
            message.textContent = '❌ Не удалось загрузить заказы';
            message.style.color = 'red';
        }
    });

    modalContent.append(
        closeBtn,
        title,
        addDateButton, // 📅 новая кнопка
        dateInput, // контейнер с полем выбора даты
        addSupplyButton,
        supplyContainer,
        loadButton,
        loader,
        message
    );

    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.remove();
    });
}

