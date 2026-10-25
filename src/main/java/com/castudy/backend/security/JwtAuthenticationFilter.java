package com.castudy.backend.security;

import com.castudy.backend.entity.Student;
import com.castudy.backend.repository.StudentRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final StudentRepository repository;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   StudentRepository repository) {
        this.jwtService = jwtService;
        this.repository = repository;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("===== Jwt Filter Called =====");

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        String username = jwtService.extractUsername(jwt);

        if (username != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            Student student = repository.findByEmail(username)
                    .orElse(null);

            System.out.println("===== JWT DEBUG =====");
            System.out.println("Username: " + username);
            System.out.println("Student found: " + (student != null));

            if (student != null) {
                System.out.println("Role: " + student.getRole());
                System.out.println(
                        "Token valid: " +
                                jwtService.isTokenValid(jwt, student)
                );
            }

            if (student != null && jwtService.isTokenValid(jwt, student)) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                student,
                                null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority(
                                                student.getRole().name()
                                        )
                                )
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                System.out.println(
                        "Authentication set: " +
                                SecurityContextHolder.getContext()
                                        .getAuthentication()
                );

                System.out.println(
                        "Authorities: " +
                                SecurityContextHolder.getContext()
                                        .getAuthentication()
                                        .getAuthorities()
                );
            }
        }

        filterChain.doFilter(request, response);
    }
}