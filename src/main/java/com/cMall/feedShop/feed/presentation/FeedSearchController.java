package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedSearchRequest;
import com.cMall.feedShop.feed.application.dto.response.FeedSearchResponseDto;
import com.cMall.feedShop.feed.application.service.FeedSearchService;
import com.cMall.feedShop.feed.domain.enums.FeedType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

/**
 * 피드 검색 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedSearchController {
    
    private final FeedSearchService feedSearchService;
    
    /**
     * 피드 검색 API
     * 
     * @param q 검색 키워드 (제목, 내용, 해시태그)
     * @param authorId 작성자 ID
     * @param feedType 피드 타입 (DAILY, EVENT, RANKING)
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param productName 상품명
     * @param productId 상품 ID
     * @param eventId 이벤트 ID
     * @param eventTitle 이벤트 제목
     * @param hashtags 해시태그 목록
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @param sort 정렬 기준 (기본값: latest)
     * @param userDetails 사용자 정보 (선택적)
     * @return 검색 결과 (페이징)
     */
    @GetMapping("/search")
    @ApiResponseFormat(message = "피드 검색이 완료되었습니다.", status = 200)
    public ResponseEntity<ApiResponse<PaginatedResponse<FeedSearchResponseDto>>> searchFeeds(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) String feedType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) String eventTitle,
            @RequestParam(required = false) List<String> hashtags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("피드 검색 요청 - q: {}, authorId: {}, feedType: {}, page: {}, size: {}, sort: {}", 
                q, authorId, feedType, page, size, sort);
        
        // 검색어 길이 제한
        if (q != null && q.length() > 100) {
            log.warn("검색어 길이 초과: {}", q.length());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("검색어는 100자를 초과할 수 없습니다."));
        }
        
        // 페이지 크기 제한
        if (size > 20) {
            log.warn("페이지 크기 초과: {}", size);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("페이지 크기는 20을 초과할 수 없습니다."));
        }
        
        // 페이지 번호 검증
        if (page < 0) {
            log.warn("잘못된 페이지 번호: {}", page);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("페이지 번호는 0 이상이어야 합니다."));
        }
        
        // 정렬 옵션 검증 (프론트엔드에서 사용하는 옵션만)
        if (!Arrays.asList("latest", "popular").contains(sort)) {
            log.warn("잘못된 정렬 옵션: {}", sort);
            sort = "latest"; // 기본값으로 설정
        }
        
        // FeedType 변환
        FeedType type = null;
        if (feedType != null && !feedType.isEmpty()) {
            try {
                type = FeedType.valueOf(feedType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 피드 타입: {}", feedType);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("잘못된 피드 타입입니다. (DAILY, EVENT, RANKING)"));
            }
        }
        
        // 검색 요청 객체 생성
        FeedSearchRequest request = FeedSearchRequest.builder()
                .keyword(q)
                .authorId(authorId)
                .feedType(type)
                .startDate(startDate)
                .endDate(endDate)
                .productName(productName)
                .productId(productId)
                .eventId(eventId)
                .eventTitle(eventTitle)
                .hashtags(hashtags)
                .page(page)
                .size(size)
                .sort(sort)
                .build();
        
        // 검색 실행
        PaginatedResponse<FeedSearchResponseDto> result = feedSearchService.searchFeeds(request, userDetails);
        
        log.info("피드 검색 완료 - 총 {}개, 현재 페이지 {}개", 
                result.getTotalElements(), result.getContent().size());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
