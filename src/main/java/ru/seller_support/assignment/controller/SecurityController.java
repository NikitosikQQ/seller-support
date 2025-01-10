package ru.seller_support.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.controller.dto.SignInRequest;
import ru.seller_support.assignment.controller.dto.SignUpRequest;
import ru.seller_support.assignment.service.AuthorizationService;
import ru.seller_support.assignment.service.UserService;

@RestController
@RequiredArgsConstructor
public class SecurityController {

    private final UserService userService;
    private final AuthorizationService authorizationService;

    @PostMapping(path = "/signup")
    public void signup(@RequestBody SignUpRequest request) {
        userService.saveUser(request);
    }

    @PostMapping(path = "/login")
    public String login(@RequestBody SignInRequest request) {
       return authorizationService.authorize(request);
    }
}
