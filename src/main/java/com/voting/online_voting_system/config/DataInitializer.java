package com.voting.online_voting_system.config;

import com.voting.online_voting_system.entity.Permission;
import com.voting.online_voting_system.entity.RolePermission;
import com.voting.online_voting_system.entity.User;
import com.voting.online_voting_system.enums.Role;
import com.voting.online_voting_system.repository.PermissionRepository;
import com.voting.online_voting_system.repository.RolePermissionRepository;
import com.voting.online_voting_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository) {

        return args -> {

            // =========================
            // CREATE PERMISSIONS
            // =========================

            createPermission(
                    permissionRepository,
                    "VIEW_ELECTION",
                    "View available elections"
            );

            createPermission(
                    permissionRepository,
                    "CAST_VOTE",
                    "Cast a vote in an election"
            );

            createPermission(
                    permissionRepository,
                    "VIEW_RESULT",
                    "View election results"
            );

            createPermission(
                    permissionRepository,
                    "SUBMIT_NOMINATION",
                    "Submit a nomination"
            );

            createPermission(
                    permissionRepository,
                    "CREATE_ELECTION",
                    "Create an election"
            );

            createPermission(
                    permissionRepository,
                    "VIEW_NOMINATIONS",
                    "View election nominations"
            );

            createPermission(
                    permissionRepository,
                    "APPROVE_NOMINATION",
                    "Approve a nomination"
            );

            createPermission(
                    permissionRepository,
                    "REJECT_NOMINATION",
                    "Reject a nomination"
            );

            createPermission(
                    permissionRepository,
                    "ASSIGN_ELECTION_OFFICER",
                    "Assign election officers"
            );

            createPermission(
                    permissionRepository,
                    "VIEW_AUDIT_LOG",
                    "View audit logs"
            );


            // =========================
            // ROLE → PERMISSION MAPPING
            // =========================

            // VOTER
            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.VOTER,
                    "VIEW_ELECTION"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.VOTER,
                    "CAST_VOTE"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.VOTER,
                    "VIEW_RESULT"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.VOTER,
                    "SUBMIT_NOMINATION"
            );


            // ELECTION OFFICER
            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ELECTION_OFFICER,
                    "VIEW_ELECTION"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ELECTION_OFFICER,
                    "CAST_VOTE"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ELECTION_OFFICER,
                    "VIEW_RESULT"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ELECTION_OFFICER,
                    "SUBMIT_NOMINATION"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ELECTION_OFFICER,
                    "CREATE_ELECTION"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ELECTION_OFFICER,
                    "VIEW_NOMINATIONS"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ELECTION_OFFICER,
                    "APPROVE_NOMINATION"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ELECTION_OFFICER,
                    "REJECT_NOMINATION"
            );


            // ADMIN
            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ADMIN,
                    "ASSIGN_ELECTION_OFFICER"
            );

            assignPermission(
                    rolePermissionRepository,
                    permissionRepository,
                    Role.ADMIN,
                    "VIEW_AUDIT_LOG"
            );


            // =========================
            // CREATE DEFAULT ADMIN
            // =========================

            boolean adminExists =
                    userRepository.findAll()
                            .stream()
                            .anyMatch(user ->
                                    user.getRole() == Role.ADMIN
                            );

            if (!adminExists) {

                User admin = new User();

                admin.setStudentId("ADMIN001");
                admin.setFirstName("System");
                admin.setLastName("Admin");
                admin.setEmail("admin@voting.com");

                admin.setPassword(
                        passwordEncoder.encode("admin123")
                );

                admin.setDepartment("Administration");
                admin.setCourse("Administration");
                admin.setYear(1);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setRole(Role.ADMIN);

                userRepository.save(admin);

                System.out.println(
                        "Default ADMIN created successfully."
                );
            }
        };
    }


    private void createPermission(
            PermissionRepository repository,
            String name,
            String description) {

        if (repository.findByName(name).isEmpty()) {

            Permission permission = new Permission();

            permission.setName(name);
            permission.setDescription(description);

            repository.save(permission);
        }
    }


    private void assignPermission(
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository,
            Role role,
            String permissionName) {

        Permission permission =
                permissionRepository.findByName(permissionName)
                        .orElseThrow();

        boolean exists =
                rolePermissionRepository
                        .findByRole(role)
                        .stream()
                        .anyMatch(rp ->
                                rp.getPermission()
                                        .getId()
                                        .equals(permission.getId())
                        );

        if (!exists) {

            RolePermission rolePermission =
                    new RolePermission();

            rolePermission.setRole(role);
            rolePermission.setPermission(permission);

            rolePermissionRepository.save(rolePermission);
        }
    }
}