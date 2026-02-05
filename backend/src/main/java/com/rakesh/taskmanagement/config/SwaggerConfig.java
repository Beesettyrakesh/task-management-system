package com.rakesh.taskmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url("https://doqueue.ddns.net")
                        .description("Production API Server (HTTPS)"))
                .info(new Info()
                        .title("Task Management API")
                        .version("1.0.0")
                        .description("Professional Task Management System with JWT Authentication\n\n" +
                                "**How to use:**\n" +
                                "1. First, use the /api/auth/login endpoint to get your JWT token\n" +
                                "2. Click the 'Authorize' button and enter: `Bearer <your-token>`\n" +
                                "3. Now you can test all protected endpoints!"))
                .addSecurityItem(new SecurityRequirement().addList("JWT"))
                .components(new Components()
                        .addSecuritySchemes("JWT", new SecurityScheme()
                                .name("JWT")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token (you'll get this from /api/auth/login)")));
    }
}
