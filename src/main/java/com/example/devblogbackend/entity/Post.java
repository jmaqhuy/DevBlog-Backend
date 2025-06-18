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


    // statistic
    @ManyToMany
    @JoinTable(
            name = "post_like",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likes = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PostComment> comments = new HashSet<>();


    @Formula("(SELECT COUNT(*) FROM post_comment pc WHERE pc.post_id = id)")
    private Integer commentCount = 0;

    @Formula( "( " +
            "   (SELECT COUNT(DISTINCT ur.user_id) FROM user_read_history ur WHERE ur.post_id = id) * 1.0 " +
            " + (SELECT COUNT(DISTINCT l.user_id) FROM post_like l WHERE l.post_id = id) * 3.0 " +
            " + (SELECT COUNT(DISTINCT c.id) FROM post_comment c WHERE c.post_id = id) * 5.0 " +
            " + (SELECT COUNT(DISTINCT b.user_id) FROM bookmark b WHERE b.post_id = id) * 10.0 " +
            ") * EXP(-DATEDIFF(CURRENT_DATE, publication_date)/10.1)")
    private Double score = 0.0;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bookmark> bookmarks = new HashSet<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserReadHistory> readHistories = new ArrayList<>();
}
