package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.dto.response.TagDTO;
import com.example.devblogbackend.dto.response.UpdateProfileResponse;
import com.example.devblogbackend.service.UserInfoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

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


    @GetMapping("/favorite-tags")
    public ApiResponse<Set<TagDTO>> getUserFavoriteTags(
            @RequestHeader("Authorization") String token) {
        return userInfoService.getUserFavoriteTags(token.substring(7));
    }

    @PutMapping("/favorite-tags")
    public ApiResponse<Set<TagDTO>> updateUserFavoriteTags(
            @RequestHeader("Authorization") String token,
            @RequestBody Set<TagDTO> tags) {
        return userInfoService.updateUserFavoriteTags(token.substring(7), tags);
    }


} 