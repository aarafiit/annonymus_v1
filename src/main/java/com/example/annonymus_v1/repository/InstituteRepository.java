package com.example.annonymus_v1.repository;

import com.example.annonymus_v1.entity.Institute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Long> {

    @Query("""
            SELECT model
            FROM Institute model
            WHERE (model.name ILIKE CONCAT('%', :searchParam, '%')
                   OR model.alias ILIKE CONCAT('%', :searchParam, '%'))
              AND (model.deleted = FALSE OR model.deleted IS NULL)
            ORDER BY model.name,model.alias
            """)
    List<Institute> getAllInstitutes(@Param("searchParam") String searchParam);
}
