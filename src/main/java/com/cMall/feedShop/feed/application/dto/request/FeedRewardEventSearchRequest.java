package com.cMall.feedShop.feed.application.dto.request;

import com.cMall.feedShop.user.domain.enums.RewardType;
import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

/**
 * 피드 리워드 이벤트 조회 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedRewardEventSearchRequest {

    // 페이지네이션
    private Integer page;
    private Integer size;
    
    // 필터링
    private Long userId;
    private Long feedId;
    private RewardType rewardType;
    private FeedRewardEvent.EventStatus eventStatus;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // 정렬
    private String sortBy;
    private String sortDirection;

    // 기본값 설정을 위한 정적 메서드
    public static FeedRewardEventSearchRequest getDefault() {
        return FeedRewardEventSearchRequest.builder()
                .page(0)
                .size(20)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();
    }

    /**
     * Pageable 객체 생성
     */
    public Pageable toPageable() {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        return PageRequest.of(page, size, sort);
    }

    /**
     * 유효성 검사
     */
    public boolean isValid() {
        return page != null && page >= 0 
            && size != null && size > 0 && size <= 100
            && (sortDirection == null || sortDirection.matches("^(ASC|DESC)$"));
    }
}
