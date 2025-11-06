package ru.seller_support.assignment.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import ru.seller_support.assignment.config.security.UserDetailsImpl;

import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SecurityUtils {

    public static void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static UserDetails getUserDetails() {
        Authentication authentication = getAuthentication();
        return (UserDetails) authentication.getPrincipal();
    }

    public static String getCurrentUsername() {
        return ((UserDetailsImpl) getAuthentication().getPrincipal()).getUsername();
    }

    public static Set<String> getRoles() {
        Authentication authentication = getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replaceFirst("^ROLE_", ""))
                .collect(Collectors.toSet());
    }
}
