package com.example.devblogbackend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Data
public class Meta {
    private String apiVersion;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private LocalDateTime timestamp;

    public Meta(String apiVersion){
        this.apiVersion = apiVersion;
        this.timestamp = LocalDateTime.now();
    }
}
