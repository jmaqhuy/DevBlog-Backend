package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoDTO {
    private String id;
    private String email;
    private String fullname;
    private String username;
    private String avatarLink;
    private String readme;
    private java.time.LocalDateTime registrationAt;
    private Set<Tag> favoriteTags;
    private Set<Role> roles;
    private int followers;
    private int following;
    private int posts;
    private Boolean isFollowing;

    public static UserInfoDTO fromEntity(User user,@Nullable Boolean isFollowing) {
        return UserInfoDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .username(user.getUsername())
                .avatarLink(user.getAvatarLink())
                .readme(user.getReadme())
                .registrationAt(user.getRegistrationAt())
                .favoriteTags(user.getFavoriteTags())
                .roles(user.getRoles())
                .followers(user.getFollowers().size())
                .following(user.getFollowing().size())
                .posts(user.getPostCount())
                .isFollowing(isFollowing)
                .build();
    }

}
