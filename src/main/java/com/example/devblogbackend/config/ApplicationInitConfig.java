package com.example.devblogbackend.config;

import com.example.devblogbackend.entity.User;
import com.example.devblogbackend.enums.Role;
import com.example.devblogbackend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

@Configuration
@Slf4j
public class ApplicationInitConfig {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner init(UserRepository userRepository) {
        return args -> {
            Optional<User> adminOpt = userRepository.findByUsername("admin");
            User user;

            if (adminOpt.isEmpty()) {
                Set<Role> roles = Set.of(Role.ADMIN, Role.USER);
                user = userRepository.save(
                        User.builder()
                                .email("admin@devblog.com")
                                .username("admin")
                                .roles(roles)
                                .password(passwordEncoder.encode("admin123"))
                                .build()
                );
            } else {
                user = adminOpt.get();
            }

            if (passwordEncoder.matches("admin123", user.getPassword())) {
                log.warn("""
                        
                        ==================================================
                        🚨 ADMIN USER CREATED WITH DEFAULT CREDENTIALS 🚨
                        --------------------------------------------------
                        Username : admin
                        Email    : admin@devblog.com
                        Password : admin123 (default)
                        ⚠️  For security reasons, please update the password immediately.
                        ==================================================
                        """);
            }

        };
    }
}
