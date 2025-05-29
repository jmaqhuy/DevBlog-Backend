package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.request.CreateNewPostRequest;
import com.example.devblogbackend.dto.request.ShareExternalPostRequest;
import com.example.devblogbackend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/for-you")
    public ApiResponse<List<PostDTO>> getPostsForYou(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam int pageNumber) {
        return postService.getPostsForYou(jwt.getSubject(), pageNumber);
    }
    @GetMapping("/following")
    public ApiResponse<List<PostDTO>> getPostsFollowing(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam int pageNumber) {
        return postService.getPostsFollowing(jwt.getSubject(), pageNumber);
    }

    @GetMapping("/top")
    public ApiResponse<List<PostDTO>> getTopPosts(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam int pageNumber){
        return postService.getTopPosts(jwt.getSubject(), pageNumber);
    }

    @PostMapping("/share")
    @Operation(
            summary = "User share an external article"
    )
    public ApiResponse<PostDTO> addNewExternalPost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ShareExternalPostRequest request) {
        return postService.addExternalPost(jwt.getSubject(), request);
    }

    @PostMapping("/create")
    @Operation(
            summary = "User create a new post by yourself"
    )
    public ApiResponse<PostDTO> addNewPost(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody CreateNewPostRequest request) {
        return postService.createPost(jwt.getSubject(), request);
    }

}
