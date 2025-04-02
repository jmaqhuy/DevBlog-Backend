package com.example.devblogbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IntrospectResponse {
    private boolean tokenValid;
}
