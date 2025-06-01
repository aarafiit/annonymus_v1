package com.example.annonymus_v1.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InstituteDto {
    private Long id;
    private String name;
    private String website;
    private String alias;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted = Boolean.FALSE;
}
