package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.entity.PostComment;
import com.example.devblogbackend.entity.Tag;
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
public class PostPreviewDTO {
    private Long id;
    private String userAvatar;
    private String username;
    private String title;
    private String thumbnail;
    private Set<Tag> tags = new HashSet<>();
    private LocalDateTime publicationDate;
    private Boolean liked;
    private Integer likes;
    private Integer comments;
    private String link;

    public static PostPreviewDTO fromEntity(Post post, User user) {

        boolean hasExternal = post.getExternalPost() != null;
        ExternalPostDTO external = hasExternal ? ExternalPostDTO.fromEntity(post.getExternalPost()) : null;


        return PostPreviewDTO.builder()
                .id(post.getId())
                .userAvatar(hasExternal ? external.getWebLogo() : post.getAuthor().getAvatarLink())
                .username(hasExternal ? (external.getDomain().contains("www") ? external.getDomain().substring(4) : external.getDomain()) : post.getAuthor().getUsername())
                .title(hasExternal ? external.getTitle() : post.getTitle())
                .thumbnail(hasExternal ? external.getThumbnail() : post.getThumbnail())
                .tags(post.getTags())
                .publicationDate(post.getPublicationDate())
                .liked(post.getLikes().contains(user))
                .likes(post.getLikes().size())
                .comments(post.getCommentCount())
                .link(hasExternal ? "https://" + external.getDomain() + external.getPath() : "#")
                .build();
    }


}
