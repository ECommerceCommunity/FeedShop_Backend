package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.response.MyFeedListResponseDto;
import com.cMall.feedShop.feed.application.service.MyFeedReadService;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;

/**
 * 마이피드 조회 REST API 컨트롤러
 * 로그인한 사용자의 피드만 조회하는 API 엔드포인트를 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/feeds/my")
@RequiredArgsConstructor
public class MyFeedReadController {

    private final MyFeedReadService myFeedReadService;
    private final UserRepository userRepository;

    /**
     * 마이피드 목록 조회 (FD-802)
     *
     * @param userDetails JWT 토큰에서 추출된 사용자 정보
     * @param feedType 피드 타입 (DAILY, EVENT, RANKING)
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @param sort 정렬 기준 (latest, popular)
     * @return 마이피드 목록 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<MyFeedListResponseDto>>> getMyFeeds(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String feedType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort) {

        // JWT 토큰에서 사용자 ID 추출
        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다."));
        }

        log.info("마이피드 목록 조회 요청 - 사용자: {}, feedType: {}, page: {}, size: {}, sort: {}",
                userId, feedType, page, size, sort);

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
        Page<MyFeedListResponseDto> feedPage;
        if (type != null) {
            feedPage = myFeedReadService.getMyFeedsByType(userId, type, pageable, userDetails);
        } else {
            feedPage = myFeedReadService.getMyFeeds(userId, pageable, userDetails);
        }

        // 응답 생성
        PaginatedResponse<MyFeedListResponseDto> response = PaginatedResponse.<MyFeedListResponseDto>builder()
                .content(feedPage.getContent())
                .page(feedPage.getNumber())
                .size(feedPage.getSize())
                .totalElements(feedPage.getTotalElements())
                .totalPages(feedPage.getTotalPages())
                .hasNext(feedPage.hasNext())
                .hasPrevious(feedPage.hasPrevious())
                .build();

        log.info("마이피드 목록 조회 완료 - 사용자: {}, 총 {}개, 현재 페이지 {}개",
                userId, response.getTotalElements(), response.getContent().size());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 마이피드 타입별 조회 (페이징)
     *
     * @param userDetails JWT 토큰에서 추출된 사용자 정보
     * @param feedType 피드 타입
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 20)
     * @param sort 정렬 기준 (latest, popular)
     * @return 마이피드 목록 페이지
     */
    @GetMapping("/type/{feedType}")
    public ResponseEntity<ApiResponse<PaginatedResponse<MyFeedListResponseDto>>> getMyFeedsByType(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String feedType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort) {

        // JWT 토큰에서 사용자 ID 추출
        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다."));
        }

        log.info("마이피드 타입별 조회 요청 - 사용자: {}, feedType: {}, page: {}, size: {}, sort: {}",
                userId, feedType, page, size, sort);

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
            Page<MyFeedListResponseDto> feedPage = myFeedReadService.getMyFeedsByType(userId, type, pageable, userDetails);

            // 응답 생성
            PaginatedResponse<MyFeedListResponseDto> response = PaginatedResponse.<MyFeedListResponseDto>builder()
                    .content(feedPage.getContent())
                    .page(feedPage.getNumber())
                    .size(feedPage.getSize())
                    .totalElements(feedPage.getTotalElements())
                    .totalPages(feedPage.getTotalPages())
                    .hasNext(feedPage.hasNext())
                    .hasPrevious(feedPage.hasPrevious())
                    .build();

            log.info("마이피드 타입별 조회 완료 - 사용자: {}, feedType: {}, 총 {}개, 현재 페이지 {}개",
                    userId, feedType, response.getTotalElements(), response.getContent().size());

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 피드 타입: {}", feedType);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("잘못된 피드 타입입니다. (DAILY, EVENT, RANKING)"));
        }
    }

    /**
     * 마이피드 개수 조회
     *
     * @param userDetails JWT 토큰에서 추출된 사용자 정보
     * @param feedType 피드 타입 (선택사항)
     * @return 마이피드 개수
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getMyFeedCount(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String feedType) {

        // JWT 토큰에서 사용자 ID 추출
        Long userId = getUserIdFromUserDetails(userDetails);
        if (userId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보를 찾을 수 없습니다."));
        }

        log.info("마이피드 개수 조회 요청 - 사용자: {}, feedType: {}", userId, feedType);

        long count;
        if (feedType != null && !feedType.isEmpty()) {
            try {
                FeedType type = FeedType.valueOf(feedType.toUpperCase());
                count = myFeedReadService.getMyFeedCountByType(userId, type);
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 피드 타입: {}", feedType);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("잘못된 피드 타입입니다. (DAILY, EVENT, RANKING)"));
            }
        } else {
            count = myFeedReadService.getMyFeedCount(userId);
        }

        log.info("마이피드 개수 조회 완료 - 사용자: {}, feedType: {}, 개수: {}", userId, feedType, count);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    /**
     * UserDetails에서 사용자 ID를 추출하는 헬퍼 메서드
     *
     * @param userDetails JWT 토큰에서 추출된 사용자 정보
     * @return 사용자 ID
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("UserDetails가 null입니다.");
            return null;
        }

        String loginId = userDetails.getUsername();
        log.debug("UserDetails에서 사용자 정보 추출 완료");

        Optional<User> userOptional = userRepository.findByLoginId(loginId);
        if (userOptional.isEmpty()) {
            log.warn("login_id로 사용자를 찾을 수 없습니다");
            return null;
        }

        User user = userOptional.get();
        log.debug("사용자 ID 추출 완료: {}", user.getId());
        return user.getId();
    }
} 