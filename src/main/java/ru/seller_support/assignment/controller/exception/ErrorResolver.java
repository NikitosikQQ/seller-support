package ru.seller_support.assignment.controller.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.seller_support.assignment.controller.exception.dto.ErrorResponse;

@Component
public class ErrorResolver {

    private static final String AUTH_ERROR_CODE = "AUTH_ERROR";

    public ErrorResponse resolve(Exception exception) {
        ErrorResponse response = new ErrorResponse(exception);
        if (exception instanceof BadCredentialsException
                || exception instanceof UsernameNotFoundException
                || exception instanceof SignatureException) {
            response.setErrorCode(AUTH_ERROR_CODE);
            response.setMessage("Неверное имя пользователя или пароль");
        }
        if (exception instanceof ExpiredJwtException) {
            response.setErrorCode(AUTH_ERROR_CODE);
            response.setMessage("Токен авторизации просрочился");
        }
        return response;
    }
}
