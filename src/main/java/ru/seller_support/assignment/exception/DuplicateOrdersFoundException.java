package ru.seller_support.assignment.exception;

public class DuplicateOrdersFoundException extends RuntimeException {

    public DuplicateOrdersFoundException() {
        super("Найдены дубликаты заказов при их сохранении. Попробуйте позже загрузить заказы");
    }
}