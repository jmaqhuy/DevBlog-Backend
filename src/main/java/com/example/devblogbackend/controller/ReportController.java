package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.ReportDTO;
import com.example.devblogbackend.entity.Report;
import com.example.devblogbackend.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {
    private ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/user")
    public ApiResponse<ReportDTO> reportUser(
            @RequestParam String userId,
            @RequestParam String reason,
            @AuthenticationPrincipal Jwt jwt) {
        return buildResponse(reportService.reportUser(userId, reason, jwt.getSubject()));
    }

    @PostMapping("/post")
    public ApiResponse<ReportDTO> reportPost(
            @RequestParam Long postId,
            @RequestParam String reason,
            @AuthenticationPrincipal Jwt jwt) {
        return buildResponse(reportService.reportPost(postId, reason, jwt.getSubject()));
    }
    @PostMapping("/comment")
    public ApiResponse<ReportDTO> reportComment(
            @RequestParam Long commentId,
            @RequestParam String reason,
            @AuthenticationPrincipal Jwt jwt) {
        return buildResponse(reportService.reportPost(commentId, reason, jwt.getSubject()));
    }

    private ApiResponse<ReportDTO> buildResponse(Report report) {
        return ApiResponse.<ReportDTO>builder()
                .data(ReportDTO.fromEntity(report))
                .meta(new Meta("v1"))
                .build();
    }

}
