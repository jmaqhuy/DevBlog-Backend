package com.example.devblogbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class DevBlogBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevBlogBackendApplication.class, args);
    }

}
