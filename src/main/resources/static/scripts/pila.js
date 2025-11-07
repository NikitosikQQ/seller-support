import {checkTokenExpirationAndGet} from "./panel.js";

let currentPage = 0;
let pageSize = 20;
let totalPages = 0;
let isLoading = false;
let hasMore = true;
let lastSearchRequest = {};
let pilaCache = [];
let observer = null;

/* ------------------- ОТКРЫТИЕ ПАНЕЛИ ------------------- */
export async function openPilaPanel() {
    const container = document.getElementById('main-container');
    container.innerHTML = '';

    // Заголовок + кнопка "Обновить список"
    const headerWrapper = document.createElement('div');
    headerWrapper.style.display = 'flex';
    headerWrapper.style.alignItems = 'center';
    headerWrapper.style.justifyContent = 'space-between';
    headerWrapper.style.marginBottom = '15px';

    const header = document.createElement('h3');
    header.textContent = 'ПИЛА';

    // 🔄 Кнопка "Обновить список"
    const refreshButton = document.createElement('button');
    refreshButton.textContent = '🔄 Обновить список';
    refreshButton.classList.add('pila-search-button');
    refreshButton.addEventListener('click', () => {
        currentPage = 0;
        hasMore = true;
        pilaCache = [];
        if (observer) observer.disconnect();

        // Просто заново выполняем поиск
        performSearch(lastSearchRequest);
    });

    headerWrapper.append(header, refreshButton);
    container.appendChild(headerWrapper);

    // Фильтры
    const filtersContainer = await createFiltersSection();
    container.appendChild(filtersContainer);

    // Таблица
    const tableContainer = document.createElement('div');
    tableContainer.id = 'pila-table-container';
    container.appendChild(tableContainer);

    // Модальное окно для QR
    createQrModal();
}

/* ------------------- ФИЛЬТРЫ ------------------- */
async function createFiltersSection() {
    const filters = document.createElement('div');
    filters.classList.add('order-filters-section');

    const lengthInput = createInput('Размер 1 (мм)');
    const widthInput = createInput('Размер 2 (мм)');
    const thicknessInput = createInput('Толщина (мм)');

    const token = checkTokenExpirationAndGet();
    const colors = await fetchJson('/api/v1/colors', token);
    const materials = await fetchJson('/api/v1/materials', token);

    const packagingMaterials = materials
        .filter(m => m.isOnlyPackaging)
        .map(m => m.name);

    const availableMaterials = materials.filter(m => !m.isOnlyPackaging);

    const colorMap = new Map(colors.map(c => [c.name, c.number]));
    const colorSelect = createSingleSelect('Все цвета', colors.map(c => c.name));

    const materialSelect = createSingleSelect(
        'Все материалы',
        availableMaterials.map(m => m.name)
    );

    const inputsRow = document.createElement('div');
    inputsRow.classList.add('inputs-row');
    inputsRow.append(lengthInput, widthInput, thicknessInput);

    const selectsRow = document.createElement('div');
    selectsRow.classList.add('selects-row');
    selectsRow.append(materialSelect.element, colorSelect.element);

    const searchButton = document.createElement('button');
    searchButton.textContent = 'Поиск';
    searchButton.classList.add('order-search-button');

    searchButton.addEventListener('click', () => {
        currentPage = 0;
        hasMore = true;
        pilaCache = [];
        if (observer) observer.disconnect();

        const selectedColorName = colorSelect.choices.getValue(true);
        const colorNumber = selectedColorName ? colorMap.get(selectedColorName) : null;

        performSearch({
            length: lengthInput.querySelector('input').value || null,
            width: widthInput.querySelector('input').value || null,
            thickness: thicknessInput.querySelector('input').value || null,
            materialName: materialSelect.choices.getValue(true) || null,
            colorNumber,
            excludeMaterialNames: packagingMaterials,
            statuses: ['CREATED'],
            sortingType: 'Пила'
        });
    });

    filters.append(inputsRow, selectsRow, searchButton);
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

function createSingleSelect(placeholder, options = [], defaultValue = '') {
    const wrapper = document.createElement('div');
    wrapper.classList.add('select-wrapper');
    const select = document.createElement('select');

    const emptyOption = document.createElement('option');
    emptyOption.value = '';
    emptyOption.textContent = placeholder;
    select.appendChild(emptyOption);

    options.forEach(optValue => {
        const opt = document.createElement('option');
        opt.value = optValue;
        opt.textContent = optValue;
        if (optValue === defaultValue) opt.selected = true;
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
async function performSearch(filters) {
    const tableContainer = document.getElementById('pila-table-container');
    tableContainer.innerHTML = '';

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
        pilaCache = Array.isArray(data?.content) ? data.content : [];

        renderPilaTable(pilaCache);

        currentPage = data.number ?? 0;
        totalPages = data.totalPages ?? 0;
        hasMore = !data.last ?? (pilaCache.length === pageSize);

        if (pilaCache.length === 0) {
            tableContainer.textContent = 'Данные не найдены';
            return;
        }

        setupScrollPagination(filters);
    } catch (err) {
        console.error('Ошибка при поиске ПИЛА', err);
        alert('Не удалось загрузить данные.');
    }
}

/* ------------------- ТАБЛИЦА ------------------- */
function renderPilaTable(items) {
    const container = document.getElementById('pila-table-container');
    container.innerHTML = '';

    if (!items || items.length === 0) {
        container.textContent = 'Нет заказов';
        return;
    }

    const table = document.createElement('table');
    table.classList.add('order-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['№ заказа', 'Полный артикул', 'Длина', 'Ширина', 'Количество', 'QR'].forEach(h => {
        const th = document.createElement('th');
        th.textContent = h;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    items.forEach(o => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${escapeHtml(o.number)}</td>
            <td>${escapeHtml(o.article ?? '-')}</td>
            <td>${o.length ?? '-'}</td>
            <td>${o.width ?? '-'}</td>
            <td>${o.quantity ?? '-'}</td>
            <td class="qr-cell" id="qr-${o.number}"></td>
        `;
        tbody.appendChild(tr);

        // Генерация маленького QR
        setTimeout(() => {
            const qrContainer = document.getElementById(`qr-${o.number}`);
            if (qrContainer) {
                new QRCode(qrContainer, {
                    text: String(o.number),
                    width: 128,
                    height: 128
                });
            }
        }, 0);

        // Клик по строке — показать большой QR
        tr.addEventListener('click', () => openQrModal(o.number));
    });

    table.appendChild(tbody);
    container.appendChild(table);
}

/* ------------------- ПАГИНАЦИЯ ------------------- */
function setupScrollPagination(filters) {
    const container = document.getElementById('pila-table-container');
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
            pilaCache.push(...newContent);
            appendPilaToTable(newContent);
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

function appendPilaToTable(newItems) {
    const tbody = document.querySelector('#pila-table-container tbody');
    newItems.forEach(o => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${escapeHtml(o.number)}</td>
            <td>${escapeHtml(o.article ?? '-')}</td>
            <td>${o.length ?? '-'}</td>
            <td>${o.width ?? '-'}</td>
            <td>${o.quantity ?? '-'}</td>
            <td class="qr-cell" id="qr-${o.number}"></td>
        `;
        tbody.appendChild(tr);

        setTimeout(() => {
            const qrContainer = document.getElementById(`qr-${o.number}`);
            if (qrContainer) {
                new QRCode(qrContainer, {
                    text: String(o.number),
                    width: 128,
                    height: 128
                });
            }
        }, 0);

        tr.addEventListener('click', () => openQrModal(o.number));
    });
}

/* ------------------- МОДАЛЬНОЕ ОКНО ------------------- */
function createQrModal() {
    if (document.getElementById('qr-modal')) return;

    const modal = document.createElement('div');
    modal.id = 'qr-modal';
    modal.classList.add('pila-modal');
    modal.style.display = 'none';

    const content = document.createElement('div');
    content.classList.add('pila-modal-content');

    const closeBtn = document.createElement('button');
    closeBtn.innerHTML = '&times;';
    closeBtn.classList.add('pila-close-button');
    closeBtn.addEventListener('click', closeQrModal);

    const qrContainer = document.createElement('div');
    qrContainer.id = 'qr-modal-code';

    content.append(closeBtn, qrContainer);
    modal.append(content);
    document.body.append(modal);
}

function openQrModal(orderNumber) {
    const modal = document.getElementById('qr-modal');
    const qrContainer = document.getElementById('qr-modal-code');
    qrContainer.innerHTML = '';

    new QRCode(qrContainer, {
        text: String(orderNumber),
        width: 300,
        height: 300
    });

    modal.style.display = 'flex';
}

function closeQrModal() {
    document.getElementById('qr-modal').style.display = 'none';
}

/* ------------------- УТИЛИТЫ ------------------- */
function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
