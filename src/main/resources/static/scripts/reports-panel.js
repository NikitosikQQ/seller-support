import {checkTokenExpirationAndGet} from "./panel.js";

export async function getPosting(from, to, wbSupplyId, signal) {
    const token = checkTokenExpirationAndGet();
    const response = await fetch(`/api/v1/reports/postings?from=${from}&to=${to}&supplyId=${wbSupplyId}`, {
        method: 'GET',
        signal,
        headers: {
            'Authorization': `Bearer ${token}`,
        },
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

    // Создаем модальное окно
    const modalContent = document.createElement('div');
    modalContent.className = 'modal-content';

    // Создаем AbortController для управления запросом
    const controller = new AbortController();
    const signal = controller.signal;

    // Кнопка закрытия
    const closeButton = document.createElement('button');
    closeButton.className = 'close-button';
    closeButton.textContent = '×';
    closeButton.addEventListener('click', () => {
        // Прерываем запрос
        controller.abort();
        document.body.removeChild(modalOverlay);
    });

    // Заголовок модального окна
    const title = document.createElement('h3');
    title.textContent = 'Фильтр по отправлениям';

    const fromDateLabel = document.createElement('label');
    fromDateLabel.textContent = 'Начало периода даты принятия в обработку:';
    const fromDateInput = document.createElement('input');
    fromDateInput.type = 'date';
    fromDateInput.className = 'input-not-role';

    const toDateLabel = document.createElement('label');
    toDateLabel.textContent = 'Конец периода принятия в обработку:';
    const toDateInput = document.createElement('input');
    toDateInput.type = 'date';
    toDateInput.className = 'input-not-role';

    const wbSupplyIdLabel = document.createElement('label');
    wbSupplyIdLabel.textContent = 'QR-Код поставки WB:';
    const wbSupplyIdInput = document.createElement('input');
    wbSupplyIdInput.type = 'text';
    wbSupplyIdInput.required = false;
    wbSupplyIdInput.className = 'input-not-role';

    // Лоадер
    const loader = document.createElement('div');
    loader.className = 'loader hidden'; // Скрыт по умолчанию

    // Сообщение об успехе
    const successMessage = document.createElement('p');
    successMessage.className = 'success-message hidden';
    successMessage.textContent = 'Документы успешно сформированы';

    const generateButton = document.createElement('button');
    generateButton.textContent = 'Сформировать';
    generateButton.className = 'create-report-button';
    generateButton.addEventListener('click', async () => {
        const fromDate = fromDateInput.value;
        const toDate = toDateInput.value;
        const wbSupplyId = wbSupplyIdInput.value || '';

        if (!fromDate || !toDate) {
            alert('Пожалуйста, заполните обе даты.');
            return;
        }

        // Показываем лоадер
        loader.classList.remove('hidden');
        successMessage.classList.add('hidden');

        // Вызов функции для получения zip-архива
        try {
            await getPosting(fromDate, toDate, wbSupplyId, signal);
            successMessage.classList.remove('hidden');
        } finally {
            // Убираем лоадер
            loader.classList.add('hidden');
        }
    });

    // Добавляем элементы в модальное окно
    modalContent.appendChild(closeButton);
    modalContent.appendChild(title);
    modalContent.appendChild(fromDateLabel);
    modalContent.appendChild(fromDateInput);
    modalContent.appendChild(toDateLabel);
    modalContent.appendChild(toDateInput);
    modalContent.appendChild(wbSupplyIdLabel);
    modalContent.appendChild(wbSupplyIdInput);
    modalContent.appendChild(generateButton);
    modalContent.appendChild(loader);
    modalContent.appendChild(successMessage);

    // Добавляем затемнение и модальное окно в DOM
    modalOverlay.appendChild(modalContent);
    document.body.appendChild(modalOverlay);
}