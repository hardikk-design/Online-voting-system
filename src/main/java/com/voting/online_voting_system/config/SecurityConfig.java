package com.voting.online_voting_system.config;

import com.voting.online_voting_system.security.JwtAuthenticationFilter;
import com.voting.online_voting_system.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            CustomUserDetailsService service) {

        return service;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Permission based
                        .requestMatchers(
                                "/api/elections/**"
                        ).hasAuthority("VIEW_ELECTION")

                        .requestMatchers(
                                "/api/votes/**"
                        ).hasAuthority("CAST_VOTE")

                        .requestMatchers(
                                "/api/results/**"
                        ).hasAuthority("VIEW_RESULT")

                        .requestMatchers(
                                "/api/nominations/**"
                        ).hasAuthority("SUBMIT_NOMINATION")

                        // Admin
                        .requestMatchers(
                                "/api/admin/audit-log"
                        ).hasAuthority("VIEW_AUDIT_LOG")

                        .requestMatchers(
                                "/api/admin/election-officers/**"
                        ).hasAuthority(
                                "ASSIGN_ELECTION_OFFICER"
                        )

                        // EO
                        .requestMatchers(
                                "/api/election-officer/elections/**"
                        ).hasAuthority("CREATE_ELECTION")

                        .requestMatchers(
                                "/api/election-officer/nominations/**"
                        ).hasAuthority("VIEW_NOMINATIONS")

                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}