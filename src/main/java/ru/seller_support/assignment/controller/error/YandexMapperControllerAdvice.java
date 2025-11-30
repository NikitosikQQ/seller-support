package ru.seller_support.assignment.controller.error;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackResponse;


@ControllerAdvice(basePackages = "ru.seller_support.assignment.controller.callback.yandexmarket")
@RequiredArgsConstructor
@Slf4j
public class YandexMapperControllerAdvice {

    private final ErrorResolver errorResolver;

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<YandexMarketCallbackResponse> handleValidationException(ValidationException ex) {
        log.warn("Не пройдена валидация колбэка от yandex-market: {}", ex.getMessage(), ex);
        YandexMarketCallbackResponse body = errorResolver.resolveYandexError(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<YandexMarketCallbackResponse> handleGeneralException(Exception ex) {
        log.error("Ошибка при обработке колбэка от yandex-market: {}", ex.getMessage(), ex);
        YandexMarketCallbackResponse body = errorResolver.resolveYandexError(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
