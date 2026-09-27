package com.voting.online_voting_system.dto;

import com.voting.online_voting_system.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private Long id;

    private String studentId;

    private String firstName;

    private String lastName;

    private String email;

    private Role role;
}