package com.example.devblogbackend.controller.web.admin;

import com.example.devblogbackend.service.AuthService;
import com.example.devblogbackend.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ReportService reportService;
    private final AuthService authService;

    public AdminController(ReportService reportService, AuthService authService) {
        this.reportService = reportService;
        this.authService = authService;
    }

    @GetMapping("/reports")
    public String reportPage(Model model,
                             @AuthenticationPrincipal Jwt jwt) {
        model.addAttribute("defaultPassword", authService.isDefaultPassword(jwt.getSubject()));
        model.addAttribute("reports", reportService.getAllReports());
        return "reports";
    }
}
