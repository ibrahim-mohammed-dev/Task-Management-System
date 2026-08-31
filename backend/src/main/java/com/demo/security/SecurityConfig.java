package com.demo.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. تعطيل حماية CSRF لأننا بنستعمل JWT ومش بنعتمد على Cookies
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. ضبط السياسة لتكون Stateless (لا يتم إنشاء Session على السيرفر)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 3. تحديد الصلاحيات والروابط المسموحة
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // روابط التسجيل والـ Login مفتوحة للجميع
                        .anyRequest().authenticated()              // أي رابط تاني محتاج Authentication (توكن سليم)
                )
                // 4.بقول لسبرينج لما يحصل authentication error، استخدم الـ object ده عشان تتعامل معاه
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint))

                // 5. إقحام الفلتر الخاص بنا قبل الفلتر الأساسي لـ Spring
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // Bean لتشفير كلمات المرور باستخدام BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean للـ AuthenticationManager لتشغيل عملية الـ Login في الـ AuthController
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // رابط الفرونت إند (Vite / React)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // الـ HTTP Methods التي يرسلها الفرونت
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // الـ Headers المسموح للفرونت إرسالها (خاصة Authorization للـ JWT)
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));

        // السماح بقرائة الـ Headers من الـ Response
        configuration.setExposedHeaders(List.of("Authorization"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}