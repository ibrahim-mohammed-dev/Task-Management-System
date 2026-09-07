package com.demo.controller;

import com.demo.dto.AuthResponseDto;
import com.demo.dto.LoginRequestDto;
import com.demo.dto.RefreshTokenRequestDto;
import com.demo.dto.RegisterRequestDto;
import com.demo.model.User;
import com.demo.service.AuthService;
import com.demo.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController
{
    private final AuthService service;
    private final RefreshTokenService refreshTokenService;
    @PostMapping ("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDto requestDto) {
        service.register(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto loginDto){
        return ResponseEntity.ok(service.login(loginDto));
    }
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto requestDto) {
        AuthResponseDto response = refreshTokenService.rotateRefreshToken(requestDto.refreshToken());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal UserDetails currentUser) {
        refreshTokenService.deleteByUsername(currentUser.getUsername());
        return ResponseEntity.ok("Logged out successfully!");
    }
}
