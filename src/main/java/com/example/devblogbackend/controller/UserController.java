package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.dto.TagDTO;
import com.example.devblogbackend.dto.response.UpdateProfileResponse;
import com.example.devblogbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping(value = "/profile")
    @Operation(
            summary = "Update User Profile"
    )
    public ApiResponse<UpdateProfileResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid UpdateProfileRequest request) {
        return userService.updateUserProfile(token.substring(7), request);
    }



    @GetMapping("/favorite-tags")
    @Operation(
            summary = "Get user favorite tags"
    )
    public ApiResponse<Set<TagDTO>> getUserFavoriteTags(
            @RequestHeader("Authorization") String token) {
        return userService.getUserFavoriteTags(token.substring(7));
    }


    @PutMapping("/favorite-tags")
    @Operation(
            summary = "User update their favorite tags"
    )
    public ApiResponse<Set<TagDTO>> updateUserFavoriteTags(
            @RequestHeader("Authorization") String token,
            @RequestBody Set<TagDTO> tags) {
        return userService.updateUserFavoriteTags(token.substring(7), tags);
    }


} 