package ru.seller_support.assignment.controller.error;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.seller_support.assignment.controller.error.dto.ErrorResponse;
import ru.seller_support.assignment.exception.UserChangeException;
import ru.seller_support.assignment.exception.RoleChangeException;

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
}
