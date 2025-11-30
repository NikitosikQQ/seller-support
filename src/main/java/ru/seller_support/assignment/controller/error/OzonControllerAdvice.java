package ru.seller_support.assignment.controller.error;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackResponse;


@ControllerAdvice(basePackages = "ru.seller_support.assignment.controller.callback.ozon")
@RequiredArgsConstructor
@Slf4j
public class OzonControllerAdvice {

    private final ErrorResolver errorResolver;

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<OzonCallbackResponse> handleValidationException(ValidationException ex) {
        log.warn("Не пройдена валидация колбэка от ozon: {}", ex.getMessage(), ex);
        OzonCallbackResponse body = errorResolver.resolveOzonError(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OzonCallbackResponse> handleGeneralException(Exception ex) {
        log.error("Ошибка при обработке колбэка от ozon: {}", ex.getMessage(), ex);
        OzonCallbackResponse body = errorResolver.resolveOzonError(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
