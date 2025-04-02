package com.example.devblogbackend.dto;

import com.example.devblogbackend.exception.ErrorDetails;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T> {
    private ErrorDetails error;
    private T data;
    private Meta meta;
}
