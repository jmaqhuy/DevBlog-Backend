package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String fullName;
    private String username;
    private String avatar;

    public static UserDTO fromEntity(User user) {
        return new UserDTO(
                user.getId(),
                user.getFullname(),
                user.getUsername(),
                user.getAvatarLink()
        );
    }
}
