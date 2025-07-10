package com.example.annonymus_v1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "users_ip",
        uniqueConstraints = @UniqueConstraint(columnNames = {"review_id", "user_ip"}),
        indexes = @Index(columnList = "review_id, user_ip", name = "idx_post_user_ip")
)
public class UsersIp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @Column(name = "user_ip", nullable = false)
    private String userIp;

    @Column(name = "liked")
    private Boolean like;

}
