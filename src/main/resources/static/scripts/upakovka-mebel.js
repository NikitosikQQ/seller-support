import {checkTokenExpirationAndGet} from "./panel.js";

// библиотека для QR — просто подключи <script src="https://cdn.jsdelivr.net/npm/qrcodejs@1.0.0/qrcode.min.js"></script> в index.html
// https://github.com/davidshimjs/qrcodejs

let currentPage = 0;
let pageSize = 20;
let totalPages = 0;
let isLoading = false;
let hasMore = true;
let lastSearchRequest = {};
let upakovkaCache = [];
let observer = null;

/* ------------------- ОТКРЫТИЕ ПАНЕЛИ ------------------- */
export async function openUpakovkaPanel() {
    const container = document.getElementById('main-container');
    container.innerHTML = '';

    const headerWrapper = document.createElement('div');
    headerWrapper.style.display = 'flex';
    headerWrapper.style.alignItems = 'center';
    headerWrapper.style.justifyContent = 'space-between';
    headerWrapper.style.marginBottom = '15px';

    const header = document.createElement('h3');
    header.textContent = 'УПАКОВКА МЕБЕЛИ';

    const refreshButton = document.createElement('button');
    refreshButton.textContent = '🔄 Обновить список';
    refreshButton.classList.add('pila-search-button');
    refreshButton.addEventListener('click', () => {
        currentPage = 0;
        hasMore = true;
        upakovkaCache = [];
        if (observer) observer.disconnect();
        performSearch(lastSearchRequest);
    });

    headerWrapper.append(header, refreshButton);
    container.appendChild(headerWrapper);

    const filtersContainer = await createMaterialFilterSection();
    container.appendChild(filtersContainer);

    const tableContainer = document.createElement('div');
    tableContainer.id = 'upakovka-table-container';
    container.appendChild(tableContainer);

    // создаём модальное окно для QR
    createQrModal();

    // выполняем поиск сразу
    performSearch(await getDefaultSearchParams());
}

/* ------------------- ФИЛЬТР МАТЕРИАЛА ------------------- */
async function createMaterialFilterSection() {
    const filters = document.createElement('div');
    filters.classList.add('mebel-filters-section');

    const token = checkTokenExpirationAndGet();
    const materials = await fetchJson('/api/v1/materials', token);
    const packagingMaterials = materials.filter(m => m.isOnlyPackaging).map(m => m.name);

    const materialSelect = createSingleSelect('Все упаковочные материалы', packagingMaterials);

    materialSelect.choices.passedElement.element.addEventListener('change', async () => {
        currentPage = 0;
        hasMore = true;
        upakovkaCache = [];
        if (observer) observer.disconnect();

        const selectedMaterial = materialSelect.choices.getValue(true) || null;
        const filtersData = await getDefaultSearchParams(selectedMaterial);
        performSearch(filtersData);
    });

    filters.append(materialSelect.element);
    return filters;
}

/* ------------------- ПАРАМЕТРЫ ПОИСКА ------------------- */
async function getDefaultSearchParams(selectedMaterial = null) {
    const token = checkTokenExpirationAndGet();
    const materials = await fetchJson('/api/v1/materials', token);

    const packagingMaterials = materials.filter(m => m.isOnlyPackaging).map(m => m.name);
    const nonPackagingMaterials = materials.filter(m => !m.isOnlyPackaging).map(m => m.name);

    return {
        statuses: ['CREATED'],
        materialName: selectedMaterial,
        excludeMaterialNames: nonPackagingMaterials,
        page: 0,
        size: 20
    };
}

/* ------------------- ПОИСК ------------------- */
async function performSearch(filters) {
    const tableContainer = document.getElementById('upakovka-table-container');
    tableContainer.innerHTML = '';

    try {
        const token = checkTokenExpirationAndGet();
        lastSearchRequest = { ...filters };

        const response = await fetch('/api/v1/orders/search', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(lastSearchRequest)
        });

        const data = await response.json();
        upakovkaCache = Array.isArray(data?.content) ? data.content : [];

        renderUpakovkaTable(upakovkaCache);

        currentPage = data.number ?? 0;
        totalPages = data.totalPages ?? 0;
        hasMore = !data.last ?? (upakovkaCache.length === pageSize);

        if (upakovkaCache.length === 0) {
            tableContainer.textContent = 'Нет данных';
            return;
        }

        setupScrollPagination(filters);
    } catch (err) {
        console.error('Ошибка при загрузке упаковки мебели', err);
        alert('Не удалось загрузить данные.');
    }
}

/* ------------------- ТАБЛИЦА ------------------- */
function renderUpakovkaTable(items) {
    const container = document.getElementById('upakovka-table-container');
    container.innerHTML = '';

    if (!items || items.length === 0) {
        container.textContent = 'Нет данных';
        return;
    }

    const table = document.createElement('table');
    table.classList.add('order-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['№ заказа', 'Артикул', 'Материал', 'Количество', 'QR'].forEach(h => {
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
            <td>${escapeHtml(o.materialName ?? '-')}</td>
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

        // Клик по строке — открыть большой QR
        tr.addEventListener('click', () => openQrModal(o.number));
    });

    table.appendChild(tbody);
    container.appendChild(table);
}

/* ------------------- ПАГИНАЦИЯ ------------------- */
function setupScrollPagination(filters) {
    const container = document.getElementById('upakovka-table-container');
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
            upakovkaCache.push(...newContent);
            appendUpakovkaToTable(newContent);
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

function appendUpakovkaToTable(newItems) {
    const tbody = document.querySelector('#upakovka-table-container tbody');
    newItems.forEach(o => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${escapeHtml(o.number)}</td>
            <td>${escapeHtml(o.article ?? '-')}</td>
            <td>${escapeHtml(o.materialName ?? '-')}</td>
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

function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
