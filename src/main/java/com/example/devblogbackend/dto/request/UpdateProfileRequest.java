package com.example.devblogbackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Email(message = "Email is not valid")
    private String email;
    
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", 
            message = "Username must be 3-20 characters long and can only contain letters, numbers, and underscores")
    private String username;
    
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    private String imageLink;
} 