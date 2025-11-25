package com.rakesh.taskmanagement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String  jwtSecretBase64;

    @Value("${jwt.expiration}")
    private Long expirationTime;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretBase64.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .issuedAt(new Date(System.currentTimeMillis()))
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean validateToken(String token, String username) {
        try {
           return extractUsername(token).equals(username) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
