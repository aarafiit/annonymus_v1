package com.example.annonymus_v1.ServiceImpl;

import com.example.annonymus_v1.dto.CommentDto;
import com.example.annonymus_v1.entity.Comment;
import com.example.annonymus_v1.exception.BaseTranslatableRuntimeException;
import com.example.annonymus_v1.mapper.CommentMapper;
import com.example.annonymus_v1.repository.CommentRepository;
import com.example.annonymus_v1.service.CommentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@Data
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepo;

    @Override
    @Cacheable(value = "comments", key = "#reviewId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<CommentDto> getAllComments(UUID reviewId, Pageable pageable) {
        log.info("Fetching comments from database for review: {} and page: {}", reviewId, pageable.getPageNumber());
        Page<Comment> comments = commentRepo.findAllByReviewId(reviewId, pageable);
        return comments.map(CommentMapper::toDto);
    }

    @Override
    @CacheEvict(value = "comments", allEntries = true)
    public CommentDto createComment(CommentDto commentDto) {
        log.info("Creating new comment and clearing comments cache");
        if (commentDto.getReviewId() == null) {
            throw new BaseTranslatableRuntimeException(
                    "review.id.missing",
                    "Review ID is required",
                    null
            );
        }
        commentDto.setDeleted(false);
        commentRepo.save(CommentMapper.toEntity(commentDto));
        return commentDto;
    }
}
