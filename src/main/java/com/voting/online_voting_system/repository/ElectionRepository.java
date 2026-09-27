package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.Election;
import com.voting.online_voting_system.enums.ElectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElectionRepository
        extends JpaRepository<Election, Long> {

    List<Election> findByDepartment(String department);

    List<Election> findByStatus(ElectionStatus status);

    List<Election> findByCreatedById(Long userId);
}