package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.PostPreviewDTO;
import com.example.devblogbackend.dto.request.CreateNewPostRequest;
import com.example.devblogbackend.dto.request.ShareExternalPostRequest;
import com.example.devblogbackend.entity.ExternalPost;
import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int SAMPLE_PAGE_SIZE = 6;
    private static final long CUTOFF_DAYS = 30;
    private static final double W_LIKE = 1.0;
    private static final double W_COMMENT = 0.5;
    private static final double W_BOOKMARK = 2.0;
    private static final double DECAY = 0.1;
    private static final double W_READ_HISTORY = 0.5;

    private final ExternalPostService externalPostService;
    private final UserService userService;
    private final PostRepository postRepository;
    private final TagService tagService;

    public ApiResponse<PostDTO> createPost(String token, CreateNewPostRequest request) {
        User user = userService.verifyAndGetUser(token);
        Post post = new Post();
        post.setAuthor(user);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setThumbnail(request.getThumbnail());
        applyTags(post, request.getTags());

        Post saved = postRepository.save(post);
        return ApiResponse.<PostDTO>builder()
                .meta(new Meta("v1"))
                .data(PostDTO.fromEntity(saved, user))
                .build();
    }

    public ApiResponse<PostDTO> addExternalPost(String token, ShareExternalPostRequest request) {
        User user = userService.verifyAndGetUser(token);
        ExternalPost ext = externalPostService.addExternalPost(request);

        Post post = new Post();
        post.setAuthor(user);
        post.setPublicationDate(LocalDateTime.now());
        post.setContent(request.getUserThrough());
        post.setExternalPost(ext);
        applyTags(post, request.getTags());

        Post saved = postRepository.save(post);
        return ApiResponse.<PostDTO>builder()
                .meta(new Meta("v1"))
                .data(PostDTO.fromEntity(saved, user))
                .build();
    }

    public ApiResponse<List<PostDTO>> getPostsForYou(String token, int page) {
        User user = userService.verifyAndGetUser(token);
        List<Post> posts = fetchByScore(user.getFavoriteTags(), page, DEFAULT_PAGE_SIZE);
        return toDtoResponse(posts, user);
    }

    public ApiResponse<List<PostDTO>> getTopPosts(String token, int page) {
        User user = userService.verifyAndGetUser(token);
        List<Post> posts = fetchByScore(null, page, SAMPLE_PAGE_SIZE);

        return toDtoResponse(posts, user);
    }

    public ApiResponse<List<PostDTO>> getPostsFollowing(String token, int page) {
        User user = userService.verifyAndGetUser(token);
        Set<User> following = user.getFollowing();
        if (following.isEmpty()) {
            throw new BusinessException("FOLLOWING_EMPTY", "Your feed is empty because you haven’t followed any user");
        }
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(CUTOFF_DAYS);
        List<Post> posts = postRepository.findPostsOfFollowing(
                Set.copyOf(following), cutoff, pageable
        );
        return toDtoResponse(posts, user);
    }

    public List<PostPreviewDTO> getSamplePosts() {
        List<Post> posts = fetchByScore(null, 0, SAMPLE_PAGE_SIZE);
        return posts.stream()
                .map(p -> PostPreviewDTO.fromEntity(p, new User()))
                .toList();
    }

    //=== Helper methods ===

    private List<Post> fetchByScore(Set<Tag> tags, int page, int size) {
        Set<Integer> tagIds = null;


        if (tags != null && !tags.isEmpty()) {
            tagIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());
            System.out.println("Tags: " + tagIds.size());
        } else {
            System.out.println("Tags: 0");
        }
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(CUTOFF_DAYS);
        return postRepository.findPostsByScore(
                tagIds,
                cutoff,
                (float) W_LIKE,
                (float) W_COMMENT,
                (float) W_BOOKMARK,
                (float) DECAY,
                (float) W_READ_HISTORY,
                pageable
        );
    }

    private ApiResponse<List<PostDTO>> toDtoResponse(List<Post> posts, User user) {
        List<PostDTO> dtos = posts.stream()
                .map(p -> PostDTO.fromEntity(p, user))
                .toList();
        return ApiResponse.<List<PostDTO>>builder()
                .meta(new Meta("v1"))
                .data(dtos)
                .build();
    }

    private void applyTags(Post post, List<String> tagNames) {
        Set<Tag> tags = tagService.getTagsByName(tagNames);
        if (!tags.isEmpty()) {
            post.setTags(tags);
        }
    }
}
