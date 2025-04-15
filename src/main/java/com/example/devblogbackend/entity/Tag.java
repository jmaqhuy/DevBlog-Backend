package com.example.devblogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(length = 1024)
    private String description;

    @ManyToMany(mappedBy = "favoriteTags")
    private Set<User> favoritedBy = new HashSet<>();
}
