package com.example.annonymus_v1.entity;

import com.example.annonymus_v1.enumurator.ReviewType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reviews")
public class Review {

    @Id
    private UUID id;
    @Column(length = 1000)
    private String description;
    private Long instituteId;
    private Long reviewType;
    private String title;
    private Long likeCount;
    private Long dislikeCount;

    @Column(columnDefinition = "timestamp")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "timestamp")
    private LocalDateTime updatedAt;

    @ColumnDefault("false")
    private Boolean deleted;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        this.createdAt = this.updatedAt = LocalDateTime.now();
        this.deleted = false;
        this.likeCount = 0L;
        this.dislikeCount = 0L;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
