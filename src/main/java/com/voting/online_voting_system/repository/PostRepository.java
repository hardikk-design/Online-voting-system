package com.voting.online_voting_system.repository;

import com.voting.online_voting_system.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository
        extends JpaRepository<Post, Long> {
}
