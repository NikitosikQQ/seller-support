package ru.seller_support.assignment.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.controller.dto.request.SignInRequest;
import ru.seller_support.assignment.controller.dto.response.JwtTokenResponse;
import ru.seller_support.assignment.service.AuthorizationService;

@RestController
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    @PostMapping(path = "/auth",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public JwtTokenResponse login(@RequestBody SignInRequest request) {
        String token = authorizationService.authorize(request);
        return JwtTokenResponse.builder()
                .token(token)
                .build();
    }
}
