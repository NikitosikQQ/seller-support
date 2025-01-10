package ru.seller_support.assignment.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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

    public static String getLogin() {
        Authentication authentication = getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).getUsername();
    }
}
