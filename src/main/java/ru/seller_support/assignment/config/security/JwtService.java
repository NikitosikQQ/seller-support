package ru.seller_support.assignment.config.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.seller_support.assignment.util.SecurityUtils;

import java.time.Duration;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {

    private static final String ROLES_KEY = "roles";

    private static final long TWELVE_HOURS_PER_MILLIS = 43200000;

    @Value("${app.security.secret}")
    private String secret;

    @Value("${app.security.lifetime}")
    private Duration lifetime;

    public String generateToken() {
        UserDetails user = SecurityUtils.getUserDetails();

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiryDate = new Date(roles.contains("ROLE_EMPLOYEE")
                ? now.getTime() + lifetime.toMillis() + TWELVE_HOURS_PER_MILLIS
                : now.getTime() + lifetime.toMillis());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .claim(ROLES_KEY, roles)
                .compact();
    }

    public String getUsernameFromToken(String jwt) {
        return getClaimsFromToken(jwt).getSubject();
    }

    public List<String> getRolesFromToken(String jwt) {
        Object rawRoles = getClaimsFromToken(jwt).get(ROLES_KEY);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.convertValue(rawRoles, new TypeReference<>() {});
    }

    public Claims getClaimsFromToken(String jwt) {
        return Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }
}
