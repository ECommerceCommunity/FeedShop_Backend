package com.cMall.feedShop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 전체 파일 업로드 관련 웹 설정
 * - 리뷰 이미지: /uploads/images/reviews/**
 * - 상품 이미지: /uploads/images/products/** (추후 추가)
 * - 사용자 프로필: /uploads/images/users/** (추후 추가)
 * - 기타 파일: /uploads/** (전체)
 */
@Configuration
public class ImageWebConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path:./uploads}")
    private String baseUploadPath;

    @Value("${file.upload.cache-period:3600}")
    private int cachePeriod;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 모든 업로드 파일에 대한 통합 경로 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + baseUploadPath + "/")
                .setCachePeriod(cachePeriod);

        // 로그로 설정된 경로 확인
        System.out.println("📁 이미지 업로드 경로 설정 완료: " + baseUploadPath);
    }
}