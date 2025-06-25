package com.example.devblogbackend.listener;

import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.repository.PostRepository;
import com.example.devblogbackend.service.PostService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PostEventListener {
    private final PostRepository postRepository;
    private final PostService postService;

    public PostEventListener(PostRepository postRepository, PostService postService, EntityManager entityManager) {
        this.postRepository = postRepository;
        this.postService = postService;
    }

    @Async
    @EventListener
    @Transactional
    public void handle(PostInteractionEvent event) {
        Post post = postRepository.findById(event.getPostId()).orElse(null);
        if (post != null) {
            double newScore = postService.calculateScore(
                    post.getViewCount(),
                    post.getLikeCount(),
                    post.getCommentCount(),
                    post.getBookmarkCount(),
                    post.getPublicationDate()
            );
            log.info("New score: {}", newScore);
            post.setScore(newScore);
            postRepository.save(post);
        }
    }
}
