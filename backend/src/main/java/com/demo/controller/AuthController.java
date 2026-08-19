package com.demo.controller;

import com.demo.dto.LoginRequestDto;
import com.demo.dto.RegisterRequestDto;
import com.demo.model.User;
import com.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController
{
    private final AuthService service;
    @Autowired
    public AuthController(AuthService service) {
        this.service = service;
    }
    @PostMapping ("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDto requestDto) {
        service.register(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginDto){
        return ResponseEntity.ok(service.login(loginDto));
    }
}
