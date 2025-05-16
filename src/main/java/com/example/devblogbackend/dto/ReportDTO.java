package com.example.devblogbackend.dto;

import com.example.devblogbackend.entity.Report;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportDTO {
    private Long reportId;
    private UserDTO reporter;
    private UserDTO reportedUser;
    private PostDTO reportedPost;
    private PostCommentDTO reportedComment;
    private String reason;
    private String status;
    private String result;
    private String resultDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReportDTO fromEntity(Report report) {
        ReportDTO reportDTO = ReportDTO.builder()
                .reportId(report.getReportId())
                .reporter(UserDTO.fromEntity(report.getReporter()))
                .reason(report.getReason())
                .status(report.getStatus().name())
                .resultDetails(report.getResultDetails())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
        if (report.getResult() != null) {
            reportDTO.setResult(report.getResult().name());
        }
        if (report.getReportedUser() != null) {
            reportDTO.setReportedUser(UserDTO.fromEntity(report.getReportedUser()));
        } else if (report.getReportedPost() != null) {
            reportDTO.setReportedPost(PostDTO.fromEntity(report.getReportedPost(), report.getReporter()));
        } else if (report.getReportedComment() != null) {
            reportDTO.setReportedComment(PostCommentDTO.fromEntity(report.getReportedComment()));
        }
        return reportDTO;
    }
}
