package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.dto.response.UpdateProfileResponse;
import com.example.devblogbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestPart(value = "profile") @Valid UpdateProfileRequest request,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        return userService.updateProfile(token.substring(7), request, avatar);
    }
} 