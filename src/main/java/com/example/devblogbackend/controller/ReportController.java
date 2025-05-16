package com.example.devblogbackend.controller;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.Meta;
import com.example.devblogbackend.dto.ReportDTO;
import com.example.devblogbackend.entity.Report;
import com.example.devblogbackend.exception.BusinessException;
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

    @PostMapping
    public ApiResponse<ReportDTO> report(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) Long commentId,
            @RequestParam String reason,
            @AuthenticationPrincipal Jwt jwt) {

        if (userId == null && postId == null && commentId == null) {
            throw new BusinessException("Invalid Report", "userId or postId or commentId must be not null");
        } else if (userId != null && postId != null || userId != null && commentId != null || postId != null && commentId != null) {
            throw new BusinessException("Invalid Report", "Use can report only User or Post or Comment");
        } else if (reason.isEmpty()){
            throw new BusinessException("Invalid Report", "Reason is empty");
        }
        Report report;
        if (userId != null) {
            report = reportService.reportUser(userId, reason, jwt.getSubject());
        } else if (postId != null) {
            report = reportService.reportPost(postId, reason, jwt.getSubject());
        } else {
            report = reportService.reportComment(commentId, reason, jwt.getSubject());
        }
        return ApiResponse.<ReportDTO>builder()
                .data(ReportDTO.fromEntity(report))
                .meta(new Meta("v1"))
                .build();
    }

}
