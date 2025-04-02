package com.example.devblogbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileResponse {
    private String email;
    private String username;
    private String name;
    private String avatarPath;
} 