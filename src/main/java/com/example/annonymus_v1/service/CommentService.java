package com.example.annonymus_v1.service;

import com.example.annonymus_v1.dto.CommentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CommentService {

    Page<CommentDto> getAllComments(UUID reviewId, Pageable pageable);
    CommentDto createComment(CommentDto commentDto);
}
