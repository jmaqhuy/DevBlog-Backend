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
import com.example.devblogbackend.exception.MyException;
import com.example.devblogbackend.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    @Value("${jwt.signerKey}")
    private String SIGNER_KEY;

    private final String API_VERSION = "v1";

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ApiResponse<RegisterResponse> registerUser(RegisterRequest request) {
        boolean isUserExists = userRepository.existsByEmail(request.getEmail());
        if (isUserExists) {
            throw new MyException("Register Error", "User already existed!");
        } else {
            User user = new User();
            user.setEmail(request.getEmail());
            PasswordEncoder pwdEncoder = new BCryptPasswordEncoder(10);
            user.setPassword(pwdEncoder.encode(request.getPassword()));
            user.setCreatedAt(new Date());
            user = userRepository.save(user);
            RegisterResponse response = RegisterResponse.builder()
                    .token(generateToken(user.getId()))
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .build();

            return ApiResponse.<RegisterResponse>builder()
                    .data(response)
                    .meta(new Meta(API_VERSION))
                    .build();
        }
    }

    public ApiResponse<LoginResponse> loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user != null) {
            PasswordEncoder pwdEncoder = new BCryptPasswordEncoder(10);
            if (pwdEncoder.matches(request.getPassword(), user.getPassword())) {

                LoginResponse response = LoginResponse.builder()
                        .token(generateToken(user.getId()))
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .build();
                return ApiResponse.<LoginResponse>builder()
                        .data(response)
                        .meta(new Meta(API_VERSION))
                        .build();
            }
        }

        throw new MyException("Login Error", "Email or password is incorrect");
    }

    private String generateToken(String uid) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(uid)
                .issuer("devblogbackend")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()))
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token for {}: {}", uid, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) {
        var token = request.getToken();
        try {
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            SignedJWT signedJWT = SignedJWT.parse(token);

            var verified = signedJWT.verify(verifier);

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            return ApiResponse.<IntrospectResponse>builder()
                    .data(new IntrospectResponse(
                            verified && expirationTime.after(new Date())))
                    .meta(new Meta(API_VERSION))
                    .build();
        } catch (JOSEException | ParseException e) {
            throw new RuntimeException(e);
        }

    }
}
