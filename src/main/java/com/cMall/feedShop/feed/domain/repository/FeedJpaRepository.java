package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedJpaRepository extends JpaRepository<Feed, Long> {
    
    // 피드 생성 관련 - 중복 체크 (OrderItem의 orderItemId 필드에 접근)
    boolean existsByOrderItemOrderItemIdAndUserId(Long orderItemId, Long userId);
    
    // 사용자별 피드 조회 (페이징)
    Page<Feed> findByUserId(Long userId, Pageable pageable);
    
    // 피드 타입별 조회 (페이징)
    Page<Feed> findByFeedType(FeedType feedType, Pageable pageable);
    
    // 이벤트별 피드 조회
    List<Feed> findByEventId(Long eventId);
    
    // 주문 아이템별 피드 조회 (OrderItem의 orderItemId 필드에 접근)
    List<Feed> findByOrderItemOrderItemId(Long orderItemId);
    
    // 이벤트별 피드 조회 (페이징)
    Page<Feed> findByEventId(Long eventId, Pageable pageable);
    
    // 사용자와 이벤트별 피드 조회
    @Query("SELECT f FROM Feed f WHERE f.user.id = :userId AND f.event.id = :eventId")
    List<Feed> findByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);
    
    // 활성 피드만 조회 (삭제되지 않은 피드)
    @Query("SELECT f FROM Feed f WHERE f.deletedAt IS NULL")
    Page<Feed> findAllActive(Pageable pageable);
    
    // 사용자별 활성 피드 조회
    @Query("SELECT f FROM Feed f WHERE f.user.id = :userId AND f.deletedAt IS NULL")
    Page<Feed> findByUserIdActive(@Param("userId") Long userId, Pageable pageable);
} 