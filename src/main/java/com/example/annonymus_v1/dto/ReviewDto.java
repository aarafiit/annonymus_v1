package com.example.annonymus_v1.dto;

import com.example.annonymus_v1.enumurator.ReviewType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {

    private UUID id;
    private String title;
    private String description;
    private Long rating;
    private Long instituteId;
    private String instituteName;
    private Long reviewType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

}
