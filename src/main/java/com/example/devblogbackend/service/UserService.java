package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.request.UpdateProfileRequest;
import com.example.devblogbackend.dto.response.UpdateProfileResponse;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.exception.AuthenticationException;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Service class for managing user profile operations.
 * This service handles user profile updates including email, username, name, and avatar changes.
 * It ensures data integrity by validating uniqueness constraints and handling file uploads.
 */
@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final FileStorageService fileStorageService;
    private final String API_VERSION = "v1";

    /**
     * Constructs a new UserService with required dependencies.
     *
     * @param userRepository Repository for user data operations
     * @param jwtTokenService Service for JWT token operations
     * @param fileStorageService Service for file storage operations
     */
    public UserService(
            UserRepository userRepository,
            JwtTokenService jwtTokenService,
            FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Updates a user's profile information.
     * This method handles the update of email, username, name, and avatar.
     * It ensures that email and username remain unique across all users.
     *
     * @param token JWT token for user authentication
     * @param request DTO containing the profile update information
     * @param avatar Optional multipart file for user avatar
     * @return ApiResponse containing the updated user profile information
     * @throws AuthenticationException if the token is invalid or user not found
     * @throws BusinessException if email or username is already taken
     */
    public ApiResponse<UpdateProfileResponse> updateProfile(
            String token, 
            UpdateProfileRequest request, 
            MultipartFile avatar) {
        // Validate token and get user ID
        String userId = jwtTokenService.validateAndGetUserId(token);
        
        // Get user from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        // Validate and update email if provided
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            validateEmailUniqueness(request.getEmail(), userId);
            user.setEmail(request.getEmail());
        }

        // Validate and update username if provided
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            validateUsernameUniqueness(request.getUsername(), userId);
            user.setUsername(request.getUsername());
        }

        // Update name if provided
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        // Handle avatar upload if provided
        if (avatar != null && !avatar.isEmpty()) {
            handleAvatarUpload(user, avatar);
        }

        // Save updated user
        user = userRepository.save(user);

        return buildUpdateResponse(user);
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
     * Handles the upload and storage of user avatar images.
     * Stores the file and updates the user's avatarPath with the download URI.
     *
     * @param user User entity to update
     * @param avatar MultipartFile containing the avatar image
     */
    private void handleAvatarUpload(User user, MultipartFile avatar) {
        String fileName = fileStorageService.storeFile(avatar);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(fileName)
                .toUriString();
        user.setAvatarPath(fileDownloadUri);
    }

    /**
     * Builds the API response for profile updates.
     *
     * @param user Updated user entity
     * @return ApiResponse containing the updated profile information
     */
    private ApiResponse<UpdateProfileResponse> buildUpdateResponse(User user) {
        return ApiResponse.<UpdateProfileResponse>builder()
                .data(UpdateProfileResponse.builder()
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .name(user.getName())
                        .avatarPath(user.getAvatarPath())
                        .build())
                .meta(new Meta(API_VERSION))
                .build();
    }
} 