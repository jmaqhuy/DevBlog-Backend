package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.UserDTO;
import com.example.devblogbackend.dto.UserInfoDTO;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.dto.TagDTO;
import com.example.devblogbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping( "/me")
    @Operation(
            summary = "Update my profile"
    )
    public ApiResponse<UserInfoDTO> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid UpdateProfileRequest request) {
        return userService.updateUserProfile(token.substring(7), request);
    }

    @Operation(
            summary = "Show my profile"
    )
    @GetMapping("/me")
    public ApiResponse<UserInfoDTO> getOwnProfile(
            @RequestHeader("Authorization") String token) {
        return userService.getOwnProfile(token.substring(7));
    }

    @Operation(
            summary = "Show another profile"
    )
    @GetMapping("/{id}")
    public ApiResponse<UserInfoDTO> getUserProfile(
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        return userService.getAnotherProfile(token.substring(7), id);
    }

    @GetMapping("/me/favorite-tags")
    @Operation(
            summary = "Get my favorite tags"
    )
    public ApiResponse<Set<TagDTO>> getUserFavoriteTags(
            @RequestHeader("Authorization") String token) {
        return userService.getUserFavoriteTags(token.substring(7));
    }


    @PostMapping("/me/favorite-tags")
    @Operation(
            summary = "User update their favorite tags"
    )
    public ApiResponse<Set<TagDTO>> updateUserFavoriteTags(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Set<TagDTO> tags) {
        return userService.updateUserFavoriteTags(jwt.getSubject(), tags);
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
            @RequestHeader("Authorization") String token,
            @PathVariable String id) {
        return userService.followUser(token.substring(7), id);
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