package com.example.devblogbackend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Meta {
    private String apiVersion;
    private LocalDateTime timestamp;

    public Meta(String apiVersion){
        this.apiVersion = apiVersion;
        this.timestamp = LocalDateTime.now();
    }
}
