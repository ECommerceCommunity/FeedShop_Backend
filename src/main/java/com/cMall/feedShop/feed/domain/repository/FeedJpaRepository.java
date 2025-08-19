package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.FeedType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedJpaRepository extends JpaRepository<Feed, Long> {
    
    // 피드 생성 관련 - 중복 체크 (OrderItem의 orderItemId 필드에 접근)
    boolean existsByOrderItemOrderItemIdAndUserId(Long orderItemId, Long userId);
    
    // 사용자별 피드 조회 (페이징) - 활성 피드만
    @Query("SELECT f FROM Feed f WHERE f.user.id = :userId AND f.deletedAt IS NULL")
    Page<Feed> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // 피드 타입별 조회 (페이징) - 활성 피드만
    @Query("SELECT f FROM Feed f WHERE f.feedType = :feedType AND f.deletedAt IS NULL")
    Page<Feed> findByFeedType(@Param("feedType") FeedType feedType, Pageable pageable);
    
    // 이벤트별 피드 조회 - 활성 피드만
    @Query("SELECT f FROM Feed f WHERE f.event.id = :eventId AND f.deletedAt IS NULL")
    List<Feed> findByEventId(@Param("eventId") Long eventId);
    
    // 주문 아이템별 피드 조회 (OrderItem의 orderItemId 필드에 접근) - 활성 피드만
    @Query("SELECT f FROM Feed f WHERE f.orderItem.orderItemId = :orderItemId AND f.deletedAt IS NULL")
    List<Feed> findByOrderItemOrderItemId(@Param("orderItemId") Long orderItemId);
    
    // 이벤트별 피드 조회 (페이징) - 활성 피드만
    @Query("SELECT f FROM Feed f WHERE f.event.id = :eventId AND f.deletedAt IS NULL")
    Page<Feed> findByEventId(@Param("eventId") Long eventId, Pageable pageable);
    
    // 사용자와 이벤트별 피드 조회 - 활성 피드만
    @Query("SELECT f FROM Feed f WHERE f.user.id = :userId AND f.event.id = :eventId AND f.deletedAt IS NULL")
    List<Feed> findByUserIdAndEventId(@Param("userId") Long userId, @Param("eventId") Long eventId);
    
    // 활성 피드만 조회 (삭제되지 않은 피드)
    @Query("SELECT f FROM Feed f WHERE f.deletedAt IS NULL")
    Page<Feed> findAllActive(Pageable pageable);
    
    // 사용자별 활성 피드 조회
    @Query("SELECT f FROM Feed f WHERE f.user.id = :userId AND f.deletedAt IS NULL")
    Page<Feed> findByUserIdActive(@Param("userId") Long userId, Pageable pageable);
    
    // 피드 상세 조회 (삭제되지 않은 피드만)
    @Query("SELECT f FROM Feed f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<Feed> findDetailById(@Param("id") Long id);
    
    // 피드 상세 조회 (모든 관계 엔티티 포함, 삭제되지 않은 피드만)
    @Query("SELECT DISTINCT f FROM Feed f " +
           "LEFT JOIN FETCH f.hashtags " +
           "LEFT JOIN FETCH f.images " +
           "LEFT JOIN FETCH f.comments " +
           "LEFT JOIN FETCH f.votes " +
           "WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<Feed> findDetailWithAllById(@Param("id") Long id);
    
    // 사용자별 피드 타입 활성 조회 (마이피드용)
    @Query("SELECT f FROM Feed f WHERE f.user.id = :userId AND f.feedType = :feedType AND f.deletedAt IS NULL")
    Page<Feed> findByUserIdAndFeedTypeActive(@Param("userId") Long userId, @Param("feedType") FeedType feedType, Pageable pageable);
    
    // 사용자별 활성 피드 개수 조회 (마이피드용)
    @Query("SELECT COUNT(f) FROM Feed f WHERE f.user.id = :userId AND f.deletedAt IS NULL")
    long countByUserIdActive(@Param("userId") Long userId);
    
    // 사용자별 피드 타입 활성 개수 조회 (마이피드용)
    @Query("SELECT COUNT(f) FROM Feed f WHERE f.user.id = :userId AND f.feedType = :feedType AND f.deletedAt IS NULL")
    long countByUserIdAndFeedTypeActive(@Param("userId") Long userId, @Param("feedType") FeedType feedType);
} 