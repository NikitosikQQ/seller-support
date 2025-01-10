package ru.seller_support.assignment.config.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.seller_support.assignment.util.SecurityUtils;

import java.util.Date;

@Component
public class JwtService {

    private static final String AUTHORITIES_KEY = "authorities";

    @Value("${app.security.secret}")
    private String secret;

    @Value("${app.security.lifetime}")
    private int lifetimeMs;

    public String generateToken(Authentication auth) {
        UserDetails userDetails = SecurityUtils.getUserDetails();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + lifetimeMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .claim(AUTHORITIES_KEY, userDetails.getAuthorities())
                .compact();
    }

    public String getUsernameFromToken(String jwt) {
        return Jwts.parser()
                .setSigningKey(secret)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }
}
