import {checkTokenExpirationAndGet, getRolesFromToken} from "./panel.js";

/* ------------------- ГЛОБАЛЬНЫЕ ------------------- */
let selectedWorkplace = null;
let allWorkplaces = [];
let userRoles = [];
let username = null;
let autoRefreshInterval = null;

/* ------------------- ОТКРЫТИЕ ПАНЕЛИ ------------------- */
export async function openWorkMonitoringPanel() {
    const container = document.getElementById('main-container');
    container.innerHTML = '';

    const token = checkTokenExpirationAndGet();
    userRoles = getRolesFromToken(token);
    username = getUsernameFromToken(token);

    const isAdmin = userRoles.includes('ROLE_ADMIN');
    const isManager = userRoles.includes('ROLE_MANAGER');

    // 🔥 clear previous auto-refresh if exists
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
        autoRefreshInterval = null;
    }

    const headerWrapper = document.createElement('div');
    headerWrapper.style.display = 'flex';
    headerWrapper.style.alignItems = 'center';
    headerWrapper.style.justifyContent = 'space-between';
    headerWrapper.style.marginBottom = '15px';

    const header = document.createElement('h3');
    header.textContent = 'Мониторинг выполненных работ';

    const buttonsWrapper = document.createElement('div');
    buttonsWrapper.style.display = 'flex';
    buttonsWrapper.style.gap = '10px';

    // кнопка выгрузки только для админа и менеджера
    let exportButton = null;
    if (isAdmin || isManager) {
        exportButton = document.createElement('button');
        exportButton.textContent = 'Выгрузить отчёт';
        exportButton.classList.add('order-export-button');
        exportButton.addEventListener('click', () => openExportModal());
        buttonsWrapper.appendChild(exportButton);
    }

    headerWrapper.append(header, buttonsWrapper);
    container.appendChild(headerWrapper);

    // селект выбора рабочего места
    const workplaceSection = document.createElement('div');
    workplaceSection.classList.add('workplace-select-section');
    container.appendChild(workplaceSection);

    // таблица
    const tableContainer = document.createElement('div');
    tableContainer.id = 'work-monitoring-table-container';
    container.appendChild(tableContainer);

    // загрузка рабочих мест
    if (isAdmin || isManager) {
        allWorkplaces = await fetchJson('/api/v1/workplaces/names', token);
        renderWorkplaceSelect(workplaceSection, allWorkplaces, false);
        await loadActualCapacity(null);
        startAutoRefresh(5 * 60 * 1000, null); // 🔥 every 5 min
    } else {
        const userWorkplaces = await fetchJson(`/api/v1/admin/users/${username}/workplaces`, token);
        if (!userWorkplaces || userWorkplaces.length === 0) {
            tableContainer.textContent = 'У вас нет доступных рабочих мест.';
            return;
        }
        renderWorkplaceSelect(workplaceSection, userWorkplaces, true);
        startAutoRefresh(60 * 60 * 1000, null); // 🔥 every 60 min
    }
}

/* ------------------- РЕНДЕР СЕЛЕКТА ------------------- */
function renderWorkplaceSelect(container, workplaces, isMandatory) {
    const wrapper = document.createElement('div');
    wrapper.classList.add('select-wrapper');

    const select = document.createElement('select');
    const placeholder = isMandatory ? 'Выберите рабочее место' : 'Все рабочие места';
    const emptyOption = document.createElement('option');
    emptyOption.value = '';
    emptyOption.textContent = placeholder;
    select.appendChild(emptyOption);

    workplaces.forEach(w => {
        const opt = document.createElement('option');
        opt.value = w;
        opt.textContent = w;
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

    container.innerHTML = ''; // очищаем перед вставкой
    container.appendChild(wrapper);

    select.addEventListener('change', async () => {
        selectedWorkplace = select.value || null;
        if (isMandatory && !selectedWorkplace) return;
        await loadActualCapacity(selectedWorkplace);
        restartAutoRefresh(selectedWorkplace);
    });
}

/* ------------------- ЗАГРУЗКА ДАННЫХ ------------------- */
async function loadActualCapacity(workplace) {
    const tableContainer = document.getElementById('work-monitoring-table-container');
    if (!tableContainer) {
        console.warn('[auto-refresh] таблица отсутствует в DOM — пропуск обновления');
        return;
    }
    tableContainer.innerHTML = '<div>Загрузка...</div>';

    try {
        const token = checkTokenExpirationAndGet();
        const url = workplace
            ? `/api/v1/employees/capacity/actual?workplace=${encodeURIComponent(workplace)}`
            : '/api/v1/employees/capacity/actual';

        const data = await fetchJson(url, token);
        const showCapacity = userRoles.includes('ROLE_ADMIN') || userRoles.includes('ROLE_MANAGER'); // 🔥 new
        renderCapacityTable(data, showCapacity);
    } catch (e) {
        console.error('Ошибка загрузки данных', e);
        tableContainer.textContent = 'Ошибка загрузки данных';
    }
}

/* ------------------- РЕНДЕР ТАБЛИЦЫ ------------------- */
function renderCapacityTable(data, showCapacity) {
    const container = document.getElementById('work-monitoring-table-container');
    container.innerHTML = '';

    if (!data || data.length === 0) {
        container.textContent = 'Нет данных';
        return;
    }

    const table = document.createElement('table');
    table.classList.add('order-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');

    const headers = showCapacity
        ? ['Сотрудник', 'Рабочее место', 'Выполнено (м²)', 'Заработано (₽)']
        : ['Сотрудник', 'Рабочее место', 'Заработано (₽)']; // 🔥 new

    headers.forEach(h => {
        const th = document.createElement('th');
        th.textContent = h;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');
    data.forEach(d => {
        const tr = document.createElement('tr');
        tr.innerHTML = showCapacity
            ? `
                <td>${escapeHtml(d.username)}</td>
                <td>${escapeHtml(d.workplace)}</td>
                <td>${d.capacity ?? '-'}</td>
                <td>${d.earnedAmount ?? '-'}</td>
              `
            : `
                <td>${escapeHtml(d.username)}</td>
                <td>${escapeHtml(d.workplace)}</td>
                <td>${d.earnedAmount ?? '-'}</td>
              `;
        tbody.appendChild(tr);
    });
    table.appendChild(tbody);

    container.appendChild(table);
}

/* ------------------- АВТО-ОБНОВЛЕНИЕ ------------------- */
function startAutoRefresh(intervalMs, workplace) {
    autoRefreshInterval = setInterval(async () => {
        console.log(`[auto-refresh] обновляем таблицу (${new Date().toLocaleTimeString()})`);
        await loadActualCapacity(selectedWorkplace ?? workplace ?? null);
    }, intervalMs);
}

// 🔥 перезапуск при смене рабочего места
function restartAutoRefresh(workplace) {
    const isAdmin = userRoles.includes('ROLE_ADMIN');
    const isManager = userRoles.includes('ROLE_MANAGER');
    if (autoRefreshInterval) clearInterval(autoRefreshInterval);
    const interval = (isAdmin || isManager) ? 5 * 60 * 1000 : 60 * 60 * 1000;
    startAutoRefresh(interval, workplace);
}


/* ------------------- МОДАЛКА ВЫГРУЗКИ ------------------- */
export async function openExportModal() {
    const token = checkTokenExpirationAndGet();

    // Создаём модалку
    const modal = document.createElement('div');
    modal.classList.add('wm-modal'); // NEW

    const modalContent = document.createElement('div');
    modalContent.classList.add('wm-modal__content'); // NEW

    // close
    const closeBtn = document.createElement('span');
    closeBtn.classList.add('wm-modal__close'); // NEW
    closeBtn.innerHTML = '&times;';
    closeBtn.onclick = () => modal.remove();

    // title
    const title = document.createElement('h3');
    title.classList.add('wm-modal__title'); // NEW
    title.textContent = 'Фильтры для отчёта по выполненной работе';

    // контейнер фильтров (используем уже существующие хелперы)
    const filtersWrapper = document.createElement('div');
    filtersWrapper.classList.add('wm-filters'); // NEW

    const fromDate = createDateInput('Дата от');
    const toDate = createDateInput('Дата до');
    const usernameInput = createInput('Имя пользователя');

    // Получаем список рабочих мест (тот же эндпоинт)
    const workplaces = await fetchJson('/api/v1/workplaces/names', token);
    const workplacesSelect = createMultiSelect('Выберите рабочие места', workplaces);

    // Оборачиваем строки фильтров в "ряда" с новыми классами
    const row1 = document.createElement('div');
    row1.classList.add('wm-row'); // NEW
    row1.appendChild(fromDate);
    row1.appendChild(toDate);

    const row2 = document.createElement('div');
    row2.classList.add('wm-row'); // NEW
    row2.appendChild(usernameInput);
    row2.appendChild(workplacesSelect.element);

    filtersWrapper.append(row1, row2);

    // Кнопка "Сформировать отчет"
    const actions = document.createElement('div');
    actions.classList.add('wm-actions'); // NEW (контейнер для кнопок)

    const generateBtn = document.createElement('button');
    generateBtn.classList.add('wm-btn-generate'); // NEW
    generateBtn.textContent = 'Сформировать отчёт';

    // Нативный визуальный loader (показываем текст при формировании)
    const genLoader = document.createElement('span');
    genLoader.classList.add('wm-gen-loader'); // NEW
    genLoader.style.display = 'none';
    genLoader.textContent = ' ⏳ Генерация...';

    actions.append(generateBtn, genLoader);

    // Собираем модалку
    modalContent.append(closeBtn, title, filtersWrapper, actions);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);

    // Закрытие по фону
    modal.addEventListener('click', (e) => {
        if (e.target === modal) modal.remove();
    });

    // Обработчик кнопки
    generateBtn.addEventListener('click', async () => {
        // Берём значения из элементов-хелперов
        const fromRaw = fromDate.querySelector('input').value;
        const toRaw = toDate.querySelector('input').value;
        const from = formatLocalDate(fromRaw);
        const to = formatLocalDate(toRaw);
        const usernameVal = usernameInput.querySelector('input').value || null;
        const workplacesVal = workplacesSelect.choices.getValue(true);
        const request = {
            from: from || null,
            to: to || null,
            username: usernameVal || null,
            workplaces: workplacesVal.length > 0 ? workplacesVal : null
        };

        // UI: loader
        generateBtn.disabled = true;
        genLoader.style.display = 'inline-block';

        try {
            const tokenLocal = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/reports/employees/capacity', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${tokenLocal}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(request)
            });

            if (response.status === 404) {
                alert('Данные для отчёта не найдены');
                return;
            }

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || 'Ошибка при формировании отчёта');
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;

            const disposition = response.headers.get('Content-Disposition');
            let filename = 'employee-capacity-report.xlsx';
            if (disposition) {
                const match = /filename\*=UTF-8''(.+)$/.exec(disposition) || /filename="(.+)"/.exec(disposition);
                if (match && match[1]) filename = decodeURIComponent(match[1]);
            }

            a.download = filename;
            a.click();
            window.URL.revokeObjectURL(url);

            // закрываем модалку после успешной генерации
            modal.remove();
        } catch (err) {
            console.error('Ошибка при экспорте отчёта', err);
            alert('Не удалось выгрузить отчёт: ' + (err.message || 'Ошибка'));
        } finally {
            generateBtn.disabled = false;
            genLoader.style.display = 'none';
        }
    });
}

/* ------------------- ЭКСПОРТ ------------------- */
async function exportEmployeeCapacity(filters, button) {
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = '⏳ Генерация...';

    try {
        const token = checkTokenExpirationAndGet();
        const response = await fetch('/api/v1/reports/employees/capacity', {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(filters)
        });

        if (response.status === 404) {
            alert('Данные для отчёта не найдены');
            return;
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        const disposition = response.headers.get('Content-Disposition');
        let filename = 'employee-capacity-report.xlsx';
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

/* ------------------- ВСПОМОГАТЕЛЬНЫЕ ------------------- */
async function fetchJson(url, token) {
    try {
        const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
        return await res.json();
    } catch (e) {
        console.error('Ошибка fetchJson', e);
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

function createMultiSelect(placeholder, options = []) {
    const wrapper = document.createElement('div');
    wrapper.classList.add('select-wrapper');
    const select = document.createElement('select');
    select.multiple = true;

    options.forEach(opt => {
        const o = document.createElement('option');
        o.value = opt;
        o.textContent = opt;
        select.appendChild(o);
    });

    wrapper.appendChild(select);
    const choices = new Choices(select, {
        removeItemButton: true,
        placeholder: true,
        placeholderValue: placeholder,
        itemSelectText: '',
        searchEnabled: true
    });

    return { element: wrapper, choices };
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
        enableTime: false,
        dateFormat: "Y-m-d",
        locale: "ru",
        allowInput: true
    });
    return wrapper;
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

function getUsernameFromToken(token) {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.sub || payload.username || null;
}

function formatLocalDate(value) {
    if (!value) return null;
    const d = new Date(value);
    return d.toISOString().split('T')[0];
}