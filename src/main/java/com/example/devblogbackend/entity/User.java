package com.example.devblogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean status;

    private String fullname;
    private String username;
    private String avatarLink;
    private String readme;
    private String linkin;
    private String github;
    private String website;
    private String stackOverflow;
    private Integer totalReadingDays;

    @Column(name = "registration_at")
    private java.time.LocalDateTime registrationAt;

    @ManyToMany
    @JoinTable(
            name = "user_favorite_tag",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> favoriteTags = new HashSet<>();
}
