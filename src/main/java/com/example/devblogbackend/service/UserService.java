package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.request.IntrospectRequest;
import com.example.devblogbackend.dto.request.LoginRequest;
import com.example.devblogbackend.dto.request.RegisterRequest;
import com.example.devblogbackend.dto.response.IntrospectResponse;
import com.example.devblogbackend.dto.response.LoginResponse;
import com.example.devblogbackend.dto.response.RegisterResponse;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.entity.UserInfo;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final String API_VERSION = "v1";

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public ApiResponse<RegisterResponse> registerUser(RegisterRequest request) {
        validateNewUser(request.getEmail());
        
        User user = createNewUser(request);
        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        user.setUserInfo(userInfo);

        user = userRepository.save(user);
        return buildRegisterResponse(user);
    }

    public ApiResponse<LoginResponse> loginUser(LoginRequest request) {
        User user = validateLogin(request);
        return buildLoginResponse(user);
    }

    private void validateNewUser(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Register Error", "User with this email already exists");
        }
    }

    private User createNewUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return user;
    }

    private User validateLogin(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Login Error","Invalid email or password");
        }
        return user;
    }

    private ApiResponse<RegisterResponse> buildRegisterResponse(User user) {
        RegisterResponse response = RegisterResponse.builder()
                .token(jwtTokenService.generateToken(user.getId()))
                .id(user.getId())
                .email(user.getEmail())
                .build();

        return ApiResponse.<RegisterResponse>builder()
                .data(response)
                .meta(new Meta(API_VERSION))
                .build();
    }

    private ApiResponse<LoginResponse> buildLoginResponse(User user) {
        LoginResponse response = LoginResponse.builder()
                .token(jwtTokenService.generateToken(user.getId()))
                .id(user.getId())
                .email(user.getEmail())
                .build();

        return ApiResponse.<LoginResponse>builder()
                .data(response)
                .meta(new Meta(API_VERSION))
                .build();
    }

    public ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) {
        boolean isValid = jwtTokenService.validateToken(request.getToken());
        return ApiResponse.<IntrospectResponse>builder()
                .data(new IntrospectResponse(isValid))
                .meta(new Meta(API_VERSION))
                .build();
    }
}
