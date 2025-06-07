package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {
    private Long id;
    private UserDTO author;
    private String title;
    private String thumbnail;
    private String content;
    private ExternalPostDTO externalPost;
    private Set<Tag> tags = new HashSet<>();
    private LocalDateTime publicationDate;
    private Boolean isLiked;
    private Integer likes;
    private Integer comments;
    private Boolean isBookmarked;
    private Double score;

    public static PostDTO fromEntity(Post post, User user, Boolean isBookmarked) {
        ExternalPostDTO externalPostDTO = null;
        if (post.getExternalPost() != null) {
            externalPostDTO = ExternalPostDTO.fromEntity(post.getExternalPost());
        }

        return new PostDTO(
                post.getId(),
                UserDTO.fromEntity(post.getAuthor()),
                post.getTitle(),
                post.getThumbnail(),
                post.getContent(),
                externalPostDTO,
                post.getTags(),
                post.getPublicationDate(),
                post.getLikes().contains(user),
                post.getLikes().size(),
                post.getCommentCount(),
                isBookmarked,
                post.getScore()
        );
    }
}
