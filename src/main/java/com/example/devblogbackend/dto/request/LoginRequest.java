package com.example.devblogbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @NotNull
    @Email(message = "Email is not valid")
    private String email;

    @NotBlank
    @NotNull
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
