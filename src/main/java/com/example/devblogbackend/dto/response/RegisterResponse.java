package com.example.devblogbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String token;
    private String id;
    private String email;
}
