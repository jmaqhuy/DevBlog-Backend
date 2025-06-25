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
import com.example.devblogbackend.repository.BookmarkRepository;
import com.example.devblogbackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final long CUTOFF_DAYS = 30;
    private static final double W_LIKE = 1;
    private static final double W_COMMENT = 3;
    private static final double W_BOOKMARK = 5;
    private static final double W_VIEW = 0.1;
    private static final int W_S = 1209600;
    private static final LocalDateTime W_T0 = LocalDateTime.of(2015, 6, 1, 0, 0, 0);

    private final ExternalPostService externalPostService;
    private final UserService userService;
    private final PostRepository postRepository;
    private final TagService tagService;
    private final BookmarkRepository bookmarkRepository;

    public ApiResponse<PostDTO> createPost(String id, CreateNewPostRequest request) {
        User user = userService.getUser(id);
        Post post = new Post();
        post.setAuthor(user);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setThumbnail(request.getThumbnail());

        applyTags(post, request.getTags());
        post.setScore(calculateScore(0, 0, 0, 0, LocalDateTime.now()));
        Post saved = postRepository.save(post);
        return ApiResponse.<PostDTO>builder()
                .meta(new Meta("v1"))
                .data(PostDTO.fromEntity(saved, user, false))
                .build();
    }

    public ApiResponse<PostDTO> addExternalPost(String id, ShareExternalPostRequest request) {
        User user = userService.getUser(id);
        ExternalPost ext = externalPostService.addExternalPost(request);

        Post post = new Post();
        post.setAuthor(user);
        post.setContent(request.getUserThrough());
        post.setExternalPost(ext);
        applyTags(post, request.getTags());
        post.setScore(calculateScore(0, 0, 0, 0, LocalDateTime.now()));
        Post saved = postRepository.save(post);
        return ApiResponse.<PostDTO>builder()
                .meta(new Meta("v1"))
                .data(PostDTO.fromEntity(saved, user, false))
                .build();
    }

    public ApiResponse<List<PostDTO>> getPostsForYou(String id, int page) {
        User user = userService.getUser(id);
        List<Post> posts = fetchByScore(user.getFavoriteTags(), page);
        return toDtoResponse(posts, user);
    }

    public ApiResponse<List<PostDTO>> getTopPosts(String id, int page) {
        User user = userService.getUser(id);
        List<Post> posts = fetchByScore(null, page);
        return toDtoResponse(posts, user);
    }

    public ApiResponse<List<PostDTO>> getPostsFollowing(String id, int page) {
        User user = userService.getUser(id);
        Set<User> following = user.getFollowing();
        if (following.isEmpty()) {
            throw new BusinessException(400, "Your feed is empty because you haven’t followed any user");
        }
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(CUTOFF_DAYS);
        List<Post> posts = postRepository.findPostsOfFollowing(
                Set.copyOf(following), cutoff, pageable
        );
        return toDtoResponse(posts, user);
    }

    public List<PostPreviewDTO> getSamplePosts() {
        List<Post> posts = fetchByScore(null, 0);
        return posts.stream()
                .map(p -> PostPreviewDTO.fromEntity(p, new User()))
                .toList();
    }

    public List<PostDTO> findPostsByTag(Tag tag, User user) {
        return postRepository.findPostsByTags(Set.of(tag))
                .stream()
                .map(p -> PostDTO.fromEntity(p, user, bookmarkRepository.existsByPostAndUser(p, user)))
                .toList();
    }

    //=== Helper methods ===

    private List<Post> fetchByScore(Set<Tag> tags, int page) {
        Set<Integer> tagIds = null;

        Pageable pageable = PageRequest.of(page, PostService.DEFAULT_PAGE_SIZE);
        if (tags != null && !tags.isEmpty()) {
            tagIds = tags.stream().map(Tag::getId).collect(Collectors.toSet());
            System.out.println("Tags: " + tagIds.size());
            return postRepository.findPostsByTagsOrderByScoreDesc(tagIds, pageable);
        } else {
            System.out.println("Tags: 0");
            return postRepository.findAllByOrderByScoreDesc(pageable);
        }


    }

    private ApiResponse<List<PostDTO>> toDtoResponse(List<Post> posts, User user) {
        List<PostDTO> dtos = posts.stream()
                .map(p -> PostDTO.fromEntity(p, user, bookmarkRepository.existsByPostAndUser(p, user)))
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

    public double calculateScore(int view, int like, int comment, int bookmark, LocalDateTime t) {
        double engagement = view* W_VIEW + like*W_LIKE + comment*W_COMMENT + bookmark*W_BOOKMARK;
        log.info("Engagement: {}", engagement);
        long timestamp = t.toEpochSecond(ZoneOffset.UTC);
        long t0Timestamp = W_T0.toEpochSecond(ZoneOffset.UTC);
        return Math.log10(Math.max(1, engagement)) + ((double) (timestamp-t0Timestamp)/W_S);
    }
}
