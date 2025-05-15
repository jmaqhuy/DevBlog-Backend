package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private int followers;
    private int following;

    public static UserInfoDTO fromEntity(User user){
        return UserInfoDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .username(user.getUsername())
                .avatarLink(user.getAvatarLink())
                .readme(user.getReadme())
                .registrationAt(user.getRegistrationAt())
                .favoriteTags(user.getFavoriteTags())
                .followers(user.getFollowers().size())
                .following(user.getFollowing().size())
                .build();
    }

}
