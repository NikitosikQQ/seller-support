package ru.seller_support.assignment.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import ru.seller_support.assignment.adapter.postgres.entity.UserEntity;
import ru.seller_support.assignment.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private static final String ROLE_PREFIX = "ROLE_";

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity user = userService.findUserByUsername(username);
        return UserDetailsImpl.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(getAuthorities(user))
                .build();
    }

    private List<SimpleGrantedAuthority> getAuthorities(UserEntity user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(String.format(ROLE_PREFIX + role.getName())))
                .toList();
    }
}
