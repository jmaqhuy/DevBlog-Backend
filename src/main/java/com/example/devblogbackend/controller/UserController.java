package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.*;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @Operation(
            summary = "Show user profile"
    )
    @GetMapping("/{id}")
    public ApiResponse<UserInfoDTO> getUserProfile(
            @PathVariable String id) {
        return userService.getUserProfile(id);
    }


    @PutMapping( "/{id}")
    @Operation(
            summary = "Update my profile"
    )
    public ApiResponse<UserInfoDTO> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody @Valid UpdateProfileRequest request) {
        return userService.updateUserProfile(jwt.getSubject(), id, request);
    }


    @GetMapping("/{id}/favorite-tags")
    @Operation(
            summary = "Get my favorite tags"
    )
    public ApiResponse<Set<Tag>> getUserFavoriteTags(
            @PathVariable String id) {
        return userService.getUserFavoriteTags(id);
    }


    @PostMapping("/{id}/favorite-tags")
    @Operation(
            summary = "User update their favorite tags"
    )
    public ApiResponse<Set<Tag>> updateUserFavoriteTags(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id,
            @RequestBody Set<Tag> tags) {
        return userService.updateUserFavoriteTags(jwt.getSubject(), id, tags);
    }

    @GetMapping("/{id}/posts")
    @Operation(
            summary = "Get user post"
    )
    public ApiResponse<List<PostDTO>> getUserPosts(
            @PathVariable String id) {
        return userService.getUserPosts(id);
    }


    @GetMapping("/{id}/followers")
    @Operation(
            summary = "Get list of who follow this id"
    )
    public ApiResponse<Set<UserDTO>> getFollowers(
            @PathVariable String id){
        return userService.getFollowerSet(id);
    }


    @GetMapping("/{id}/following")
    @Operation(
            summary = "Get list of who followed by id"
    )
    public ApiResponse<Set<UserDTO>> getFollowings(
            @PathVariable String id){
        return userService.getFollowingSet(id);
    }

    @Operation(
            summary = "Follow another user"
    )
    @PutMapping("/{id}/follow")
    public ApiResponse<Map<String, Boolean>> follow(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {
        return userService.followUser(jwt.getSubject(), id);
    }

//    @Operation(
//            summary = "Unfollow user"
//    )
//    @DeleteMapping("/{id}/follow")
//    public ApiResponse<Map<String, Boolean>> unfollow(
//            @RequestHeader("Authorization") String token,
//            @PathVariable String id) {
//        return userService.followUser(token.substring(7), id, false);
//    }
} 