package com.example.devblogbackend.dto;

public interface TagScoreProjection {
    int getTagId();
    String getTagName();
    double getTotalScore();
}
