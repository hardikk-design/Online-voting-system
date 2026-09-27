package com.voting.online_voting_system.security;

import com.voting.online_voting_system.service.CustomUserDetailsService;
import com.voting.online_voting_system.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println(
                "========== JWT FILTER =========="
        );

        System.out.println(
                "Request = "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
        );

        String authHeader =
                request.getHeader("Authorization");

        System.out.println(
                "Authorization header present = "
                        + (authHeader != null)
        );

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            System.out.println(
                    "NO BEARER TOKEN"
            );

            System.out.println(
                    "================================"
            );

            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        // DEBUG: Show exactly what Spring receives
        System.out.println(
                "AUTH HEADER = " + authHeader
        );

        System.out.println(
                "EXTRACTED JWT = " + jwt
        );

        // DEBUG: Count dots in the JWT.
        // A normal signed JWT must contain exactly 2 dots.
        long dotCount = jwt.chars()
                .filter(ch -> ch == '.')
                .count();

        System.out.println(
                "JWT DOT COUNT = " + dotCount
        );

        System.out.println(
                "JWT LENGTH = " + jwt.length()
        );

        try {

            String email =
                    jwtService.extractUsername(jwt);

            System.out.println(
                    "JWT email = " + email
            );

            if (email != null &&
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(email);

                System.out.println(
                        "User loaded = "
                                + userDetails.getUsername()
                );

                System.out.println(
                        "Authorities from JWT filter = "
                                + userDetails.getAuthorities()
                );

                if (jwtService.isTokenValid(
                        jwt,
                        userDetails)) {

                    System.out.println(
                            "JWT VALID"
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );

                    System.out.println(
                            "Authentication set successfully"
                    );

                } else {

                    System.out.println(
                            "JWT INVALID"
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "JWT ERROR = "
                            + e.getMessage()
            );
        }

        System.out.println(
                "================================"
        );

        filterChain.doFilter(request, response);
    }
}