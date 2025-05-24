package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.UserInfoDTO;
import com.example.devblogbackend.dto.request.LoginRequest;
import com.example.devblogbackend.dto.request.RegisterRequest;
import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.response.LoginResponse;
import com.example.devblogbackend.dto.response.RegisterResponse;
import com.example.devblogbackend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register new account"
    )
    public ApiResponse<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return authService.registerUser(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login to app"
    )
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return authService.loginUser(request);
    }

    @PostMapping("/introspect")
    @Operation(
            summary = "Validate token"
    )
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LoginResponse> introspect(@AuthenticationPrincipal Jwt jwt) {
        return authService.introspect(jwt);
    }
}
