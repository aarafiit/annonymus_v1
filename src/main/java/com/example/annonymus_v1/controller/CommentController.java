package com.example.annonymus_v1.controller;

import com.example.annonymus_v1.dto.CommentDto;
import com.example.annonymus_v1.dto.ReviewDto;
import com.example.annonymus_v1.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/reviews/{id}/comment")
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentDto> comments = commentService.getAllComments(id,pageable);

        return ResponseEntity.ok(comments);
    }

    @PostMapping("/reviews/{id}/comment")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable UUID id,
            @RequestBody CommentDto commentDto) {
        commentDto.setReviewId(id);
        CommentDto createdComment = commentService.createComment(commentDto);
        return ResponseEntity.ok(createdComment);
    }

}
