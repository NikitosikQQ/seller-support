package ru.seller_support.assignment.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.seller_support.assignment.adapter.postgres.entity.UserEntity;
import ru.seller_support.assignment.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminViewController {

    private final UserService userService;

    @GetMapping
    public String adminView(Model model) {
        List<UserEntity> users = userService.findAllUsers();
        model.addAttribute("message", users);
        return "admin";
    }
}
