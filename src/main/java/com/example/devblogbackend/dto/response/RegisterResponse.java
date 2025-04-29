package com.example.devblogbackend.dto.response;

import com.example.devblogbackend.dto.UserInfoDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String token;
    private UserInfoDTO userInfo;
}
