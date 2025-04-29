package com.example.devblogbackend.exception;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends RuntimeException {
    private String errorType;
    private String message;
}
