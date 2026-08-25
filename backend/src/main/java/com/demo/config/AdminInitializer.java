package com.demo.config;

import com.demo.model.Role;
import com.demo.model.User;
import com.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByUsername("admin")) {

            User admin = new User(
                    "admin",
                    "admin@example.com",
                    passwordEncoder.encode("admin123")
            );

            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
        }
    }
}