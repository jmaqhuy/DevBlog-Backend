package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.request.IntrospectRequest;
import com.example.devblogbackend.dto.request.LoginRequest;
import com.example.devblogbackend.dto.request.RegisterRequest;
import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.response.IntrospectResponse;
import com.example.devblogbackend.dto.response.LoginResponse;
import com.example.devblogbackend.dto.response.RegisterResponse;
import com.example.devblogbackend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return userService.loginUser(request);
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        return userService.introspect(request);
    }
}
