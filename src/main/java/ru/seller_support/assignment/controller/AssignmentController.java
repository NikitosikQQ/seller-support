package ru.seller_support.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.seller_support.assignment.util.SecurityUtils;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssignmentController {

    @GetMapping("/me")
    public String getUserInfo() {
        return SecurityUtils.getLogin();
    }
}
