package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.*;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.exception.AuthenticationException;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final String API_VERSION = "v1";
    private final TagService tagService;

    /**
     * Constructs a new UserService with required dependencies.
     *
     * @param userRepository Repository for user data operations
     * @param jwtTokenService Service for JWT token operations
     */
    public UserService(UserRepository userRepository,
                       JwtTokenService jwtTokenService,
                       TagService tagService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.tagService = tagService;
    }

    /**
     * Updates a user's profile information.
     * This method handles the update of email, username, name, and avatar.
     * It ensures that email and username remain unique across all users.
     *
     * @param token JWT token for user authentication
     * @param request DTO containing the profile update information
     * @return ApiResponse containing the updated user profile information
     * @throws AuthenticationException if the token is invalid or user not found
     * @throws BusinessException if email or username is already taken
     */
    public ApiResponse<UserInfoDTO> updateUserProfile(
            String token,
            UpdateProfileRequest request) {
        // Get user from database
        User user = verifyAndGetUser(token);

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

        return buildResponse(userRepository.save(user));
    }

    public ApiResponse<Set<TagDTO>> getUserFavoriteTags(String token) {
        User user = verifyAndGetUser(token);
        return getUserFavoriteTags(user);
    }

    public ApiResponse<Set<TagDTO>> updateUserFavoriteTags(String token, Set<TagDTO> tags) {
        User user = verifyAndGetUser(token);
        user.getFavoriteTags().clear();
        for (TagDTO tagDto : tags) {
            Tag tag = tagService.findById(tagDto.getId());
            if (tag == null) {
                break;
            }
            user.getFavoriteTags().add(tag);
        }
        user = userRepository.save(user);
        return getUserFavoriteTags(user);
    }

    private ApiResponse<Set<TagDTO>> getUserFavoriteTags(User user) {
        Set<TagDTO> tags = user.getFavoriteTags()
                .stream()
                .map( tag -> new TagDTO(tag.getId(), tag.getName(), 0))
                .collect(Collectors.toSet());

        return ApiResponse.<Set<TagDTO>>builder()
                .data(tags)
                .meta(new Meta(API_VERSION))
                .build();
    }

    public User verifyAndGetUser(String token) {
        String userId = jwtTokenService.validateAndGetUserId(token);

        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("","User not found"));
    }

    /**
     * Get user profile
     *
     * @param token token of ...
     * @param userId id of user you want to find
     * @throws BusinessException if userId not match to any user*/
    public ApiResponse<UserInfoDTO> getAnotherProfile(String token, String userId) {
        User current = verifyAndGetUser(token);
        // TODO: following follower ...
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found","Can't find user with this id"));
        return buildResponse(user);
    }

    public ApiResponse<UserInfoDTO> getOwnProfile(String token) {
        User user = verifyAndGetUser(token);
        return buildResponse(user);
    }

    public ApiResponse<Map<String, Boolean>> followUser(String token, String userId) {
        User user_this = verifyAndGetUser(token);


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
    private ApiResponse<UserInfoDTO> buildResponse(User user) {
        return ApiResponse.<UserInfoDTO>builder()
                .data(UserInfoDTO.fromEntity(user))
                .meta(new Meta(API_VERSION))
                .build();
    }

    public ApiResponse<Set<UserDTO>> getFollowerSet(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found","Can't find user with this id"));
        Set<UserDTO> followers = user.getFollowers()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toSet());

        return ApiResponse.<Set<UserDTO>>builder()
                .data(followers)
                .meta(new Meta(API_VERSION))
                .build();
    }

    public ApiResponse<Set<UserDTO>> getFollowingSet(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("User not found","Can't find user with this id"));
        Set<UserDTO> followings = user.getFollowing()
                .stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toSet());

        return ApiResponse.<Set<UserDTO>>builder()
                .data(followings)
                .meta(new Meta(API_VERSION))
                .build();
    }


}
