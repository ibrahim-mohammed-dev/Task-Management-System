package com.demo.security;

import com.demo.model.Group;
import com.demo.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    // المفتاح السري لتشفير الـ Token (مكون من 256 بت على الأقل)
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // 1. توليد Token باسم المستخدم
    public String generateToken(User user) {
        // استخراج الصلاحيات
        List<String> permissions = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // استخراج الجروبات
        List<String> groups = user.getGroups().stream()
                .map(Group::getName)
                .toList();

        return Jwts.builder()
                .subject(user.getUsername())      // اسم المستخدم
                .claim("permissions", permissions) // الـ Custom Claim للصلاحيات
                .claim("groups", groups)           // الـ Custom Claim للجروبات
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // 2. استخراج اسم المستخدم من الـ Token
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 3. التحقق من صحة الـ Token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}