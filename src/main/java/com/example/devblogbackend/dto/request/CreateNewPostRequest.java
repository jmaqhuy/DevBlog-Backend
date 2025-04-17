package com.example.devblogbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateNewPostRequest {
    private String title;
    private String thumbnail;
    private String content;
    private List<String> tags;
}
