package com.example.devblogbackend.exception;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessException extends RuntimeException {
    private Integer errorCode;
    private String message;
}
