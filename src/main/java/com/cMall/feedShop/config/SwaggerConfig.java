package com.cMall.feedShop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info; 
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() { 
        String securitySchemeName = "jwtAuth"; 
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        Components components = new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP) 
                        .scheme("bearer")
                        .bearerFormat("JWT")); 

        return new OpenAPI()
                .components(components)
                .info(apiInfo()) // ✅ API 정보 추가
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .title("FeedShop API Documentation")
                .description("FeedShop 쇼핑몰 API 문서입니다.")
                .version("1.0.0");
    }
}