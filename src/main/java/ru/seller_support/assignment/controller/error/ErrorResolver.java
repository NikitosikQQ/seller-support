package ru.seller_support.assignment.controller.error;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.seller_support.assignment.controller.error.dto.ErrorResponse;

import java.util.Map;

@Component
public class ErrorResolver {

    private static final String AUTH_ERROR_CODE = "AUTH_ERROR";
    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

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

    public ErrorResponse resolveValidation(Map<String, String> messages) {
        ErrorResponse response = new ErrorResponse();
        response.setErrorCode(VALIDATION_ERROR_CODE);
        response.setMessage(messages.toString());
        return response;
    }
}
