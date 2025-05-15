package com.example.devblogbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports",
        indexes = {
                @Index(name = "idx_reports_reporter", columnList = "reporter_id"),
                @Index(name = "idx_reports_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_post_id")
    private Post reportedPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_comment_id")
    private PostComment reportedComment;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReportResult result;

    private String resultDetails;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Report reportUser(User reporter, User reportedUser, String reason) {
        return createReport(reporter, reportedUser, null, null, reason);
    }

    public static Report reportPost(User reporter, Post reportedPost, String reason) {
        return createReport(reporter, null, reportedPost, null, reason);
    }

    public static Report reportComment(User reporter, PostComment reportedComment, String reason) {
        return createReport(reporter, null, null, reportedComment, reason);
    }

    private static Report createReport(User reporter, User reportedUser, Post reportedPost, PostComment reportedComment, String reason) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReportedPost(reportedPost);
        report.setReportedComment(reportedComment);
        report.setReason(reason);
        return report;
    }

    public enum ReportStatus {
        PENDING,
        IN_REVIEW,
        RESOLVED,
        DISMISSED
    }

    public enum ReportResult {
        APPROVED,
        REJECTED
    }
}
