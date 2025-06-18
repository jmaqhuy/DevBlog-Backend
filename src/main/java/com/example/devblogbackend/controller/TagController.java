package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.TagWithScore;
import com.example.devblogbackend.dto.response.TagDetailResponse;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.service.PostService;
import com.example.devblogbackend.service.TagService;
import com.example.devblogbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TagController {
    private final TagService tagService;
    private final UserService userService;
    private final PostService postService;


    @GetMapping("/tags")
    @Operation(
            summary = "Get all available tags"
    )
    public ApiResponse<List<Tag>> getAllTags() {
        return tagService.getAllTag();
    }

    @GetMapping("/tags/top")
    public ApiResponse<List<TagWithScore>> getTopTags() {
        return tagService.getTopTagsWithScore();
    }

    @GetMapping("/tags/{id}")
    public ApiResponse<TagDetailResponse> getTagDetail(
            @PathVariable Integer id,
            @AuthenticationPrincipal Jwt jwt) {
        User user = userService.getUser(jwt.getSubject());
        Tag tag = tagService.findById(id);
        Boolean isFavorite = user.getFavoriteTags().contains(tag);

        List<PostDTO> posts = postService.findPostsByTag(tag, user);
        return ApiResponse.<TagDetailResponse>builder()
                .data(new TagDetailResponse(TagWithScore.fromEntity(tag, null, null, isFavorite), posts))
                .meta(new Meta("v1"))
                .build();
    }
}
