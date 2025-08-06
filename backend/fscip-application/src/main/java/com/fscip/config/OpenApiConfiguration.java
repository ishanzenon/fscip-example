package com.fscip.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for FSCIP application
 * Defines API documentation and security schemes
 * Follows coding standards requirements for API documentation
 */
@Configuration
public class OpenApiConfiguration {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${spring.application.name:fscip-backend}")
    private String applicationName;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080" + contextPath)
                    .description("Local development server"),
                new Server()
                    .url("https://dev-api.fscip.com" + contextPath)
                    .description("Development server"),
                new Server()
                    .url("https://api.fscip.com" + contextPath)
                    .description("Production server")
            ))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", createBearerJwtSecurityScheme())
                .addSecuritySchemes("oauth2", createOAuth2SecurityScheme())
            )
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
            .addSecurityItem(new SecurityRequirement().addList("oauth2"));
    }

    private Info apiInfo() {
        return new Info()
            .title("FSCIP Backend API")
            .description("Financial Services Customer Interaction Portal - Backend API Documentation")
            .version("1.0.0")
            .contact(new Contact()
                .name("FSCIP Development Team")
                .email("dev-team@fscip.com")
                .url("https://fscip.com/contact")
            )
            .license(new License()
                .name("Proprietary")
                .url("https://fscip.com/license")
            );
    }

    private SecurityScheme createBearerJwtSecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT Bearer token authentication")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");
    }

    private SecurityScheme createOAuth2SecurityScheme() {
        return new SecurityScheme()
            .type(SecurityScheme.Type.OAUTH2)
            .description("OAuth2 authentication with Keycloak")
            .flows(new io.swagger.v3.oas.models.security.OAuthFlows()
                .authorizationCode(new io.swagger.v3.oas.models.security.OAuthFlow()
                    .authorizationUrl("http://localhost:8081/realms/fscip/protocol/openid-connect/auth")
                    .tokenUrl("http://localhost:8081/realms/fscip/protocol/openid-connect/token")
                    .scopes(new io.swagger.v3.oas.models.security.Scopes()
                        .addString("openid", "OpenID Connect")
                        .addString("profile", "User profile information")
                        .addString("email", "User email address")
                        .addString("roles", "User roles and permissions")
                    )
                )
            );
    }
}