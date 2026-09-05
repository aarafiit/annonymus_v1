package com.example.annonymus_v1.mapper;

import com.example.annonymus_v1.dto.CommentDto;
import com.example.annonymus_v1.entity.Comment;

public class CommentMapper {
    public static CommentDto toDto(Comment comment){
        if (comment == null) {
            return null;
        }

        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setReviewId(comment.getReviewId());
        dto.setComment(comment.getComment());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setDeleted(comment.getDeleted());
        return dto;
    }

    public static Comment toEntity(CommentDto dto){
        if (dto == null) {
            return null;
        }

        Comment comment = new Comment();
        comment.setReviewId(dto.getReviewId());
        comment.setComment(dto.getComment());
        comment.setDeleted(dto.getDeleted());
        return comment;
    }
}
