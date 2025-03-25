import {checkTokenExpirationAndGet} from "./panel.js";

const WILDBERRIES = 'WILDBERRIES';

export async function getPosting(from, to, yandexTo, supplies, signal) {
    const token = checkTokenExpirationAndGet();
    const response = await fetch(`/api/v1/reports/postings`, {
        method: 'POST',
        signal,
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            from: from,
            to: to,
            yandexTo: yandexTo,
            supplies: supplies
        })
    });

    if (!response.ok) {
        const data = await response.json();
        alert('Ошибка: ' + (data.message || 'Неизвестная ошибка'));
        throw new Error();
    }

    const blob = await response.blob();

    // Генерация названия файла на фронте
    const now = new Date();
    const formattedDate = now.toLocaleDateString('ru-RU').replace(/\./g, '');
    const filename = `Postings${formattedDate}.zip`;

    // Создаем ссылку для скачивания файла
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = filename; // Используем сгенерированное имя
    link.click();

    URL.revokeObjectURL(link.href);

}

export function openModal() {
    const modalOverlay = document.createElement('div');
    modalOverlay.className = 'modal';

    const modalContent = document.createElement('div');
    modalContent.className = 'modal-content';

    const controller = new AbortController();
    const signal = controller.signal;

    const closeButton = document.createElement('button');
    closeButton.className = 'close-button';
    closeButton.textContent = '×';
    closeButton.addEventListener('click', () => {
        controller.abort();
        document.body.removeChild(modalOverlay);
    });

    const title = document.createElement('h3');
    title.textContent = 'Фильтр по отправлениям';

    const fromDateLabel = document.createElement('label');
    fromDateLabel.textContent = 'Начало периода даты принятия в обработку OZON:';
    const fromDateInput = document.createElement('input');
    fromDateInput.type = 'date';
    fromDateInput.className = 'input-not-role';

    const toDateLabel = document.createElement('label');
    toDateLabel.textContent = 'Конец периода принятия в обработку OZON:';
    const toDateInput = document.createElement('input');
    toDateInput.type = 'date';
    toDateInput.className = 'input-not-role';

    // Лоадер
    const loader = document.createElement('div');
    loader.className = 'loader hidden'; // Скрыт по умолчанию

    // Сообщение об успехе
    const successMessage = document.createElement('p');
    successMessage.className = 'success-message hidden';
    successMessage.textContent = 'Документы успешно сформированы';

    const supplyContainer = document.createElement('div');
    supplyContainer.className = 'supply-container';

    // Функция добавления строки поставки
    function addSupplyRow() {
        const supplyRow = document.createElement('div');
        supplyRow.className = 'supply-row';

        const qrInput = document.createElement('input');
        qrInput.type = 'text';
        qrInput.placeholder = 'QR-код поставки';
        qrInput.className = 'input-not-role';
        qrInput.required = true

        const storeSelect = document.createElement('select');
        storeSelect.className = 'input-not-role';

        // Заполняем select магазинами из API
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        defaultOption.textContent = 'Выберите магазин';
        storeSelect.appendChild(defaultOption);

        findShopsByMarketplaceName(WILDBERRIES)
            .then(shops => {
                if (shops) {
                    shops.forEach(shop => {
                        const option = document.createElement('option');
                        option.textContent = shop.name; // Отображаем название магазина
                        storeSelect.appendChild(option);
                    });
                }
            })

        // Кнопка удаления строки (крестик)
        const removeButton = document.createElement('button');
        removeButton.className = 'remove-supply-button';
        removeButton.textContent = '×';
        removeButton.addEventListener('click', () => {
            supplyRow.remove();
        });

        // Добавляем элементы в строку
        supplyRow.appendChild(qrInput);
        supplyRow.appendChild(storeSelect);
        supplyRow.appendChild(removeButton);

        supplyContainer.appendChild(supplyRow);
    }

    const addSupplyButton = document.createElement('button');
    addSupplyButton.textContent = '+ добавить WB поставку';
    addSupplyButton.className = 'add-supply-button';
    addSupplyButton.addEventListener('click', addSupplyRow);

    const yandexContainer = document.createElement('div');
    yandexContainer.className = 'yandex-container';

    const addYandexButton = document.createElement('button');
    addYandexButton.textContent = '+ добавить дату поставки YANDEX';
    addYandexButton.className = 'add-yandex-button';
    addYandexButton.addEventListener('click', () => {
        const yandexLabel = document.createElement('label');
        yandexLabel.textContent = 'Дата поставки Yandex Market:';
        const yandexInput = document.createElement('input');
        yandexInput.type = 'date';
        yandexInput.className = 'input-not-role';
        yandexInput.id = 'yandexTo';

        yandexContainer.innerHTML = '';
        yandexContainer.appendChild(yandexLabel);
        yandexContainer.appendChild(yandexInput);
    });

    yandexContainer.appendChild(addYandexButton);

    const generateButton = document.createElement('button');
    generateButton.textContent = 'Сформировать';
    generateButton.className = 'generate-report-button';
    generateButton.addEventListener('click', async () => {
        const fromDate = fromDateInput.value;
        const toDate = toDateInput.value;
        const yandexInput = document.getElementById('yandexTo');
        const yandexToDate = yandexInput ? yandexInput.value : null;

        if (!fromDate || !toDate) {
            alert('Пожалуйста, заполните обе даты.');
            return;
        }

        // Получаем данные поставок
        const supplies = [];
        let isValidWbQrCodes = true;

        document.querySelectorAll('.supply-row').forEach(row => {
            const supplyId = row.querySelector('input').value.trim();
            const shopName = row.querySelector('select').value;

            if (!supplyId || shopName === '') {
                isValidWbQrCodes = false;
                row.querySelector('input').classList.add('error'); // Подсветим поле
            } else {
                row.querySelector('input').classList.remove('error');
            }

            supplies.push({supplyId: supplyId, shopName: shopName});
        });

        if (!isValidWbQrCodes) {
            alert('Пожалуйста, заполните все QR-коды поставок и магазины.');
            return;
        }

        const removeSupplyButtons = document.getElementsByClassName('remove-supply-button');

        Array.from(removeSupplyButtons).forEach(button => {
            button.classList.add('hidden');
        });

        addSupplyButton.classList.add('hidden')
        addYandexButton.classList.add('hidden')
        generateButton.classList.add('hidden')

        // Показываем лоадер
        loader.classList.remove('hidden');
        successMessage.classList.add('hidden');

        // Вызов функции для получения zip-архива
        try {
            await getPosting(fromDate, toDate, yandexToDate, supplies, signal);
            successMessage.classList.remove('hidden');
        } finally {
            // Убираем лоадер
            loader.classList.add('hidden');
        }
    });

    modalContent.appendChild(closeButton);
    modalContent.appendChild(title);
    modalContent.appendChild(fromDateLabel);
    modalContent.appendChild(fromDateInput);
    modalContent.appendChild(toDateLabel);
    modalContent.appendChild(toDateInput);
    modalContent.appendChild(yandexContainer);
    modalContent.appendChild(addSupplyButton);
    modalContent.appendChild(supplyContainer);
    modalContent.appendChild(generateButton);
    modalContent.appendChild(loader);
    modalContent.appendChild(successMessage);

    modalOverlay.appendChild(modalContent);
    document.body.appendChild(modalOverlay);
}

export async function findShopsByMarketplaceName(marketplaceName) {
    var token = checkTokenExpirationAndGet();
    const response = await fetch(`/api/v1/shops/${encodeURIComponent(marketplaceName)}`, {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`,
        }
    });

    if (!response.ok) {
        const errorData = await response.json();
        alert('Ошибка: ' + errorData.message);
        return null;
    }

    return await response.json();
}