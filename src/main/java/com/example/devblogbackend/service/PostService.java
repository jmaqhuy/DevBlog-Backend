package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.request.CreateNewPostRequest;
import com.example.devblogbackend.dto.request.ShareExternalPostRequest;
import com.example.devblogbackend.entity.ExternalPost;
import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class PostService {
    private final ExternalPostService externalPostService;
    private final UserService userService;
    private final PostRepository postRepository;
    private final TagService tagService;

    public PostService(ExternalPostService externalPostService,
                       UserService userService,
                       PostRepository postRepository,
                       TagService tagService) {
        this.externalPostService = externalPostService;
        this.userService = userService;
        this.postRepository = postRepository;
        this.tagService = tagService;
    }

    public ApiResponse<PostDTO> createPost(String token, CreateNewPostRequest request) {
        Post post = new Post();
        User user = userService.verifyAndGetUser(token);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setThumbnail(request.getThumbnail());
        post.setAuthor(user);
        Set<Tag> tags = tagService.getTagsByName(request.getTags());
        if (!tags.isEmpty()) {
            post.setTags(tags);
        }

        post = postRepository.save(post);
        return ApiResponse.<PostDTO>builder()
                .meta(new Meta("v1"))
                .data(PostDTO.fromEntity(post))
                .build();
    }

    public ApiResponse<PostDTO> addExternalPost(String token, ShareExternalPostRequest request) {
        Post post = new Post();

        // set user
        User user = userService.verifyAndGetUser(token);
        post.setAuthor(user);
        post.setPublicationDate(LocalDateTime.now());
        post.setContent(request.getUserThrough());

        // set external post
        ExternalPost externalPost = externalPostService.addExternalPost(request);
        post.setExternalPost(externalPost);

        // set tag
        Set<Tag> tags = tagService.getTagsByName(request.getTags());
        if (!tags.isEmpty()) {
            post.setTags(tags);
        }

        post = postRepository.save(post);

        return ApiResponse.<PostDTO>builder()
                .data(PostDTO.fromEntity(post))
                .meta(new Meta("v1"))
                .build();
    }


}
