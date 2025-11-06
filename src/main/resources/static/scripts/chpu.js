import {checkTokenExpirationAndGet} from "./panel.js";

/* ------------------- ГЛОБАЛЬНЫЕ ------------------- */
let chpuOrdersCache = [];

/* ------------------- ОТКРЫТИЕ ПАНЕЛИ ------------------- */
export async function openChpuPanel() {
    const container = document.getElementById('main-container');
    container.innerHTML = '';

    // Заголовок + кнопка "Выгрузить отчёт"
    const headerWrapper = document.createElement('div');
    headerWrapper.style.display = 'flex';
    headerWrapper.style.alignItems = 'center';
    headerWrapper.style.justifyContent = 'space-between';
    headerWrapper.style.marginBottom = '15px';

    const header = document.createElement('h3');
    header.textContent = 'Заказы для ЧПУ';

    // Кнопка "Выгрузить отчёт"
    const searchButton = document.createElement('button');
    searchButton.textContent = '🔄 Обновить список';
    searchButton.classList.add('chpu-search-button');
    searchButton.addEventListener('click', performChpuSearch);

    headerWrapper.append(header, searchButton);
    container.appendChild(headerWrapper);

    // Контейнер для таблицы
    const tableContainer = document.createElement('div');
    tableContainer.id = 'chpu-table-container';
    container.appendChild(tableContainer);

    // ⚙️ Автоматически запускаем поиск сразу при открытии
    performChpuSearch();
}

/* ------------------- ПОИСК ------------------- */
async function performChpuSearch() {
    const container = document.getElementById('chpu-table-container');
    container.innerHTML = '<div class="loader"></div>';

    try {
        const token = checkTokenExpirationAndGet();
        const response = await fetch('/api/v1/orders/chpu/search', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                statuses: ["CREATED"]
            })
        });

        if (!response.ok) {
            throw new Error('Ошибка при получении заказов для ЧПУ');
        }

        const data = await response.json();
        chpuOrdersCache = Array.isArray(data) ? data : [];

        if (chpuOrdersCache.length === 0) {
            container.textContent = 'Нет заказов для ЧПУ.';
            return;
        }

        renderChpuTable(chpuOrdersCache);
    } catch (err) {
        console.error('Ошибка при поиске ЧПУ заказов', err);
        container.textContent = '❌ Не удалось загрузить заказы для ЧПУ.';
    }
}

/* ------------------- ТАБЛИЦА ------------------- */
function renderChpuTable(orders) {
    const container = document.getElementById('chpu-table-container');
    container.innerHTML = '';

    const table = document.createElement('table');
    table.classList.add('chpu-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Артикул', 'Суммарная площадь (м²)', 'Действие'].forEach(h => {
        const th = document.createElement('th');
        th.textContent = h;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    orders.forEach(o => {
        const tr = document.createElement('tr');

        const tdArticle = document.createElement('td');
        tdArticle.textContent = o.shortArticle || '-';

        const tdArea = document.createElement('td');
        tdArea.textContent = o.areaSummary != null ? Number(o.areaSummary).toFixed(3) : '-';

        const tdAction = document.createElement('td');
        const downloadBtn = document.createElement('button');
        downloadBtn.textContent = '📥 Выгрузить шаблон';
        downloadBtn.classList.add('chpu-download-button');
        downloadBtn.addEventListener('click', () => exportChpuTemplate(o.orderNumbers, downloadBtn));

        tdAction.appendChild(downloadBtn);
        tr.append(tdArticle, tdArea, tdAction);
        tbody.appendChild(tr);
    });

    table.appendChild(tbody);
    container.appendChild(table);
}

/* ------------------- ВЫГРУЗКА ------------------- */
async function exportChpuTemplate(orderNumbers, button) {
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = '⏳ Генерация...';

    try {
        const token = checkTokenExpirationAndGet();
        const response = await fetch('/api/v1/reports/orders/chpu', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ orderNumbers })
        });

        if (!response.ok) {
            throw new Error(`Ошибка при генерации шаблона (${response.status})`);
        }

        // 🎯 Получаем имя файла из заголовка
        const disposition = response.headers.get('Content-Disposition');
        let filename = 'chpu-template.xlsx';
        if (disposition) {
            const match = /filename\*=UTF-8''(.+)$/.exec(disposition) || /filename="(.+)"/.exec(disposition);
            if (match && match[1]) {
                filename = decodeURIComponent(match[1]);
            }
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        a.click();
        window.URL.revokeObjectURL(url);
    } catch (err) {
        console.error('Ошибка при выгрузке ЧПУ шаблона', err);
        alert('❌ Не удалось выгрузить шаблон.');
    } finally {
        button.textContent = originalText;
        button.disabled = false;
    }
}
