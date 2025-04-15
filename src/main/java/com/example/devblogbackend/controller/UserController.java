package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.dto.response.UpdateProfileResponse;
import com.example.devblogbackend.service.UserInfoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserInfoService userInfoService;

    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @PutMapping(value = "/profile")
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid UpdateProfileRequest request) {
        return userInfoService.updateUserProfile(token.substring(7), request);
    }
} 