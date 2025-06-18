package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.*;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.exception.AuthenticationException;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.BookmarkRepository;
import com.example.devblogbackend.repository.PostRepository;
import com.example.devblogbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final String API_VERSION = "v1";
    private final TagService tagService;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;

    public ApiResponse<UserInfoDTO> updateUserProfile(
            String tokenId,
            String id,
            UpdateProfileRequest request) {

        if (!Objects.equals(tokenId, id)) {
            throw new BusinessException("Cannot update profile","You have not permission to update this profile");
        }
        // Get user from database
        User user = getUser(tokenId);

        // Validate and update email if provided
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateEmailUniqueness(request.getEmail(), user.getId());
            user.setEmail(request.getEmail());
        }

        // Validate and update username if provided
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            validateUsernameUniqueness(request.getUsername(), user.getId());
            user.setUsername(request.getUsername());
        }

        // Update name if provided
        if (request.getName() != null) {
            user.setFullname(request.getName());
        }

        // Handle avatar upload if provided
        if (request.getImageLink() != null && !request.getImageLink().isEmpty()) {
            user.setAvatarLink(request.getImageLink());
        }

        return buildResponse(userRepository.save(user), null);
    }

    public ApiResponse<Set<Tag>> getUserFavoriteTags(String id) {
        return getUserFavoriteTags(getUser(id));
    }

    public ApiResponse<Set<Tag>> updateUserFavoriteTags(String tokenId, String id, Set<Tag> tags) {
        if (!Objects.equals(tokenId, id)) {
            throw new BusinessException("Cannot update favorite tags","You have not permission to update this tags");
        }
        User user = userRepository.findById(tokenId)
                .orElseThrow(() -> new BusinessException("","User not found"));
        user.getFavoriteTags().clear();
        if (tags == null || tags.size() < 5){
            throw new BusinessException("","You need add at least 5 tags");
        }
        for (Tag t : tags) {
            Tag tag = tagService.findById(t.getId());
            if (tag == null) {
                break;
            }
            user.getFavoriteTags().add(tag);
        }
        user = userRepository.save(user);
        return getUserFavoriteTags(user);
    }

    private ApiResponse<Set<Tag>> getUserFavoriteTags(User user) {
        Set<Tag> tags = user.getFavoriteTags();

        return ApiResponse.<Set<Tag>>builder()
                .data(tags)
                .meta(new Meta(API_VERSION))
                .build();
    }

    public ApiResponse<UserInfoDTO> getUserProfile(String userId, String currentUserId) {
        User user = getUser(userId);
        User currentUser = getUser(currentUserId);
        return buildResponse(user, currentUser.getFollowing().contains(user));
    }

    public ApiResponse<Map<String, Boolean>> followUser(String id, String userId) {
        User user_this = getUser(id);


        if (user_this.getId().equals(userId)) {
            throw new BusinessException("Invalid Operation", "You cannot follow yourself.");
        }
        User user_that = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found","Can't find user with this id"));

        boolean is_following = user_this.getFollowing().contains(user_that);
        Map<String, Boolean> following = new HashMap<>();
        if (is_following) {
            user_this.getFollowing().remove(user_that);
            following.put("following", false);

        } else {
            user_this.getFollowing().add(user_that);
            following.put("following", true);
        }
        userRepository.save(user_this);


        return ApiResponse.<Map<String, Boolean>>builder()
                .data(following)
                .meta(new Meta(API_VERSION))
                .build();
    }


    /**
     * Validates that the email is unique across all users except the current user.
     *
     * @param email Email to validate
     * @param userId Current user's ID to exclude from uniqueness check
     * @throws BusinessException if email is already taken by another user
     */
    private void validateEmailUniqueness(String email, String userId) {
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new BusinessException("Update Error", "Email already taken");
        }
    }

    /**
     * Validates that the username is unique across all users except the current user.
     *
     * @param username Username to validate
     * @param userId Current user's ID to exclude from uniqueness check
     * @throws BusinessException if username is already taken by another user
     */
    private void validateUsernameUniqueness(String username, String userId) {
        if (userRepository.existsByUsernameAndIdNot(username, userId)) {
            throw new BusinessException("Update Error", "Username already taken");
        }
    }

    /**
     * Builds the API response for profile updates.
     *
     * @param user Updated user entity
     * @return ApiResponse containing the updated profile information
     */
    private ApiResponse<UserInfoDTO> buildResponse(User user, Boolean isFollowing) {
        return ApiResponse.<UserInfoDTO>builder()
                .data(UserInfoDTO.fromEntity(user, isFollowing))
                .meta(new Meta(API_VERSION))
                .build();
    }

    public ApiResponse<Set<UserDTO>> getFollowerSet(String id) {
        Set<UserDTO> followers = getUser(id).getFollowers()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toSet());

        return ApiResponse.<Set<UserDTO>>builder()
                .data(followers)
                .meta(new Meta(API_VERSION))
                .build();
    }

    public ApiResponse<Set<UserDTO>> getFollowingSet(String id) {
        Set<UserDTO> followings = getUser(id).getFollowing()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toSet());

        return ApiResponse.<Set<UserDTO>>builder()
                .data(followings)
                .meta(new Meta(API_VERSION))
                .build();
    }


    public ApiResponse<List<PostDTO>> getUserPosts(String id) {
        User user = getUser(id);
        List<PostDTO> post = postRepository.findPostsByAuthor(user)
                .stream()
                .map(p -> PostDTO.fromEntity(p, user, bookmarkRepository.existsByPostAndUser(p, user)))
                .toList();
        return ApiResponse.<List<PostDTO>>builder()
                .data(post)
                .meta(new Meta(API_VERSION))
                .build();
    }

    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found","Can't find user with this id"));
    }
}
