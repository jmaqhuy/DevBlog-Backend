package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.PostCommentDTO;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.request.CommentRequest;
import com.example.devblogbackend.service.PostInteractionService;
import jakarta.annotation.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostInteractionController {
    private final PostInteractionService postInteractionService;
    public PostInteractionController(PostInteractionService postInteractionService) {
        this.postInteractionService = postInteractionService;
    }
    @GetMapping("/{postId}")
    public ApiResponse<PostDTO> readPost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId) {
        return postInteractionService.readPost(postId, jwt.getSubject());
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<Map<String, Boolean>> likePost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId) {
        return postInteractionService.likePost(postId, jwt.getSubject());
    }

    @PostMapping("/{postId}/comment")
    public ApiResponse<PostCommentDTO> commentPost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @RequestBody CommentRequest request) {
        return postInteractionService.commentPost(postId, request, jwt.getSubject());
    }

    @GetMapping("/{postId}/comment")
    public ApiResponse<List<PostCommentDTO>> commentPost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId,
            @RequestParam @Nullable String parentId) {
        return postInteractionService.getCommentPost(postId, jwt.getSubject(), parentId);
    }

    @PostMapping("/{postId}/bookmark")
    public ApiResponse<Map<String, Boolean>> bookmarkPost(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long postId){
        return postInteractionService.bookmarkPost(postId, jwt.getSubject());
    }

}
