package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.config.security.JwtService;
import ru.seller_support.assignment.controller.dto.SignInRequest;
import ru.seller_support.assignment.util.SecurityUtils;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public String authorize(SignInRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityUtils.setAuthentication(auth);
        return jwtService.generateToken(auth);
    }
}
