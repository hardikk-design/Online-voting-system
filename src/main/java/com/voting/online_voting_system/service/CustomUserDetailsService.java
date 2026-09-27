package com.voting.online_voting_system.service;

import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.entity.RolePermission;
import com.voting.online_voting_system.repository.RolePermissionRepository;
import com.voting.online_voting_system.repository.UserRepository;
import com.voting.online_voting_system.repository.ElectionOfficerAssignmentRepository;
import com.voting.online_voting_system.enums.AssignmentStatus;
import com.voting.online_voting_system.enums.Role;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ElectionOfficerAssignmentRepository assignmentRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            RolePermissionRepository rolePermissionRepository,
            ElectionOfficerAssignmentRepository assignmentRepository) {

        this.userRepository = userRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        )
                );

        List<SimpleGrantedAuthority> authorities =
                new ArrayList<>();

        // --------------------------------------------------
        // 1. Add user's normal/base role
        // --------------------------------------------------

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                )
        );

        // --------------------------------------------------
        // 2. Load permissions for user's normal role
        // --------------------------------------------------

        List<RolePermission> rolePermissions =
                rolePermissionRepository.findByRole(
                        user.getRole()
                );

        for (RolePermission rolePermission : rolePermissions) {

            authorities.add(
                    new SimpleGrantedAuthority(
                            rolePermission
                                    .getPermission()
                                    .getName()
                    )
            );
        }

        // --------------------------------------------------
        // 3. Check ACTIVE Election Officer assignment
        // --------------------------------------------------

        boolean activeAssignment =
                assignmentRepository.findByUserIdAndStatus(
                        user.getId(),
                        AssignmentStatus.ACTIVE
                ).isPresent();

        // --------------------------------------------------
        // DEBUG
        // --------------------------------------------------

        System.out.println("=================================");
        System.out.println("EO DEBUG");
        System.out.println("User ID = " + user.getId());
        System.out.println("User Email = " + user.getEmail());
        System.out.println("User Role = " + user.getRole());
        System.out.println(
                "Active EO Assignment = " + activeAssignment
        );
        System.out.println("=================================");

        // --------------------------------------------------
        // 4. Add Election Officer permissions
        // --------------------------------------------------

        if (activeAssignment) {

            List<RolePermission> eoPermissions =
                    rolePermissionRepository.findByRole(
                            Role.ELECTION_OFFICER
                    );

            for (RolePermission rolePermission : eoPermissions) {

                String permissionName =
                        rolePermission
                                .getPermission()
                                .getName();

                authorities.add(
                        new SimpleGrantedAuthority(
                                permissionName
                        )
                );

                System.out.println(
                        "Added EO permission = "
                                + permissionName
                );
            }
        }

        // --------------------------------------------------
        // 5. Print final authorities
        // --------------------------------------------------

        System.out.println(
                "FINAL AUTHORITIES = " + authorities
        );

        System.out.println("=================================");

        // --------------------------------------------------
        // 6. Create Spring Security UserDetails
        // --------------------------------------------------

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}