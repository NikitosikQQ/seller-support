package ru.seller_support.assignment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.adapter.postgres.entity.RoleEntity;
import ru.seller_support.assignment.adapter.postgres.entity.UserEntity;
import ru.seller_support.assignment.controller.dto.request.CreateUserRequest;
import ru.seller_support.assignment.controller.dto.request.DeleteUserRequest;
import ru.seller_support.assignment.controller.dto.request.UserChangeRequest;
import ru.seller_support.assignment.controller.dto.response.UserResponse;
import ru.seller_support.assignment.service.UserService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping(path = "/users")
    public List<UserResponse> createUser() {
        List<UserEntity> users = userService.findAllUsers();
        return users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .roles(user.getRoles().stream().map(RoleEntity::getName).toList())
                        .build())
                .toList();
    }

    @PostMapping(path = "/users")
    public void createUser(@RequestBody @Valid CreateUserRequest request) {
        userService.saveUser(request);
    }

    @DeleteMapping(path = "/users")
    public void deleteUser(@RequestBody DeleteUserRequest request) {
        userService.deleteUser(request);
    }

    @PutMapping(path = "/users")
    public void updateUser(@RequestBody @Valid UserChangeRequest request) {
        userService.updateUser(request);
    }

    @GetMapping("/roles")
    public List<String> getAllRoles() {
        return userService.getAllRoleNames();
    }
}
