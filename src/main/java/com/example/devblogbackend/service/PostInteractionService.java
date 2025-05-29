package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.PostCommentDTO;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.request.CommentRequest;
import com.example.devblogbackend.entity.*;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.BookmarkRepository;
import com.example.devblogbackend.repository.PostCommentRepository;
import com.example.devblogbackend.repository.PostRepository;
import com.example.devblogbackend.repository.UserReadRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PostInteractionService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostCommentRepository postCommentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserReadRepository userReadRepository;


    public PostInteractionService(PostRepository postRepository,
                                  UserService userService,
                                  PostCommentRepository postCommentRepository,
                                  BookmarkRepository bookmarkRepository,
                                  UserReadRepository userReadRepository) {
        this.postRepository = postRepository;
        this.userService = userService;
        this.postCommentRepository = postCommentRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.userReadRepository = userReadRepository;
    }


    public ApiResponse<Map<String, Boolean>> likePost(long postId, String id) {
        User user = userService.getUser(id);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("", "Post not found"));

        boolean liked = post.getLikes().contains(user);
        if (liked) {
            post.getLikes().remove(user);
        } else {
            post.getLikes().add(user);
        }
        postRepository.save(post);
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", !liked);
        return ApiResponse.<Map<String, Boolean>>builder()
                .data(response)
                .meta(new Meta("v1"))
                .build();
    }


    @Transactional
    public ApiResponse<PostCommentDTO> commentPost(long postId, CommentRequest request, String id) {

        User user = userService.getUser(id);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("", "Post not found"));
        PostComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = postCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new BusinessException("", "Comment you reply has been remove or not exist"));
        }


        PostComment postComment = PostComment.builder()
                .user(user)
                .post(post)
                .content(request.getComment())
                .parent(parentComment)
                .build();

        postComment = postCommentRepository.save(postComment);
        return ApiResponse.<PostCommentDTO>builder()
                .data(PostCommentDTO.fromEntity(postComment))
                .meta(new Meta("v1"))
                .build();
    }

    public ApiResponse<List<PostCommentDTO>> getCommentPost(long postId, String id, String parentId) {

        User user = userService.getUser(id);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("", "Post not found"));

        PostComment parent = parentId != null
                ? postCommentRepository.findById(Long.parseLong(parentId))
                .orElseThrow(() -> new BusinessException("", "Parent comment not found"))
                : null;

        List<PostComment> postComment = parentId == null
                ? postCommentRepository.findByPostAndParentIsNull(post)
                : postCommentRepository.findByPostAndParent(post, parent);

        return ApiResponse.<List<PostCommentDTO>>builder()
                .data(postComment.stream()
                        .sorted(Comparator.comparing(PostComment::getCommentAt).reversed())
                        .map(PostCommentDTO::fromEntity)
                        .toList())
                .meta(new Meta("v1"))
                .build();
    }

    @Transactional
    public ApiResponse<Map<String, Boolean>> bookmarkPost(long postId, String id) {
        User user = userService.getUser(id);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("", "Post not found"));

        Bookmark.BookmarkID bookmarkId = new Bookmark.BookmarkID(user.getId(), postId);


        Optional<Bookmark> existing = bookmarkRepository.findById(bookmarkId);
        boolean isAdd;

        if (existing.isPresent()) {
            bookmarkRepository.deleteById(bookmarkId);
            isAdd = false;
        } else {
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .post(post)
                    .build();
            bookmarkRepository.save(bookmark);
            isAdd = true;
        }

        return ApiResponse.<Map<String, Boolean>>builder()
                .data(Map.of("bookmark", isAdd))
                .meta(new Meta("v1"))
                .build();
    }

    public ApiResponse<PostDTO> readPost(Long postId, String id) {
        User user = userService.getUser(id);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("", "Post not found"));

        UserReadHistory.UserReadHistoryID hid = new UserReadHistory.UserReadHistoryID(user.getId(), post.getId());
        Optional<UserReadHistory> existed = userReadRepository.findById(hid);
        if (existed.isEmpty()) {
            UserReadHistory userReadHistory = new UserReadHistory();
            userReadHistory.setUser(user);
            userReadHistory.setPost(post);
            userReadRepository.save(userReadHistory);
        }

        boolean isBookmarked = bookmarkRepository.existsByPostAndUser(post, user);

        return ApiResponse.<PostDTO>builder()
                .data(PostDTO.fromEntity(post, user, isBookmarked))
                .meta(new Meta("v1"))
                .build();
    }
}
