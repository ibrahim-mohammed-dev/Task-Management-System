package com.demo.service;

import com.demo.dto.LoginRequestDto;
import com.demo.dto.RegisterRequestDto;
import com.demo.model.User;
import com.demo.repository.UserRepository;
import com.demo.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }
    public User mapToEntity(RegisterRequestDto dto) {
        return new User(
                dto.username(),
                dto.email(),
                passwordEncoder.encode(dto.password())
        );
    }
    public User register(RegisterRequestDto dto){
        if (userRepository.existsByUsername(dto.username())){
            throw new RuntimeException("Username is already taken!");
        }
        if (userRepository.existsByEmail(dto.email())){
            throw new RuntimeException("Email is already registered!");
        }
        User user = mapToEntity(dto);
        return userRepository.save(user);
    }
    public String login(LoginRequestDto dto){
        authenticationManager.authenticate
                (new UsernamePasswordAuthenticationToken
                        (dto.getUsername(), dto.getPassword()));
        return jwtUtils.generateToken(dto.getUsername());
    }
}

