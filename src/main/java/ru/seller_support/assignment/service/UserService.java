package ru.seller_support.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.seller_support.assignment.adapter.postgres.entity.RoleEntity;
import ru.seller_support.assignment.adapter.postgres.entity.UserEntity;
import ru.seller_support.assignment.adapter.postgres.repository.RoleRepository;
import ru.seller_support.assignment.adapter.postgres.repository.UserRepository;
import ru.seller_support.assignment.controller.dto.request.CreateUserRequest;
import ru.seller_support.assignment.controller.dto.request.DeleteUserRequest;
import ru.seller_support.assignment.controller.dto.request.UserChangeRequest;
import ru.seller_support.assignment.exception.RoleChangeException;
import ru.seller_support.assignment.exception.UserChangeException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional
    public void saveUser(CreateUserRequest request) {
        checkUsernameIsNotUnique(request.getUsername());
        UserEntity user = new UserEntity();
        Set<RoleEntity> roles = findRolesByNames(request.getRoles());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);
        userRepository.save(user);
    }

    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    public void updateUser(UserChangeRequest request) {
        checkUsernameIsNotUnique(request.getUpdatedUsername());
        UserEntity user = findUserByUsername(request.getUsername());
        Set<RoleEntity> roles = findRolesByNames(request.getUpdatedRoles());
        if (Objects.nonNull(request.getUpdatedRoles()) && !request.getUpdatedRoles().isEmpty()) {
            user.getRoles().clear();
            user.getRoles().addAll(roles);
        }
        if (Objects.nonNull(request.getUpdatedUsername())) {
            user.setUsername(request.getUpdatedUsername());
        }
        userRepository.save(user);
    }

    public List<String> getAllRoleNames() {
        return roleRepository.findAll()
                .stream()
                .map(RoleEntity::getName)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserEntity findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Нет пользователя с логином %s", username)));
    }

    private Set<RoleEntity> findRolesByNames(List<String> roleNames) {
        List<RoleEntity> roles = roleRepository.findByNameIn(roleNames);
        if (roles.isEmpty()) {
            throw new RoleChangeException(String.format("Данные роли не были найдены: %s",
                    Arrays.toString(roles.toArray())));
        }
        return Set.copyOf(roles);
    }

    @Transactional
    public void deleteUser(DeleteUserRequest request) {
        UserEntity user = findUserByUsername(request.getUsername());
        userRepository.deleteById(user.getId());
    }

    private void checkUsernameIsNotUnique(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserChangeException("Username = " + username + " уже используется");
        }
    }
}
