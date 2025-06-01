package com.example.annonymus_v1.service;

import com.example.annonymus_v1.dto.InstituteDto;

import java.util.List;

public interface InstituteService {
    List<InstituteDto> getAllInstitutes(String searchParam);
}
