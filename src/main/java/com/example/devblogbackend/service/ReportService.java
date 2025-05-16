package com.example.devblogbackend.service;

import com.example.devblogbackend.dto.ReportDTO;
import com.example.devblogbackend.entity.Post;
import com.example.devblogbackend.entity.PostComment;
import com.example.devblogbackend.entity.Report;
import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.repository.PostCommentRepository;
import com.example.devblogbackend.repository.PostRepository;
import com.example.devblogbackend.repository.ReportRepository;
import com.example.devblogbackend.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;

    public ReportService(ReportRepository reportRepository,
                         UserRepository userRepository,
                         PostRepository postRepository,
                         PostCommentRepository postCommentRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ReportDTO> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return reports.stream().map(ReportDTO::fromEntity).toList();
    }

    public Report reportUser(String userId, String reason, String reporterId) {
        User reporter = getReporter(reporterId);
        User reported = userRepository.findById(userId).orElseThrow(
                () -> new BusinessException("Report Error", "Unknown user who reported")
        );
        Report report = Report.reportUser(reporter, reported, reason);
        return saveReport(report);
    }
    public Report reportPost(Long postId, String reason, String reporterId) {
        User reporter = getReporter(reporterId);
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new BusinessException("Report Error", "Unknown post which reported")
        );
        Report report = Report.reportPost(reporter, post, reason);
        return saveReport(report);
    }
    public Report reportComment(Long cmId, String reason, String reporterId) {
        User reporter = getReporter(reporterId);
        PostComment postComment = postCommentRepository.findById(cmId).orElseThrow(
                () -> new BusinessException("Report Error", "Unknown comment which reported")
        );
        Report report = Report.reportComment(reporter, postComment, reason);
        return saveReport(report);
    }


    private Report saveReport(Report report) {
        return reportRepository.save(report);
    }

    private User getReporter(String reporterId) {
        return userRepository.findById(reporterId).orElseThrow(
                () -> new BusinessException("Report Error", "Unknown reporter")
        );
    }
}
