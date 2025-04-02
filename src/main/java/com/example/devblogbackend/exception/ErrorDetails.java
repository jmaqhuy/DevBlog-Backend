package com.example.devblogbackend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ErrorDetails {
    private String type;
    private String message;
    private Map<String, String> details;
}
