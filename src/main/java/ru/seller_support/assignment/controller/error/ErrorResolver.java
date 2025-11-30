package ru.seller_support.assignment.controller.error;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ValidationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import ru.seller_support.assignment.controller.dto.request.callback.ozon.OzonCallbackResponse;
import ru.seller_support.assignment.controller.dto.request.callback.yandexmarket.YandexMarketCallbackResponse;
import ru.seller_support.assignment.controller.error.dto.ErrorResponse;

import java.util.Map;

@Component
public class ErrorResolver {

    private static final String AUTH_ERROR_CODE = "AUTH_ERROR";
    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

    private static final String OZON_ERROR_UNKNOWN = "ERROR_UNKNOWN";
    private static final String OZON_VALIDATION_ERROR = "ERROR_PARAMETER_VALUE_MISSED";

    private static final String YANDEX_MARKET_UNKNOWN_ERROR = "UNKNOWN";
    private static final String YANDEX_MARKET_VALIDATION_ERROR = "WRONG_EVENT_FORMAT";

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
        if (exception instanceof ValidationException) {
            response.setErrorCode(VALIDATION_ERROR_CODE);
            response.setMessage(exception.getMessage());
        }
        return response;
    }

    public OzonCallbackResponse resolveOzonError(Exception exception) {
        if (exception instanceof ValidationException e) {
            var error = new OzonCallbackResponse.Error();
            error.setCode(OZON_VALIDATION_ERROR);
            error.setMessage(e.getMessage());

            return OzonCallbackResponse.builder()
                    .error(error)
                    .build();
        }

        OzonCallbackResponse.Error error = new OzonCallbackResponse.Error();
        error.setCode(OZON_ERROR_UNKNOWN);
        error.setMessage(exception.getMessage());

        return OzonCallbackResponse.builder()
                .error(error)
                .build();
    }

    public YandexMarketCallbackResponse resolveYandexError(Exception exception) {
        if (exception instanceof ValidationException e) {
            var error = new YandexMarketCallbackResponse.Error();
            error.setType(YANDEX_MARKET_VALIDATION_ERROR);
            error.setMessage(e.getMessage());

            return YandexMarketCallbackResponse.builder()
                    .error(error)
                    .build();
        }

        var error = new YandexMarketCallbackResponse.Error();
        error.setType(YANDEX_MARKET_UNKNOWN_ERROR);
        error.setMessage(exception.getMessage());

        return YandexMarketCallbackResponse.builder()
                .error(error)
                .build();
    }

    public ErrorResponse resolveValidation(Map<String, String> messages) {
        ErrorResponse response = new ErrorResponse();
        response.setErrorCode(VALIDATION_ERROR_CODE);
        response.setMessage(messages.toString());
        return response;
    }
}
