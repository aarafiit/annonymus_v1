package com.example.annonymus_v1.mapper;

import com.example.annonymus_v1.dto.InstituteDto;
import com.example.annonymus_v1.entity.Institute;

public class InstituteMapper {
    public static InstituteDto toDto(Institute institute){
        if (institute == null) {
            return null;
        }

        InstituteDto dto = new InstituteDto();
        dto.setId(institute.getId());
        dto.setName(institute.getName());
        dto.setWebsite(institute.getWebsite());
        dto.setCreatedAt(institute.getCreatedAt());
        dto.setUpdatedAt(institute.getUpdatedAt());
        dto.setAlias(institute.getAlias());
        dto.setDeleted(institute.getDeleted());
        return dto;
    }

    public static Institute toEntity(InstituteDto dto){
        if (dto == null) {
            return null;
        }

        Institute institute = new Institute();
        institute.setId(dto.getId());
        institute.setName(dto.getName());
        institute.setWebsite(dto.getWebsite());
        institute.setCreatedAt(dto.getCreatedAt());
        institute.setUpdatedAt(dto.getUpdatedAt());
        institute.setDeleted(dto.getDeleted());
        return institute;
    }
}
