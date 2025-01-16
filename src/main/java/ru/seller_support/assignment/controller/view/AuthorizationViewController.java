package ru.seller_support.assignment.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AuthorizationViewController {

    @GetMapping(path = "/login")
    public String login() {
        return "login";
    }
}
