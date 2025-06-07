package com.example.devblogbackend.dto.response;

import com.example.devblogbackend.dto.PostDTO;
import com.example.devblogbackend.dto.UserDTO;
import com.example.devblogbackend.entity.Tag;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    private List<PostDTO> posts;
    private List<UserDTO> users;
    private List<Tag> tags;
}
