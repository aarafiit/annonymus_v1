package com.example.annonymus_v1.dto;

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

    private String description;
    private Long rating;
    private Long instituteId;
    private String instituteName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;

}
