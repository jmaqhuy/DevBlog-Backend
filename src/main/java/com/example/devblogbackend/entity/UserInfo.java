package com.example.devblogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_information")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    @Id
    @Column(name = "user_id")
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

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
}
