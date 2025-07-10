package com.example.annonymus_v1.controller;

import com.example.annonymus_v1.dto.InstituteDto;
import com.example.annonymus_v1.service.InstituteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InstituteController {

    private final InstituteService instituteService;


    @GetMapping("/institutes")
    public List<InstituteDto> getInstitutes(
            @RequestParam(required = false, defaultValue = "") String searchParam
    ) {
        return instituteService.getAllInstitutes(searchParam);
    }
}
