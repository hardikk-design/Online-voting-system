package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
            Long userId
    );
}