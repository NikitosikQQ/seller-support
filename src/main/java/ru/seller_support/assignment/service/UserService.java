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
import ru.seller_support.assignment.controller.dto.request.AddRolesRequest;
import ru.seller_support.assignment.controller.dto.request.CreateUserRequest;
import ru.seller_support.assignment.controller.dto.request.DeleteRoleRequest;
import ru.seller_support.assignment.exception.RoleChangeException;

import java.util.Arrays;
import java.util.List;
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
        UserEntity user = new UserEntity();
        Set<RoleEntity> roles = findRolesByNames(request.getRoles());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roles);
        userRepository.save(user);
    }

    @Transactional
    public void addRolesToUser(AddRolesRequest request) {
        UserEntity user = findUserByUsername(request.getUsername());
        Set<RoleEntity> roles = findRolesByNames(request.getRoles());
        user.getRoles().addAll(roles);
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
    public void deleteRoleFromUser(DeleteRoleRequest request) {
        UserEntity user = findUserByUsername(request.getUsername());
        RoleEntity roleToDelete = findRolesByNames(List.of(request.getRole())).stream().toList().getFirst();
        Set<RoleEntity> userRoles = user.getRoles();
        boolean userNotHasThatRole = !userRoles.contains(roleToDelete);
        if (userNotHasThatRole) {
            throw new RoleChangeException(String.format("Роль %s отсутствует у пользователя %s",
                    request.getRole(), request.getUsername()));
        }
        userRoles.remove(roleToDelete);
        userRepository.save(user);
    }
}
