package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.feed.application.dto.response.MyFeedListResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 마이피드 조회 서비스
 * 로그인한 사용자의 피드만 조회하는 비즈니스 로직을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyFeedReadService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FeedMapper feedMapper;

    /**
     * 사용자의 마이피드 목록 조회 (페이징)
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 및 정렬 정보
     * @return 마이피드 목록 페이지
     */
    public Page<MyFeedListResponseDto> getMyFeeds(Long userId, Pageable pageable) {
        log.info("마이피드 목록 조회 - userId: {}, page: {}, size: {}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        // 사용자의 피드 조회
        Page<Feed> feedPage = feedRepository.findByUserId(userId, pageable);

        // Feed 엔티티를 DTO로 변환
        Page<MyFeedListResponseDto> responsePage = feedPage.map(feedMapper::toMyFeedListResponseDto);

        log.info("마이피드 목록 조회 완료 - userId: {}, 총 {}개, 현재 페이지 {}개",
                userId, responsePage.getTotalElements(), responsePage.getNumberOfElements());

        return responsePage;
    }

    /**
     * 사용자의 특정 타입 마이피드 목록 조회 (페이징)
     *
     * @param userId 사용자 ID
     * @param feedType 피드 타입
     * @param pageable 페이징 및 정렬 정보
     * @return 마이피드 목록 페이지
     */
    public Page<MyFeedListResponseDto> getMyFeedsByType(Long userId, FeedType feedType, Pageable pageable) {
        log.info("마이피드 타입별 조회 - userId: {}, feedType: {}, page: {}, size: {}",
                userId, feedType, pageable.getPageNumber(), pageable.getPageSize());

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        // 사용자의 특정 타입 피드 조회
        Page<Feed> feedPage = feedRepository.findByUserIdAndFeedType(userId, feedType.name(), pageable);

        // Feed 엔티티를 DTO로 변환
        Page<MyFeedListResponseDto> responsePage = feedPage.map(feedMapper::toMyFeedListResponseDto);

        log.info("마이피드 타입별 조회 완료 - userId: {}, feedType: {}, 총 {}개, 현재 페이지 {}개",
                userId, feedType, responsePage.getTotalElements(), responsePage.getNumberOfElements());

        return responsePage;
    }

    /**
     * 사용자의 마이피드 개수 조회
     *
     * @param userId 사용자 ID
     * @return 마이피드 개수
     */
    public long getMyFeedCount(Long userId) {
        log.info("마이피드 개수 조회 - userId: {}", userId);

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        long count = feedRepository.countByUserId(userId);

        log.info("마이피드 개수 조회 완료 - userId: {}, 개수: {}", userId, count);

        return count;
    }

    /**
     * 사용자의 특정 타입 마이피드 개수 조회
     *
     * @param userId 사용자 ID
     * @param feedType 피드 타입
     * @return 마이피드 개수
     */
    public long getMyFeedCountByType(Long userId, FeedType feedType) {
        log.info("마이피드 타입별 개수 조회 - userId: {}, feedType: {}", userId, feedType);

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedAccessDeniedException(userId, "존재하지 않는 사용자입니다."));

        long count = feedRepository.countByUserIdAndFeedType(userId, feedType.name());

        log.info("마이피드 타입별 개수 조회 완료 - userId: {}, feedType: {}, 개수: {}", userId, feedType, count);

        return count;
    }
} 