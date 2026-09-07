package com.demo.service;

import com.demo.dto.AuthResponseDto;
import com.demo.exception.TokenRefreshException;
import com.demo.model.RefreshToken;
import com.demo.model.User;
import com.demo.repository.RefreshTokenRepository;
import com.demo.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService
{

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;
    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    //helper method for hashing the token
    private String hashToken(String rawtoken)
    {
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = hasher.digest(rawtoken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        }
        catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 Algorithm is not available" ,e);
        }
    }
    @Transactional
    public String createRefreshToken(User user)
    {
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();
        String rawToken = UUID.randomUUID().toString();
        String hashedToken =hashToken(rawToken);
        RefreshToken refreshToken = RefreshToken.create(user, hashedToken, refreshExpirationMs);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }
    @Transactional
    public RefreshToken verifyRefreshToken(RefreshToken token)
    {
         if (token.getExpiryDate().isBefore(Instant.now())){
             refreshTokenRepository.delete(token);
             throw new TokenRefreshException("Refresh Token was expired");
         }
         return token;
    }
    @Transactional
    public AuthResponseDto rotateRefreshToken(String rawRefreshToken)
    {
        String hashedToken = hashToken(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByHashedToken(hashedToken)
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));
        verifyRefreshToken(refreshToken);
        User user = refreshToken.getUser();
        refreshTokenRepository.delete(refreshToken);
        AuthResponseDto dto =new AuthResponseDto(jwtUtils.generateToken(user) ,createRefreshToken(user));
        return dto;
    }
    @Transactional
    public int deleteByUser(User user)
    {
            return refreshTokenRepository.deleteByUser(user);
    }
    @Transactional
    public int deleteByUsername(String username)
    {
            return refreshTokenRepository.deleteByUserUsername(username);
    }
}
