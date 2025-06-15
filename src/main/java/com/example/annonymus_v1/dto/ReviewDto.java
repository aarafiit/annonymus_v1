package com.example.annonymus_v1.dto;

import com.example.annonymus_v1.enumurator.ReviewType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {

    private Long id;
    @Size(min = 10, max = 255)
    private String title;
    private String description;
    private Long rating;
    private Long instituteId;
    private String instituteName;
    private ReviewType reviewType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

}
