package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.RolePermission;
import com.voting.online_voting_system.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository
        extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(Role role);
}