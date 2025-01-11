package ru.seller_support.assignment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.controller.dto.request.CreateUserRequest;
import ru.seller_support.assignment.controller.dto.request.AddRolesRequest;
import ru.seller_support.assignment.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @PostMapping(path = "/users")
    public void createUser(@RequestBody CreateUserRequest request) {
        userService.saveUser(request);
    }

    @PatchMapping(path = "/users/roles")
    public void addRolesToUser(@RequestBody AddRolesRequest request) {
        userService.addRolesToUser(request);
    }


    @GetMapping("/roles")
    public List<String> getAllRoles() {
        return userService.getAllRoleNames();
    }
}
