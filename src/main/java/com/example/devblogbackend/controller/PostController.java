package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.request.CreateNewPostRequest;
import com.example.devblogbackend.dto.request.ShareExternalPostRequest;
import com.example.devblogbackend.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
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
            @RequestHeader("Authorization") String token,
            @RequestParam int pageNumber) {
        return postService.getPostsForYou(token.substring(7), pageNumber);
    }
    @GetMapping("/following")
    public ApiResponse<List<PostDTO>> getPostsFollowing(
            @RequestHeader("Authorization") String token,
            @RequestParam int pageNumber) {
        return postService.getPostsFollowing(token.substring(7), pageNumber);
    }

    @GetMapping("/top")
    public ApiResponse<List<PostDTO>> getTopPosts(
            @RequestHeader("Authorization") String token,
            @RequestParam int pageNumber){
        return postService.getTopPosts(token.substring(7), pageNumber);
    }

    @PostMapping("/share")
    @Operation(
            summary = "User share an external article"
    )
    public ApiResponse<PostDTO> addNewExternalPost(
            @RequestHeader("Authorization") String token,
            @RequestBody ShareExternalPostRequest request) {
        return postService.addExternalPost(token.substring(7), request);
    }

    @PostMapping("/create")
    @Operation(
            summary = "User create a new post by yourself"
    )
    public ApiResponse<PostDTO> addNewPost(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateNewPostRequest request) {
        return postService.createPost(token.substring(7), request);
    }

}
