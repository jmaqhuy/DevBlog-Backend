package com.example.devblogbackend.services;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.request.LoginRequest;
import com.example.devblogbackend.dto.request.RegisterRequest;
import com.example.devblogbackend.dto.response.LoginResponse;
import com.example.devblogbackend.dto.response.RegisterResponse;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.enums.Role;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.UserRepository;
import com.example.devblogbackend.service.AuthService;
import com.example.devblogbackend.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegisterUser_ValidData() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("example_email@gmail.com");
        request.setEmail("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");


        User user = new User();
        user.setId("user-id");
        user.setEmail(request.getEmail());
        user.setPassword("encodedPassword");
        user.setStatus(true);
        user.setRoles(Set.of(Role.USER));

        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenService.generateToken(user.getId(), user.getRoles())).thenReturn("fake-jwtToken");


        // Act
        ApiResponse<RegisterResponse> response = authService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(user.getId(), response.getData().getUserInfo().getId());
        assertEquals(user.getEmail(), response.getData().getUserInfo().getEmail());
        assertEquals("fake-jwtToken", response.getData().getToken());

        verify(userRepository).existsByEmail(request.getEmail());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("example_email@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            authService.registerUser(request);
        });

        // Assert
        assertEquals("User with this email already exists", ex.getMessage());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginUser_ValidData() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("example@gmail.com");
        request.setPassword("password123");

        User user = new User();
        user.setId("user-id");
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        when(userRepository.findByEmail(request.getEmail())).thenReturn(user);
        when(passwordEncoder.matches(request.getPassword(), "password123")).thenReturn(true);
        when(jwtTokenService.generateToken(user.getId(), user.getRoles())).thenReturn("fake-jwtToken");

        // Act
        ApiResponse<LoginResponse> response = authService.loginUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("user-id", response.getData().getUserInfo().getId());
        assertEquals("fake-jwtToken", response.getData().getToken());
    }
}


