package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.InstituteDto;
import com.example.annonymus_v1.entity.Institute;
import com.example.annonymus_v1.exception.BaseTranslatableRuntimeException;
import com.example.annonymus_v1.mapper.InstituteMapper;
import com.example.annonymus_v1.repository.InstituteRepository;
import com.example.annonymus_v1.service.InstituteService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstituteServiceImpl implements InstituteService {

    private final InstituteRepository instituteRepository;


    @Override
    @Cacheable(value = "institutes", key = "#searchParam.isEmpty()? 'all' : #searchParam")
    public List<InstituteDto> getAllInstitutes(String searchParam) {
        log.info("Fetching institutes from database with search param: {}", searchParam);
        List<Institute> universities =  instituteRepository.getAllInstitutes(searchParam);

        if(!universities.isEmpty()) {
            return universities
                    .stream()
                    .map(InstituteMapper::toDto)
                    .toList();
        }
        else {
            throw new BaseTranslatableRuntimeException(
                    "no.universities.found",
                    "No universities found for the given search parameter",
                    null
                    );
        }
    }

    @CacheEvict(value = "institutes", allEntries = true)
    public void clearInstituteCache() {
        log.info("Clearing institute cache");
    }
}
