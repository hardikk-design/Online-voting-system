package com.voting.online_voting_system.controller;

import com.voting.online_voting_system.entity.Notification;
import com.voting.online_voting_system.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(
            NotificationRepository notificationRepository) {

        this.notificationRepository =
                notificationRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>>
    getUserNotifications(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                userId
                        )
        );
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>>
    getUnreadNotifications(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                notificationRepository
                        .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                                userId
                        )
        );
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId) {

        Notification notification =
                notificationRepository.findById(
                        notificationId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Notification not found"
                        ));

        notification.setRead(true);

        return ResponseEntity.ok(
                notificationRepository.save(notification)
        );
    }
}