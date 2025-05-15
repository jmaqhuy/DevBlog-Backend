package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.PostCommentDTO;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.request.CommentRequest;
import com.example.devblogbackend.service.PostInteractionService;
import jakarta.annotation.Nullable;
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
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        return postInteractionService.readPost(postId, token.substring(7));
    }

    @PostMapping("/{postId}/like")
    public ApiResponse<Map<String, Boolean>> likePost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId) {
        return postInteractionService.likePost(postId, token.substring(7));
    }

    @PostMapping("/{postId}/comment")
    public ApiResponse<PostCommentDTO> commentPost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId,
            @RequestBody CommentRequest request) {
        return postInteractionService.commentPost(postId, request, token.substring(7));
    }

    @GetMapping("/{postId}/comment")
    public ApiResponse<List<PostCommentDTO>> commentPost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId,
            @RequestParam @Nullable String parentId) {
        return postInteractionService.getCommentPost(postId, token.substring(7), parentId);
    }

    @PostMapping("/{postId}/bookmark")
    public ApiResponse<Map<String, Boolean>> bookmarkPost(
            @RequestHeader("Authorization") String token,
            @PathVariable Long postId){
        return postInteractionService.bookmarkPost(postId, token.substring(7));
    }

}
