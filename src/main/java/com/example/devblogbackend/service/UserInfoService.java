package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.dto.response.TagDTO;
import com.example.devblogbackend.dto.response.UpdateProfileResponse;
import com.example.devblogbackend.entity.Tag;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.entity.UserInfo;
import com.example.devblogbackend.exception.AuthenticationException;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.UserInfoRepository;
import com.example.devblogbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserInfoService {

    private final UserInfoRepository userInfoRepository;
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final String API_VERSION = "v1";
    private final UserService userService;
    private final TagService tagService;

    /**
     * Constructs a new UserService with required dependencies.
     *
     * @param userInfoRepository Repository for User Information data operations
     * @param userRepository Repository for user data operations
     * @param jwtTokenService Service for JWT token operations
     */
    public UserInfoService(UserInfoRepository userInfoRepository,
                           UserRepository userRepository,
                           JwtTokenService jwtTokenService, UserService userService, TagService tagService) {
        this.userInfoRepository = userInfoRepository;
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
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
    public ApiResponse<UpdateProfileResponse> updateUserProfile(
            String token,
            UpdateProfileRequest request) {
        // Get user from database
        User user = verifyAndGetUser(token);

        UserInfo userInfo = userInfoRepository.findById(user.getId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Validate and update email if provided
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateEmailUniqueness(request.getEmail(), user.getId());
            user.setEmail(request.getEmail());
        }

        // Validate and update username if provided
        if (request.getUsername() != null && !request.getUsername().equals(userInfo.getUsername())) {
            validateUsernameUniqueness(request.getUsername(), user.getId());
            userInfo.setUsername(request.getUsername());
        }

        // Update name if provided
        if (request.getName() != null) {
            userInfo.setFullname(request.getName());
        }

        // Handle avatar upload if provided
        if (request.getImageLink() != null && !request.getImageLink().isEmpty()) {
            userInfo.setAvatarLink(request.getImageLink());
        }

        userInfo = userInfoRepository.save(userInfo);

        return buildUpdateResponse(userInfo);
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
                .map( tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toSet());

        return ApiResponse.<Set<TagDTO>>builder()
                .data(tags)
                .meta(new Meta(API_VERSION))
                .build();
    }

    private User verifyAndGetUser(String token) {
        String userId = jwtTokenService.validateAndGetUserId(token);

        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));
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
        if (userInfoRepository.existsByUsernameAndUserIdNot(username, userId)) {
            throw new BusinessException("Update Error", "Username already taken");
        }
    }

    /**
     * Builds the API response for profile updates.
     *
     * @param userInfo Updated user entity
     * @return ApiResponse containing the updated profile information
     */
    private ApiResponse<UpdateProfileResponse> buildUpdateResponse(UserInfo userInfo) {
        return ApiResponse.<UpdateProfileResponse>builder()
                .data(UpdateProfileResponse.builder()
                        .email(userInfo.getUser().getEmail())
                        .username(userInfo.getUsername())
                        .name(userInfo.getFullname())
                        .avatarPath(userInfo.getAvatarLink())
                        .build())
                .meta(new Meta(API_VERSION))
                .build();
    }
}
