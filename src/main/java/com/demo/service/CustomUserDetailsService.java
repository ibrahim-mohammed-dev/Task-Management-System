package com.demo.service;

import com.demo.model.User;
import com.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // البحث عن المستخدم في الداتابيز بواسطة اسم المستخدم
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        // إرجاع كائن User الخاص بنا مباشرةً — فهو يُحقق UserDetails عبر getAuthorities()
        // هذا يضمن أن @AuthenticationPrincipal يُحلَّل للنوع الصحيح في الـ Controllers
        return user;
    }
}