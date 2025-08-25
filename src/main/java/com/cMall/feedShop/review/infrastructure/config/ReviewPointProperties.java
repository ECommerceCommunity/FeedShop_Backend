package com.cMall.feedShop.review.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 리뷰 포인트 적립 관련 설정
 */
@Component
@ConfigurationProperties(prefix = "review.points")
@Getter
@Setter
public class ReviewPointProperties {
    
    /**
     * 일반 리뷰 작성 시 적립 포인트
     */
    private int baseReward = 100;
    
    /**
     * 이미지 포함 리뷰 작성 시 추가 포인트
     */
    private int imageBonus = 50;
    
    /**
     * 고품질 리뷰(긴 내용) 작성 시 추가 포인트
     */
    private int qualityBonus = 30;
    
    /**
     * 고품질 리뷰로 인정되는 최소 글자 수
     */
    private int qualityThreshold = 100;
    
    /**
     * 포인트 적립 활성화 여부
     */
    private boolean enabled = true;
}