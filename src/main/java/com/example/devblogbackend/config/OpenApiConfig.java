package com.example.devblogbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Example API")
                .version("1.0")
                .description("Example API description"))
                .components(new Components()
                        .addSecuritySchemes("bearer",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                .security(List.of(new SecurityRequirement().addList("bearerAuth")));
    }

    @Bean
    public GroupedOpenApi customGroupedOpenAPI() {
        return GroupedOpenApi.builder()
                .group("api-service")
                .packagesToScan("com.example.devblogbackend.controller")
                .build();
    }


}
