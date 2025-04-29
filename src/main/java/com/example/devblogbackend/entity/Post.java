package com.example.devblogbackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.HashSet;
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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "external_post_id", referencedColumnName = "id")
    private ExternalPost externalPost;

    @ManyToMany
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

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

    @OneToMany(mappedBy = "post")
    private Set<PostComment> comments = new HashSet<>();


    @Formula("(SELECT COUNT(*) FROM post_comment pc WHERE pc.post_id = id)")
    private Integer commentCount;


}
