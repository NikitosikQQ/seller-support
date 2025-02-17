package ru.seller_support.assignment.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.seller_support.assignment.util.SecurityUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtSecurityFilter extends OncePerRequestFilter {

    private static final int INDEX_START_TOKEN = 7;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String jwt = null;
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (Objects.nonNull(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt = headerAuth.substring(INDEX_START_TOKEN);
        }
        if (Objects.nonNull(jwt)) {
            String username = jwtService.getUsernameFromToken(jwt);
            if (Objects.nonNull(username) && Objects.isNull(SecurityUtils.getAuthentication())) {
                UserDetails user = userDetailsService.loadUserByUsername(username);
                List<SimpleGrantedAuthority> roles = jwtService.getRolesFromToken(jwt).stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, roles);
                SecurityUtils.setAuthentication(authentication);

            }
        }
        filterChain.doFilter(request, response);
    }
}
