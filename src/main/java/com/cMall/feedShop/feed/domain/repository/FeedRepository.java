package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.Feed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FeedRepository {
    
    // 기본 CRUD
    Feed save(Feed feed);
    Optional<Feed> findById(Long id);
    void delete(Feed feed);
    
    // 피드 생성 관련
    boolean existsByOrderItemIdAndUserId(Long orderItemId, Long userId);
    
    // 피드 목록 조회 (페이징)
    Page<Feed> findAll(Pageable pageable);
    
    // 사용자별 피드 조회
    Page<Feed> findByUserId(Long userId, Pageable pageable);
    
    // 피드 타입별 조회
    Page<Feed> findByFeedType(String feedType, Pageable pageable);
    
    // 이벤트별 피드 조회
    List<Feed> findByEventId(Long eventId);
    
    // 주문 아이템별 피드 조회
    List<Feed> findByOrderItemId(Long orderItemId);
    
    // 피드 상세 조회 (삭제되지 않은 피드만)
    Optional<Feed> findDetailById(Long id);
    
    // 피드 상세 조회 (모든 관계 엔티티 포함)
    Optional<Feed> findDetailWithAllById(Long id);
} 