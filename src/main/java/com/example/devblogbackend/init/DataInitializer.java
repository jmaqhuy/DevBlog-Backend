package com.example.devblogbackend.init;

import com.example.devblogbackend.service.TagService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataInitializer implements ApplicationRunner {

    private final TagService tagService;

    public DataInitializer(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        defaultTag.forEach((key, value) -> tagService.add(key, value));
    }

    private Map<String, String> defaultTag = Map.of(
        ".NET", "Explore the .NET framework for building scalable and cross-platform applications. Learn about .NET languages such as C# and F#, as well as frameworks like ASP.NET and Xamarin. Whether you're a .NET developer, software architect, or enthusiast, there's content here to help you leverage the power of .NET for your projects.",
        ".NET-Core", "A cross-platform, open-source framework for building modern, scalable, and high-performance applications. Readers can explore .NET Core's features, libraries, and tools for developing web applications, microservices, and cloud-native applications using C#, F#, and Visual Basic.",
        ".NET-MAUI", ".NET MAUI is a cross-platform framework for building native mobile and desktop applications using .NET and C# programming languages. It allows developers to create single-codebase applications targeting iOS, Android, macOS, and Windows platforms. Readers can explore how .NET MAUI simplifies cross-platform development by providing a unified API and tooling for building responsive and adaptive user interfaces across devices and platforms.",
        "3D","Explore the world of 3D graphics and visualization, including 3D modeling, rendering, and animation techniques. Learn about 3D software, workflows, and best practices for creating immersive 3D experiences."
    );
}
