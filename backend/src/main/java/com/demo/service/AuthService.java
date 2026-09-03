package com.demo.service;

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
        user.getGroups().add(defaultGroup);

        defaultGroup.getUsers().add(user);
        return userRepository.save(user);
    }
    public String login(LoginRequestDto dto){
        authenticationManager.authenticate
                (new UsernamePasswordAuthenticationToken
                        (dto.username(), dto.password()));
        return jwtUtils.generateToken(dto.username());
    }
}

