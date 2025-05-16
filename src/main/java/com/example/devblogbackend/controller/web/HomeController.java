package com.example.devblogbackend.controller.web;

import com.example.devblogbackend.dto.ApiResponse;
import com.example.devblogbackend.dto.request.LoginRequest;
import com.example.devblogbackend.dto.response.LoginResponse;
import com.example.devblogbackend.enums.Role;
import com.example.devblogbackend.exception.BusinessException;
import com.example.devblogbackend.service.AuthService;
import com.example.devblogbackend.service.PostService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.File;

@Controller
public class HomeController {

    @Value("${file.apk}")
    private String apkFilePath;

    private final PostService postService;
    private final AuthService authService;
    public HomeController(PostService postService,
                          AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("listPosts", postService.getSamplePosts());
        return "index";
    }

    @GetMapping("/download-apk")
    public ResponseEntity<Resource> downloadApk() {
        System.out.println("APK file path: {}" + apkFilePath);
        File file = new File(apkFilePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {
            if (auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                return "redirect:/admin/reports";
            else {
                return "redirect:/";
            }
        }
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@ModelAttribute @Valid LoginRequest loginRequest,
                          BindingResult br,
                          HttpServletResponse response,
                          Model model) {
        if (br.hasErrors()) {
            return "login";
        }

        try {
            ApiResponse<LoginResponse> apiResp = authService.loginUser(loginRequest);

            String token = apiResp.getData().getToken();

            Cookie cookie = new Cookie("JWT", token);
            cookie.setMaxAge(60*60);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setSecure(true);
            response.addCookie(cookie);
            if (!apiResp.getData().getUserInfo().getRoles().contains(Role.ADMIN)) {
                return "redirect:/";
            }
            return "redirect:/admin/reports";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login";
        }
    }
}
