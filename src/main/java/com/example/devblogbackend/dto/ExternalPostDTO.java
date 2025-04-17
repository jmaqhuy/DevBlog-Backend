package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.ExternalPost;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalPostDTO {
    private String domain;
    private String path;

    private String webLogo;
    private String title;
    private String thumbnail;

    public static ExternalPostDTO fromEntity(ExternalPost externalPost) {
        return new ExternalPostDTO(
                externalPost.getDomain(),
                externalPost.getPath(),
                externalPost.getWebLogo(),
                externalPost.getTitle(),
                externalPost.getThumbnail()
        );
    }
}
