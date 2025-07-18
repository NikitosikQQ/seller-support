import {checkTokenExpirationAndGet} from "./panel.js";
import {fetchArticles} from "./article-panel.js";

export async function fetchComments(needTable) {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/comments', {
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
        const comments = await response.json();
        if (needTable) {
            renderCommentsTable(comments);
        }
        return comments;
    } catch (error) {
        console.error('Ошибка при получении комментариев:', error);
    }
}

export async function fetchDeleteComment(id) {
    var token = checkTokenExpirationAndGet();
    try {
        const response = await fetch('/api/v1/comments', {
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
            alert('Коммент успешно удален');
            fetchComments(true);
        }
    } catch (error) {
        console.error('Ошибка при удалении коммента:', error);
    }
}

export function renderCommentsTable(comments) {
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
    header.textContent = 'Информация по комментариям к отчету';
    headerContainer.appendChild(header);

    const createButton = document.createElement('button');
    createButton.textContent = 'Создать';
    createButton.classList.add('edit-button');
    createButton.addEventListener('click', openCreateCommentModal);
    headerContainer.appendChild(createButton);

    container.appendChild(headerContainer);

    // Создаем таблицу
    const table = document.createElement('table');
    table.classList.add('shop-table');

    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    ['Комментарий', 'Условия', 'Артикулы', 'Действия'].forEach(text => {
        const th = document.createElement('th');
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);
    const tbody = document.createElement('tbody');
    comments.forEach(comment => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${comment.value}</td>
            <td>${formatConditionsToText(comment)}</td>
            <td>${formatArticlesInLines(comment.articlesName || [])}</td>
            <td>
                <button class="edit-button">Изменить</button>
                <button class="delete-button">Удалить</button>
            </td>
        `;
        tbody.appendChild(row);

        const editButton = row.querySelector('.edit-button');
        editButton.addEventListener('click', () => openEditCommentModal(comment));

        const deleteButton = row.querySelector('.delete-button');
        deleteButton.addEventListener('click', () => fetchDeleteComment(comment.id));
    });
    table.appendChild(tbody);

    container.appendChild(table);
}

export async function openCreateCommentModal() {
    const modal = document.createElement('div');
    modal.classList.add('smart-modal-overlay');

    const modalContent = document.createElement('div');
    modalContent.classList.add('smart-modal-content');

    const closeButton = document.createElement('button');
    closeButton.textContent = '×';
    closeButton.classList.add('smart-modal-close');
    closeButton.addEventListener('click', () => modal.remove());

    const form = document.createElement('form');
    const headerForm = document.createElement('h3');
    headerForm.textContent = 'Создание комментария';

    const commentData = await fetchComments(false);
    const articlesData = await fetchArticles(false);

    const commentValueInput = document.createElement('input');
    commentValueInput.type = 'text';
    commentValueInput.placeholder = 'Значение комментария';
    commentValueInput.required = true;
    commentValueInput.classList.add('smart-input');

    // Группа логики и кнопка добавления условия в одном ряду
    const logicRowContainer = document.createElement('div');
    logicRowContainer.classList.add('smart-logic-row');

    const logicSymbols = commentData[0].logicGroupValues
    const conditionFields = commentData[0].conditionFields
    const conditionOperators = commentData[0].conditionOperators

    const groupLogicInput = createAutocompleteInput('Тип логической группировки условий', logicSymbols);

    const addConditionButton = document.createElement('button');
    addConditionButton.type = 'button';
    addConditionButton.textContent = 'Добавить простое условие';
    addConditionButton.classList.add('smart-button', 'add');
    addConditionButton.addEventListener('click', () => {
        conditionsContainer.appendChild(createConditionRow());
    });

    logicRowContainer.appendChild(groupLogicInput);
    logicRowContainer.appendChild(addConditionButton);

    const conditionsContainer = document.createElement('div');
    conditionsContainer.classList.add('smart-condition-container');

    function createConditionRow() {
        const conditionRow = document.createElement('div');
        conditionRow.classList.add('smart-condition-row');

        const fieldInput = createAutocompleteInput('Поле из заказа', conditionFields);
        const operatorInput = createAutocompleteInput('Оператор', conditionOperators);
        const valueInput = document.createElement('input');
        valueInput.type = 'text';
        valueInput.placeholder = 'Значение';
        valueInput.classList.add('smart-select-wrapper');

        const removeButton = document.createElement('button');
        removeButton.type = 'button';
        removeButton.textContent = '×';
        removeButton.classList.add('smart-button', 'cancel');
        removeButton.addEventListener('click', () => conditionRow.remove());

        conditionRow.append(fieldInput, operatorInput, valueInput, removeButton);
        return conditionRow;
    }

    const articleSelect = createAutocompleteInput('Выберите артикул из списка', articlesData.map(a => a.name));
    const selectedArticlesContainer = document.createElement('div');
    selectedArticlesContainer.classList.add('smart-article-tags');

    const addArticleButton = document.createElement('button');
    addArticleButton.type = 'button';
    addArticleButton.textContent = 'Добавить артикул';
    addArticleButton.classList.add('smart-button', 'add');
    addArticleButton.addEventListener('click', () => {
        const val = articleSelect.querySelector('select')?.value;
        if (val && !Array.from(selectedArticlesContainer.children).some(child => child.textContent.startsWith(val))) {
            const tag = document.createElement('span');
            tag.classList.add('smart-article-tag');
            tag.textContent = val;

            const removeBtn = document.createElement('button');
            removeBtn.textContent = '×';
            removeBtn.addEventListener('click', () => tag.remove());

            tag.appendChild(removeBtn);
            selectedArticlesContainer.appendChild(tag);
        }
    });

    const addAllArticlesButton = document.createElement('button');
    addAllArticlesButton.type = 'button';
    addAllArticlesButton.textContent = 'Добавить все артикулы';
    addAllArticlesButton.classList.add('smart-button', 'add');
    addAllArticlesButton.style.marginLeft = '0.5rem'; // небольшой отступ слева
    addAllArticlesButton.addEventListener('click', () => {
        articlesData.forEach(article => {
            const val = article.name;
            // Проверяем, что такого артикула ещё нет в выбранных
            if (!Array.from(selectedArticlesContainer.children).some(child => child.textContent.startsWith(val))) {
                const tag = document.createElement('span');
                tag.classList.add('smart-article-tag');
                tag.textContent = val;

                const removeBtn = document.createElement('button');
                removeBtn.textContent = '×';
                removeBtn.addEventListener('click', () => tag.remove());

                tag.appendChild(removeBtn);
                selectedArticlesContainer.appendChild(tag);
            }
        });
    });

    const buttonGroup = document.createElement('div');
    buttonGroup.classList.add('smart-button-group');

    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Отменить';
    cancelButton.classList.add('smart-button', 'cancel');
    cancelButton.type = 'button';
    cancelButton.addEventListener('click', () => modal.remove());

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.classList.add('smart-button', 'save');
    saveButton.type = 'submit';

    buttonGroup.append(saveButton, cancelButton);

    form.append(
        headerForm,
        commentValueInput,
        logicRowContainer,
        conditionsContainer,
        articleSelect,
        addArticleButton,
        addAllArticlesButton,
        selectedArticlesContainer,
        buttonGroup
    );

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const conditions = [];
        conditionsContainer.querySelectorAll('.smart-condition-row').forEach(row => {
            const wrappers = row.querySelectorAll('.smart-select-wrapper');

            // первые два — это div с select внутри
            const fieldSelect = wrappers[0].querySelector('select');
            const operatorSelect = wrappers[1].querySelector('select');

            // третий — это input (сам элемент)
            const valueInput = wrappers[2].tagName === 'INPUT' ? wrappers[2] : wrappers[2].querySelector('input');

            const field = fieldSelect?.value.trim() || '';
            const condition = operatorSelect?.value.trim() || '';
            const value = valueInput?.value.trim() || '';

            if (field && condition && value) {
                conditions.push({field, condition, value});
            }
        });

        const articles = Array.from(selectedArticlesContainer.children).map(span => span.firstChild.textContent);

        const payload = {
            value: commentValueInput.value.trim(),
            conditions: [{groupLogic: groupLogicInput.querySelector('select').value || null, rules: conditions}],
            articlesName: articles
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch('/api/v1/comments', {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const data = await response.json();
                alert('Ошибка: ' + data.message);
            } else {
                alert('Коммент успешно создан');
                modal.remove();
                await fetchComments(true);
            }
        } catch (error) {
            console.error('Ошибка при создании комментария:', error);
            alert('Не удалось создать комментарий');
        }
    });

    modalContent.append(closeButton, form);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);
}


export async function openEditCommentModal(comment) {
    const modal = document.createElement('div');
    modal.classList.add('smart-modal-overlay');

    const modalContent = document.createElement('div');
    modalContent.classList.add('smart-modal-content');

    const closeButton = document.createElement('button');
    closeButton.textContent = '×';
    closeButton.classList.add('smart-modal-close');
    closeButton.addEventListener('click', () => modal.remove());

    const form = document.createElement('form');
    const headerForm = document.createElement('h3');
    headerForm.textContent = 'Редактирование комментария';

    const commentData = await fetchComments(false);
    const articlesData = await fetchArticles(false);

    const logicSymbols = commentData[0].logicGroupValues;
    const conditionFields = commentData[0].conditionFields;
    const conditionOperators = commentData[0].conditionOperators;

    const commentValueInput = document.createElement('input');
    commentValueInput.type = 'text';
    commentValueInput.placeholder = 'Значение комментария';
    commentValueInput.required = true;
    commentValueInput.classList.add('smart-input');
    commentValueInput.value = comment.value || '';

    const logicRowContainer = document.createElement('div');
    logicRowContainer.classList.add('smart-logic-row');

    const groupLogicInput = createAutocompleteInput('Тип логической группировки условий', logicSymbols);
    const groupLogicSelect = groupLogicInput.querySelector('select');
    if (comment.conditions?.[0]?.groupLogic) {
        groupLogicSelect.value = comment.conditions[0].groupLogic;
    }

    const addConditionButton = document.createElement('button');
    addConditionButton.type = 'button';
    addConditionButton.textContent = 'Добавить простое условие';
    addConditionButton.classList.add('smart-button', 'add');
    addConditionButton.addEventListener('click', () => {
        conditionsContainer.appendChild(createConditionRow());
    });

    logicRowContainer.appendChild(groupLogicInput);
    logicRowContainer.appendChild(addConditionButton);

    const conditionsContainer = document.createElement('div');
    conditionsContainer.classList.add('smart-condition-container');

    function createConditionRow(condition = {}) {
        const conditionRow = document.createElement('div');
        conditionRow.classList.add('smart-condition-row');

        const fieldInput = createAutocompleteInput('Поле из заказа', conditionFields);
        const operatorInput = createAutocompleteInput('Оператор', conditionOperators);
        const valueInput = document.createElement('input');
        valueInput.type = 'text';
        valueInput.placeholder = 'Значение';
        valueInput.classList.add('smart-select-wrapper');

        // Установить значения, если есть
        if (condition.field) fieldInput.querySelector('select').value = condition.field;
        if (condition.condition) operatorInput.querySelector('select').value = condition.condition;
        if (condition.value) valueInput.value = condition.value;

        const removeButton = document.createElement('button');
        removeButton.type = 'button';
        removeButton.textContent = '×';
        removeButton.classList.add('smart-button', 'cancel');
        removeButton.addEventListener('click', () => conditionRow.remove());

        conditionRow.append(fieldInput, operatorInput, valueInput, removeButton);
        return conditionRow;
    }

    // Заполняем условия
    const rules = comment.conditions?.[0]?.rules || [];
    rules.forEach(rule => {
        conditionsContainer.appendChild(createConditionRow(rule));
    });

    const articleSelect = createAutocompleteInput('Выберите артикул из списка', articlesData.map(a => a.name));
    const selectedArticlesContainer = document.createElement('div');
    selectedArticlesContainer.classList.add('smart-article-tags');

    const addArticleButton = document.createElement('button');
    addArticleButton.type = 'button';
    addArticleButton.textContent = 'Добавить артикул';
    addArticleButton.classList.add('smart-button', 'add');
    addArticleButton.addEventListener('click', () => {
        const val = articleSelect.querySelector('select')?.value;
        if (val && !Array.from(selectedArticlesContainer.children).some(child => child.textContent.startsWith(val))) {
            const tag = document.createElement('span');
            tag.classList.add('smart-article-tag');
            tag.textContent = val;

            const removeBtn = document.createElement('button');
            removeBtn.textContent = '×';
            removeBtn.addEventListener('click', () => tag.remove());

            tag.appendChild(removeBtn);
            selectedArticlesContainer.appendChild(tag);
        }
    });

    const addAllArticlesButton = document.createElement('button');
    addAllArticlesButton.type = 'button';
    addAllArticlesButton.textContent = 'Добавить все артикулы';
    addAllArticlesButton.classList.add('smart-button', 'add');
    addAllArticlesButton.style.marginLeft = '0.5rem';
    addAllArticlesButton.addEventListener('click', () => {
        articlesData.forEach(article => {
            const val = article.name;
            if (!Array.from(selectedArticlesContainer.children).some(child => child.textContent.startsWith(val))) {
                const tag = document.createElement('span');
                tag.classList.add('smart-article-tag');
                tag.textContent = val;

                const removeBtn = document.createElement('button');
                removeBtn.textContent = '×';
                removeBtn.addEventListener('click', () => tag.remove());

                tag.appendChild(removeBtn);
                selectedArticlesContainer.appendChild(tag);
            }
        });
    });

    // Заполняем артикулы
    (comment.articlesName || []).forEach(name => {
        const tag = document.createElement('span');
        tag.classList.add('smart-article-tag');
        tag.textContent = name;

        const removeBtn = document.createElement('button');
        removeBtn.textContent = '×';
        removeBtn.addEventListener('click', () => tag.remove());

        tag.appendChild(removeBtn);
        selectedArticlesContainer.appendChild(tag);
    });

    const buttonGroup = document.createElement('div');
    buttonGroup.classList.add('smart-button-group');

    const cancelButton = document.createElement('button');
    cancelButton.textContent = 'Отменить';
    cancelButton.classList.add('smart-button', 'cancel');
    cancelButton.type = 'button';
    cancelButton.addEventListener('click', () => modal.remove());

    const saveButton = document.createElement('button');
    saveButton.textContent = 'Сохранить';
    saveButton.classList.add('smart-button', 'save');
    saveButton.type = 'submit';

    buttonGroup.append(saveButton, cancelButton);

    form.append(
        headerForm,
        commentValueInput,
        logicRowContainer,
        conditionsContainer,
        articleSelect,
        addArticleButton,
        addAllArticlesButton,
        selectedArticlesContainer,
        buttonGroup
    );

    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const conditions = [];
        conditionsContainer.querySelectorAll('.smart-condition-row').forEach(row => {
            const wrappers = row.querySelectorAll('.smart-select-wrapper');
            const fieldSelect = wrappers[0].querySelector('select');
            const operatorSelect = wrappers[1].querySelector('select');
            const valueInput = wrappers[2].tagName === 'INPUT' ? wrappers[2] : wrappers[2].querySelector('input');

            const field = fieldSelect?.value.trim() || '';
            const condition = operatorSelect?.value.trim() || '';
            const value = valueInput?.value.trim() || '';

            if (field && condition && value) {
                conditions.push({ field, condition, value });
            }
        });

        const articles = Array.from(selectedArticlesContainer.children).map(span => span.firstChild.textContent);

        const payload = {
            id: comment.id,
            value: commentValueInput.value.trim(),
            conditions: [{
                groupLogic: groupLogicSelect.value || null,
                rules: conditions
            }],
            articlesName: articles
        };

        try {
            const token = checkTokenExpirationAndGet();
            const response = await fetch(`/api/v1/comments`, {
                method: 'PUT',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(payload),
            });

            if (!response.ok) {
                const data = await response.json();
                alert('Ошибка: ' + data.message);
            } else {
                alert('Комментарий успешно обновлён');
                modal.remove();
                await fetchComments(true);
            }
        } catch (error) {
            console.error('Ошибка при редактировании комментария:', error);
            alert('Не удалось обновить комментарий');
        }
    });

    modalContent.append(closeButton, form);
    modal.appendChild(modalContent);
    document.body.appendChild(modal);
}


function formatConditionsToText(comment) {
    if (!comment.conditions || comment.conditions.length === 0 || comment.conditions[0].rules.length === 0) {
        return 'Отсутствуют';
    }

    return comment.conditions.map(conditionGroup => {
        const rules = conditionGroup.rules || [];

        // Если одно условие — просто отобразить его
        if (rules.length === 1) {
            const rule = rules[0];
            return `${rule.field} ${rule.condition} ${rule.value}`;
        }

        // Если несколько — соединяем через groupLogic
        const groupLogic = conditionGroup.groupLogic;

        const rulesText = rules.map(rule => {
            return `${rule.field} ${rule.condition} ${rule.value}`;
        }).join(` ${groupLogic} `);

        return rulesText;
    }).join(';<br>');
}

function createAutocompleteInput(placeholder, options = []) {
    const wrapper = document.createElement('div');
    wrapper.classList.add('smart-select-wrapper');

    const select = document.createElement('select');

    // Placeholder как disabled пункт
    const placeholderOption = document.createElement('option');
    placeholderOption.value = '';
    placeholderOption.textContent = placeholder;
    placeholderOption.disabled = true;
    placeholderOption.selected = true;
    select.appendChild(placeholderOption);

    // Добавляем опции
    options.forEach(opt => {
        const option = document.createElement('option');
        option.value = opt;
        option.textContent = opt;
        select.appendChild(option);
    });

    wrapper.appendChild(select);
    wrapper.input = select;
    return wrapper;
}

function formatArticlesInLines(articles, itemsPerLine = 4) {
    const lines = [];
    for (let i = 0; i < articles.length; i += itemsPerLine) {
        lines.push(articles.slice(i, i + itemsPerLine).join(', '));
    }
    return lines.join('<br>');
}