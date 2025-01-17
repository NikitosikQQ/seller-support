package ru.seller_support.assignment.controller.error;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.seller_support.assignment.controller.error.dto.ErrorResponse;
import ru.seller_support.assignment.exception.RoleChangeException;
import ru.seller_support.assignment.exception.UserChangeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalControllerAdvice {

    private final ErrorResolver errorResolver;

    @ExceptionHandler({BadCredentialsException.class, ExpiredJwtException.class,
            UsernameNotFoundException.class, SignatureException.class})
    public ResponseEntity<ErrorResponse> handleAuthorizationException(Exception ex) {
        log.warn("Ошибка авторизации: {}", ex.getMessage(), ex);
        ErrorResponse body = errorResolver.resolve(ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler({RoleChangeException.class})
    public ResponseEntity<ErrorResponse> handleException(RoleChangeException ex) {
        log.warn("Ошибка при работе с ролями: {}", ex.getMessage(), ex);
        ErrorResponse body = errorResolver.resolve(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler({UserChangeException.class})
    public ResponseEntity<ErrorResponse> handleException(UserChangeException ex) {
        log.warn("Ошибка при работе с пользователями: {}", ex.getMessage(), ex);
        ErrorResponse body = errorResolver.resolve(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error(ex.getMessage(), ex);
        ErrorResponse body = errorResolver.resolve(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // Обработка ошибок валидации для параметров метода
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        // Проходим по всем ошибкам валидации
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Ошибка валидации: {}", errors, ex);
        ErrorResponse body = errorResolver.resolveValidation(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // Обработка исключений при нарушении ограничений, таких как @NotNull и т.д.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        log.warn("Ошибка валидации: {}", errors, ex);

        ErrorResponse body = errorResolver.resolveValidation(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
