package com.demo.security;

import com.demo.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtils = jwtUtils;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. استخراج الـ Token من الهيدر بتاع الـ Request
            String jwt = parseJwt(request);

            // 2. فحص التوكن للتأكد من صحته
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // استخراج اسم المستخدم من التوكن
                String username = jwtUtils.getUsernameFromJwtToken(jwt);

                // 3. عمل كائن Authentication يمثل المستخدم الحالي
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                //بتجيب بيانات زيادة زي ip address و session id لو الحالة stateless هتكون null
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. حفظ المستخدم جوه الـ SecurityContext (كده Spring Security عرف إن الـ Request ده صاحب حق)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }

        // 5. تمرير الطلب للـ Filter اللي بعده أو للـ Controller
        filterChain.doFilter(request, response);
    }

    // ميثود مساعدة لقص التوكن من الهيدر (Authorization: Bearer <TOKEN>)
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // قص كلمة Bearer مع المسافة
        }

        return null;
    }
}