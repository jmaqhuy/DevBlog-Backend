package com.example.devblogbackend.listener;

import org.springframework.context.ApplicationEvent;

public class PostInteractionEvent extends ApplicationEvent {
    private final Long postId;
    public PostInteractionEvent(Object source, Long postId) {
        super(source);
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }
}
