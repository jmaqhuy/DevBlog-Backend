package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.UserDTO;
import com.example.devblogbackend.dto.response.SearchResponse;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.SearchLog;
import com.example.devblogbackend.enums.Role;
import com.example.devblogbackend.repository.PostRepository;
import com.example.devblogbackend.repository.TagRepository;
import com.example.devblogbackend.repository.UserRepository;
import com.example.devblogbackend.repository.SearchLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final SearchLogRepository searchLogRepository;

    public ApiResponse<SearchResponse> search(String keyword, String target, String userId) {
        int limit = (target == null || target.equalsIgnoreCase("all")) ? 5 : 15;
        String kw = keyword == null ? "" : keyword;
        List<PostDTO> posts = null;
        List<UserDTO> users = null;
        List<Tag> tags = null;

        if (target == null || target.equalsIgnoreCase("all")) {
            users = userRepository.findUserByUsernameOrFullnameNotContainingAdmin(kw, Role.ADMIN, PageRequest.of(0, limit))
                    .stream().map(UserDTO::fromEntity).collect(Collectors.toList());
            posts = postRepository.searchPostsByKeyword(kw, PageRequest.of(0, limit))
                    .stream().map(post -> PostDTO.fromEntity(post, null, null)).collect(Collectors.toList());
            tags = tagRepository.findByNameContainingIgnoreCase(kw, PageRequest.of(0, limit));
        } else if (target.equalsIgnoreCase("user")) {
            users = userRepository.findUserByUsernameOrFullnameNotContainingAdmin(kw, Role.ADMIN, PageRequest.of(0, limit))
                    .stream().map(UserDTO::fromEntity).collect(Collectors.toList());
        } else if (target.equalsIgnoreCase("post")) {
            posts = postRepository.searchPostsByKeyword(kw, PageRequest.of(0, limit))
                    .stream().map(post -> PostDTO.fromEntity(post, null, null)).collect(Collectors.toList());
        } else if (target.equalsIgnoreCase("tag")) {
            tags = tagRepository.findByNameContainingIgnoreCase(kw, PageRequest.of(0, limit));
        }

        // Log search keyword if userId is provided and keyword is not empty
        if (userId != null && !kw.isBlank()) {
            userRepository.findById(userId).ifPresent(user -> {
                SearchLog log = searchLogRepository.findByUserAndKeyword(user, kw);
                if (log != null) {
                    log.setSearchedAt(java.time.LocalDateTime.now());
                } else {
                    log = SearchLog.builder()
                            .user(user)
                            .keyword(kw)
                            .build();
                }
                searchLogRepository.save(log);
            });
        }

        SearchResponse response = SearchResponse.builder()
                .posts(posts)
                .users(users)
                .tags(tags)
                .build();
        return ApiResponse.<SearchResponse>builder().data(response).build();
    }
    public ApiResponse<List<String>> getSearchHistory(String userId) {
        List<String> searchHistory = List.of();
        var userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            var logs = searchLogRepository.findByUserOrderBySearchedAtDesc(userOpt.get());
            searchHistory = logs.stream().map(SearchLog::getKeyword).toList();
        }
        return ApiResponse.<List<String>>builder().data(searchHistory).build();
    }

    public ApiResponse<List<String>> getRecommendSearch(String kw){
        List<String> recommendations = List.of();
        if (kw != null && !kw.isBlank()) {
            recommendations = searchLogRepository.findByKeywordContainsIgnoreCase(kw)
                    .stream()
                    .map(SearchLog::getKeyword)
                    .distinct()
                    .limit(10)
                    .collect(Collectors.toList());
        }
        return ApiResponse.<List<String>>builder().data(recommendations).build();
    }
}
