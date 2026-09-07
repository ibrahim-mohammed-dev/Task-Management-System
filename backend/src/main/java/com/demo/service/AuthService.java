package com.demo.service;

import com.demo.dto.AuthResponseDto;
import com.demo.dto.LoginRequestDto;
import com.demo.dto.RegisterRequestDto;
import com.demo.exception.DuplicateResourceException;
import com.demo.exception.ResourceNotFoundException;
import com.demo.model.Group;
import com.demo.model.User;
import com.demo.repository.GroupRepository;
import com.demo.repository.UserRepository;
import com.demo.security.JwtUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService
{
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public User mapToEntity(RegisterRequestDto dto) {
        return new User(
                dto.username(),
                dto.email(),
                passwordEncoder.encode(dto.password())
        );
    }

    @Transactional
    public User register(RegisterRequestDto dto){
        if (userRepository.existsByUsername(dto.username())){
            throw new DuplicateResourceException("Username is already taken!");
        }
        if (userRepository.existsByEmail(dto.email())){
            throw new DuplicateResourceException("Email is already registered!");
        }
        User user = mapToEntity(dto);
        Group defaultGroup = groupRepository.findByName("USERS")
                .orElseThrow(() -> new ResourceNotFoundException("Default group USERS not found"));

        User savedUser = userRepository.save(user);
        defaultGroup.getUsers().add(savedUser);
        groupRepository.save(defaultGroup);
        return savedUser;
    }

    public AuthResponseDto login(LoginRequestDto dto){
        // 1. إرجاع كائن الـ Authentication
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.username(), dto.password())
        );

        // 2. سحب الـ User من الـ Principal
        User user = (User) authentication.getPrincipal();
        AuthResponseDto dto1 = new AuthResponseDto(jwtUtils.generateToken(user)
                ,refreshTokenService.createRefreshToken(user));
        return dto1;
    }
}