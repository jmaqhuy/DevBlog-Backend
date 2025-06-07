package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.response.SearchResponse;
import com.example.devblogbackend.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "target", required = false) String target,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<SearchResponse> response = searchService.search(keyword, target, jwt.getSubject());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search-history")
    public ResponseEntity<ApiResponse<List<String>>> getSearchHistory(
            @AuthenticationPrincipal Jwt jwt
            ) {
        ApiResponse<List<String>> response = searchService.getSearchHistory(jwt.getSubject());
        response.setMeta(new Meta("v1"));
        return ResponseEntity.ok(response);
    }
}
