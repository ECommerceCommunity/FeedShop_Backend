package com.cMall.feedShop.review.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "review.image")
@Getter
@Setter
public class ReviewImageProperties {

    private String uploadPath = "feedshop-dev-bucket/images/reviews";
    private String baseUrl = "https://storage.googleapis.com";
    private long maxFileSize = 5 * 1024 * 1024; // 5MB
    private int maxImageCount = 5;
    private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "webp");
    private List<String> allowedContentTypes = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
}