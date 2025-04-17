package com.example.devblogbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareExternalPostRequest {
    private String url;
    private String userThrough;
    private List<String> tags;
}
