package com.cMall.feedShop.feed.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FeedCreateRequestDto {
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    
    @NotNull(message = "주문 아이템 ID는 필수입니다.")
    private Long orderItemId;
    
    // 선택 필드들
    private Long eventId;  // 이벤트 참여 시에만 사용
    
    private String instagramId;  // 인스타그램 연동 시에만 사용
    
    private List<String> hashtags;  // 해시태그 목록
    
    private List<String> imageUrls;  // 이미지 URL 목록
} 