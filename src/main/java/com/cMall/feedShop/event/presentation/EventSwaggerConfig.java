package com.cMall.feedShop.event.presentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Event 도메인 Swagger 설정
 * 
 * <p>Event 관련 API 문서화를 위한 별도 Swagger 설정</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Configuration
public class EventSwaggerConfig {

    @Bean
    public OpenAPI eventOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FeedShop Event API")
                        .description("FeedShop 이벤트 관리 API 문서")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
