package com.example.annonymus_v1.repository;

import com.example.annonymus_v1.entity.UsersIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserIpRepository extends JpaRepository<UsersIp,Long> {
    Optional<UsersIp> findByReviewIdAndUserIp(UUID reviewId, String userIp);
}
