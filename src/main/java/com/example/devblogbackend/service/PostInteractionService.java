package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.PostCommentDTO;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.request.CommentRequest;
import com.example.devblogbackend.entity.*;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.listener.PostEventListener;
import com.example.devblogbackend.listener.PostInteractionEvent;
import com.example.devblogbackend.repository.BookmarkRepository;
import com.example.devblogbackend.repository.PostCommentRepository;
import com.example.devblogbackend.repository.PostRepository;
import com.example.devblogbackend.repository.UserReadRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostInteractionService {
    private final PostRepository postRepository;
    private final UserService userService;
    private final PostCommentRepository postCommentRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserReadRepository userReadRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager entityManager;


    public ApiResponse<Map<String, Boolean>> likePost(long postId, String id) {
        User user = userService.getUser(id);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "Post not found"));

        boolean liked = post.getLikes().contains(user);
        log.info("Post score: {}", post.getScore());
        if (liked) {
            post.getLikes().remove(user);
            log.info("Remove like");
        } else {
            post.getLikes().add(user);
            log.info("Add like");
        }
        postRepository.saveAndFlush(post);
        eventPublisher.publishEvent(new PostInteractionEvent(this, post.getId()));


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
                .orElseThrow(() -> new BusinessException(404, "Post not found"));

        PostComment postComment = PostComment.builder()
                .user(user)
                .post(post)
                .content(request.getComment())
                .build();
        postComment = postCommentRepository.save(postComment);
        postRepository.saveAndFlush(post);
        eventPublisher.publishEvent(new PostInteractionEvent(this, post.getId()));
        return ApiResponse.<PostCommentDTO>builder()
                .data(PostCommentDTO.fromEntity(postComment))
                .meta(new Meta("v1"))
                .build();
    }

    public ApiResponse<List<PostCommentDTO>> getCommentPost(long postId, String id) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "Post not found"));

        List<PostComment> postComments = postCommentRepository.findByPostOrderByCommentAtDesc(post);

        return ApiResponse.<List<PostCommentDTO>>builder()
                .data(postComments.stream()
                        .map(PostCommentDTO::fromEntity)
                        .toList())
                .meta(new Meta("v1"))
                .build();
    }

    @Transactional
    public ApiResponse<Map<String, Boolean>> bookmarkPost(long postId, String id) {
        User user = userService.getUser(id);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "Post not found"));

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
        postRepository.saveAndFlush(post);
        eventPublisher.publishEvent(new PostInteractionEvent(this, post.getId()));

        return ApiResponse.<Map<String, Boolean>>builder()
                .data(Map.of("bookmark", isAdd))
                .meta(new Meta("v1"))
                .build();
    }

    public ApiResponse<PostDTO> readPost(Long postId, String id) {
        User user = userService.getUser(id);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "Post not found"));

        UserReadHistory.UserReadHistoryID hid = new UserReadHistory.UserReadHistoryID(user.getId(), post.getId());
        Optional<UserReadHistory> existed = userReadRepository.findById(hid);
        if (existed.isEmpty()) {
            UserReadHistory userReadHistory = new UserReadHistory();
            userReadHistory.setUser(user);
            userReadHistory.setPost(post);
            userReadRepository.save(userReadHistory);
            postRepository.saveAndFlush(post);
            eventPublisher.publishEvent(new PostInteractionEvent(this, post.getId()));
        }

        boolean isBookmarked = bookmarkRepository.existsByPostAndUser(post, user);

        return ApiResponse.<PostDTO>builder()
                .data(PostDTO.fromEntity(post, user, isBookmarked))
                .meta(new Meta("v1"))
                .build();
    }

    @Transactional
    public ApiResponse<Map<String, Boolean>> deletePost(Long postId, String userid) {
        User user = userService.getUser(userid);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(404, "Post not found"));

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new BusinessException(403, "You are not the author of this post");
        }

        deletePostWithEntityManager(postId);

        return ApiResponse.<Map<String, Boolean>>builder()
                .data(Map.of("deleted", true))
                .meta(new Meta("v1"))
                .build();
    }

    private void deletePostWithEntityManager(Long postId) {

        entityManager.createNativeQuery("DELETE FROM post_tag WHERE post_id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM post_like WHERE post_id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM user_read_history WHERE post_id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM bookmark WHERE post_id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM post_comment WHERE post_id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();

        entityManager.flush();

        Post post = entityManager.find(Post.class, postId);
        if (post != null) {
            entityManager.remove(post);
        }
    }
}
