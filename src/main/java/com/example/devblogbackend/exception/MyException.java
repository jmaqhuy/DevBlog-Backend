package com.example.devblogbackend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MyException extends RuntimeException {
    private String errorType;
    private String message;
}
