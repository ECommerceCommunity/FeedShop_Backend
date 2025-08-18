package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import com.cMall.feedShop.feed.application.service.FeedReadService;
import com.cMall.feedShop.feed.domain.FeedType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 피드 조회 REST API 컨트롤러
 * 피드 목록 조회 등의 API 엔드포인트를 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedReadController {
    
    private final FeedReadService feedReadService;
    
    /**
     * 피드 전체 목록 조회 (FD-801)
     * 
     * @param feedType 피드 타입 (DAILY, EVENT, RANKING)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @param sort 정렬 기준 (latest, popular)
     * @param userDetails 사용자 정보 (선택적)
     * @return 피드 목록 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<FeedListResponseDto>>> getFeeds(
            @RequestParam(required = false) String feedType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("피드 목록 조회 요청 - feedType: {}, page: {}, size: {}, sort: {}", 
                feedType, page, size, sort);
        
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
        
        // 정렬 설정
        Sort sortConfig;
        if ("popular".equalsIgnoreCase(sort)) {
            sortConfig = Sort.by(Sort.Direction.DESC, "likeCount");
        } else {
            // 기본값: 최신순
            sortConfig = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        // 페이징 설정
        Pageable pageable = PageRequest.of(page, size, sortConfig);
        
        // 서비스 호출
        Page<FeedListResponseDto> feedPage = feedReadService.getFeeds(type, pageable, userDetails);
        
        // 응답 생성
        PaginatedResponse<FeedListResponseDto> response = PaginatedResponse.<FeedListResponseDto>builder()
                .content(feedPage.getContent())
                .page(feedPage.getNumber())
                .size(feedPage.getSize())
                .totalElements(feedPage.getTotalElements())
                .totalPages(feedPage.getTotalPages())
                .hasNext(feedPage.hasNext())
                .hasPrevious(feedPage.hasPrevious())
                .build();
        
        log.info("피드 목록 조회 완료 - 총 {}개, 현재 페이지 {}개", 
                response.getTotalElements(), response.getContent().size());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 피드 타입별 조회 (페이징)
     * 
     * @param feedType 피드 타입
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @param sort 정렬 기준 (latest, popular)
     * @param userDetails 사용자 정보 (선택적)
     * @return 피드 목록 페이지
     */
    @GetMapping("/type/{feedType}")
    public ResponseEntity<ApiResponse<PaginatedResponse<FeedListResponseDto>>> getFeedsByType(
            @PathVariable String feedType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("피드 타입별 조회 요청 - feedType: {}, page: {}, size: {}, sort: {}", 
                feedType, page, size, sort);
        
        try {
            FeedType type = FeedType.valueOf(feedType.toUpperCase());
            
            // 정렬 설정
            Sort sortConfig;
            if ("popular".equalsIgnoreCase(sort)) {
                sortConfig = Sort.by(Sort.Direction.DESC, "likeCount");
            } else {
                // 기본값: 최신순
                sortConfig = Sort.by(Sort.Direction.DESC, "createdAt");
            }
            
            // 페이징 설정
            Pageable pageable = PageRequest.of(page, size, sortConfig);
            
            // 서비스 호출
            Page<FeedListResponseDto> feedPage = feedReadService.getFeedsByType(type, pageable, userDetails);
            
            // 응답 생성
            PaginatedResponse<FeedListResponseDto> response = PaginatedResponse.<FeedListResponseDto>builder()
                    .content(feedPage.getContent())
                    .page(feedPage.getNumber())
                    .size(feedPage.getSize())
                    .totalElements(feedPage.getTotalElements())
                    .totalPages(feedPage.getTotalPages())
                    .hasNext(feedPage.hasNext())
                    .hasPrevious(feedPage.hasPrevious())
                    .build();
            
            log.info("피드 타입별 조회 완료 - feedType: {}, 총 {}개, 현재 페이지 {}개", 
                    feedType, response.getTotalElements(), response.getContent().size());
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 피드 타입: {}", feedType);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 피드 타입입니다. (DAILY, EVENT, RANKING)"));
        }
    }
} 