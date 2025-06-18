package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.PostComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PostCommentDTO {
    private Long id;

    private UserDTO user;
    private Long postId;
    private String content;
    private LocalDateTime commentAt;
    public static PostCommentDTO fromEntity(PostComment postComment) {
        return PostCommentDTO.builder()
                .id(postComment.getId())
                .user(UserDTO.fromEntity(postComment.getUser()))
                .postId(postComment.getPost().getId())
                .content(postComment.getContent())
                .commentAt(postComment.getCommentAt())
                .build();
    }
}
