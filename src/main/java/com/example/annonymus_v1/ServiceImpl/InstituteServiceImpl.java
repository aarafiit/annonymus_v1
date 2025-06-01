package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.InstituteDto;
import com.example.annonymus_v1.entity.Institute;
import com.example.annonymus_v1.mapper.InstituteMapper;
import com.example.annonymus_v1.repository.InstituteRepository;
import com.example.annonymus_v1.service.InstituteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstituteServiceImpl implements InstituteService {

    private final InstituteRepository instituteRepository;


    @Override
    public List<InstituteDto> getAllInstitutes(String searchParam) {
        List<Institute> universities =  instituteRepository.getAllInstitutes(searchParam);

        if(!universities.isEmpty()) {
            return universities
                    .stream()
                    .map(InstituteMapper::toDto)
                    .toList();
        }
        else {
            throw new RuntimeException("No institutes found");
        }
    }
}
