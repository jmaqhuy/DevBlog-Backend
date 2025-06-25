package com.example.devblogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post", indexes = {
        @Index(name = "idx_post_publication_date", columnList = "publication_date"),
        @Index(name = "idx_post_author", columnList = "author"),
        @Index(name = "idx_post_title", columnList = "title")
})
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author")
    private User author;

    private String title;
    private String thumbnail;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "external_post_id", referencedColumnName = "id")
    private ExternalPost externalPost;

    @ManyToMany
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "publication_date")
    private LocalDateTime publicationDate;

    private Double score = 0.0;


    // statistic
    @ManyToMany
    @JoinTable(
            name = "post_like",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes = new HashSet<>();

    @Formula("(SELECT COUNT(*) FROM post_like pl WHERE pl.post_id = id)")
    private Integer likeCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PostComment> comments = new HashSet<>();


    @Formula("(SELECT COUNT(*) FROM post_comment pc WHERE pc.post_id = id)")
    private Integer commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookmark> bookmarks = new HashSet<>();

    @Formula("(SELECT COUNT(*) FROM bookmark b WHERE b.post_id = id)")
    private Integer bookmarkCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserReadHistory> readHistories = new ArrayList<>();

    @Formula("(SELECT COUNT(*) FROM user_read_history urh WHERE urh.post_id = id)")
    private Integer viewCount = 0;
}
