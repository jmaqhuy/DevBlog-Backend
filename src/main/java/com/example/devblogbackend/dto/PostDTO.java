package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDTO {
    private long id;
    private UserDTO author;
    private String title;
    private String thumbnail;
    private String content;
    private ExternalPostDTO externalPost;
    private Set<TagDTO> tags = new HashSet<>();
    private LocalDateTime publicationDate;
    private boolean isLiked;
    private int likes;
    private int comments;

    public static PostDTO fromEntity(Post post, User user) {
        Set<TagDTO> tagDTOs = Optional.ofNullable(post.getTags())
                .orElse(Collections.emptySet())
                .stream()
                .map(tag -> TagDTO.fromEntity(tag, 0))
                .collect(Collectors.toSet());

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
                tagDTOs,
                post.getPublicationDate(),
                post.getLikes().contains(user),
                post.getLikes().size(),
                post.getComments().size()
        );
    }
}
