package com.example.annonymus_v1.mapper;

import com.example.annonymus_v1.dto.CommentDto;
import com.example.annonymus_v1.dto.InstituteDto;
import com.example.annonymus_v1.entity.Comment;
import com.example.annonymus_v1.entity.Institute;

public class CommentMapper {
    public static CommentDto toDto(Comment comment){
        if (comment == null) {
            return null;
        }

        CommentDto dto = new CommentDto();
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
        comment.setCreatedAt(dto.getCreatedAt());
        comment.setUpdatedAt(dto.getUpdatedAt());
        comment.setDeleted(dto.getDeleted());
        return comment;
    }
}
