package ru.seller_support.assignment.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.seller_support.assignment.adapter.postgres.entity.RoleEntity;
import ru.seller_support.assignment.adapter.postgres.entity.UserEntity;
import ru.seller_support.assignment.controller.dto.request.user.CreateUserRequest;
import ru.seller_support.assignment.controller.dto.request.user.DeleteUserRequest;
import ru.seller_support.assignment.controller.dto.request.user.UserChangeRequest;
import ru.seller_support.assignment.controller.dto.response.UserResponse;
import ru.seller_support.assignment.domain.enums.Workplace;
import ru.seller_support.assignment.service.UserService;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Slf4j
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path = "/users")
    public List<UserResponse> getUsers() {
        List<UserEntity> users = userService.findAllUsers();
        return users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .roles(user.getRoles().stream()
                                .map(RoleEntity::getName)
                                .toList())
                        .workplaces(Objects.isNull(user.getWorkplaces())
                                ? null
                                : user.getWorkplaces().stream()
                                    .map(Workplace::getValue)
                                    .toList())
                        .build())
                .toList();
    }

    @GetMapping(path = "/users/{username}/workplaces")
    public List<String> getUserWorkplaces(@PathVariable String username) {
        UserEntity user = userService.findUserByUsername(username);
        log.info("Получение рабочих мест...");
        return Objects.isNull(user.getWorkplaces())
                ? null
                : user.getWorkplaces().stream()
                .map(Workplace::getValue)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(path = "/users")
    public void createUser(@RequestBody @Valid CreateUserRequest request) {
        userService.saveUser(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/users")
    public void deleteUser(@RequestBody DeleteUserRequest request) {
        userService.deleteUser(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(path = "/users")
    public void updateUser(@RequestBody @Valid UserChangeRequest request) {
        userService.updateUser(request);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles")
    public List<String> getAllRoles() {
        return userService.getAllRoleNames();
    }
}
