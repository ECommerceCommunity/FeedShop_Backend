package com.cMall.feedShop.feed.domain.repository;

import com.cMall.feedShop.feed.domain.model.FeedRewardEvent;
import com.cMall.feedShop.user.domain.enums.RewardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 피드 리워드 이벤트 Repository
 * 피드 리워드 이벤트의 저장, 조회, 상태 관리 등을 담당
 */
public interface FeedRewardEventRepository extends JpaRepository<FeedRewardEvent, Long> {

    // 대기중인 리워드 이벤트 조회 (처리 대기)
    List<FeedRewardEvent> findByEventStatusOrderByCreatedAtAsc(FeedRewardEvent.EventStatus eventStatus);

    // 특정 사용자의 리워드 이벤트 조회 (페이징)
    Page<FeedRewardEvent> findByUserOrderByCreatedAtDesc(
            com.cMall.feedShop.user.domain.model.User user, 
            Pageable pageable);

    // 특정 피드의 리워드 이벤트 조회
    List<FeedRewardEvent> findByFeedOrderByCreatedAtDesc(com.cMall.feedShop.feed.domain.Feed feed);

    // 특정 사용자와 피드의 리워드 이벤트 조회
    List<FeedRewardEvent> findByUserAndFeedOrderByCreatedAtDesc(
            com.cMall.feedShop.user.domain.model.User user, 
            com.cMall.feedShop.feed.domain.Feed feed);

    // 특정 사용자의 특정 타입 리워드 이벤트 조회
    List<FeedRewardEvent> findByUserAndRewardTypeOrderByCreatedAtDesc(
            com.cMall.feedShop.user.domain.model.User user, 
            RewardType rewardType);

    // 특정 사용자의 특정 기간 리워드 이벤트 조회
    @Query("SELECT fre FROM FeedRewardEvent fre WHERE fre.user = :user " +
           "AND fre.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY fre.createdAt DESC")
    List<FeedRewardEvent> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("user") com.cMall.feedShop.user.domain.model.User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 특정 사용자의 특정 타입과 기간 리워드 이벤트 조회
    @Query("SELECT fre FROM FeedRewardEvent fre WHERE fre.user = :user " +
           "AND fre.rewardType = :rewardType " +
           "AND fre.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY fre.createdAt DESC")
    List<FeedRewardEvent> findByUserAndRewardTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("user") com.cMall.feedShop.user.domain.model.User user,
            @Param("rewardType") RewardType rewardType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 처리 실패한 리워드 이벤트 조회 (재시도용)
    List<FeedRewardEvent> findByEventStatusAndRetryCountLessThanOrderByCreatedAtAsc(
            FeedRewardEvent.EventStatus eventStatus, 
            Integer maxRetryCount);

    // 특정 사용자의 일일 리워드 이벤트 수 조회
    @Query("SELECT COUNT(fre) FROM FeedRewardEvent fre WHERE fre.user = :user " +
           "AND fre.rewardType = :rewardType " +
           "AND DATE(fre.createdAt) = DATE(:date)")
    Long countDailyEventsByUserAndType(
            @Param("user") com.cMall.feedShop.user.domain.model.User user,
            @Param("rewardType") RewardType rewardType,
            @Param("date") LocalDateTime date);

    // 특정 사용자의 월간 리워드 이벤트 수 조회
    @Query("SELECT COUNT(fre) FROM FeedRewardEvent fre WHERE fre.user = :user " +
           "AND fre.rewardType = :rewardType " +
           "AND YEAR(fre.createdAt) = YEAR(:date) " +
           "AND MONTH(fre.createdAt) = MONTH(:date)")
    Long countMonthlyEventsByUserAndType(
            @Param("user") com.cMall.feedShop.user.domain.model.User user,
            @Param("rewardType") RewardType rewardType,
            @Param("date") LocalDateTime date);

    // 특정 사용자의 특정 피드에 대한 중복 이벤트 방지 체크
    @Query("SELECT COUNT(fre) > 0 FROM FeedRewardEvent fre WHERE fre.user = :user " +
           "AND fre.feed = :feed AND fre.rewardType = :rewardType " +
           "AND fre.eventStatus IN ('PENDING', 'PROCESSING', 'PROCESSED')")
    boolean existsByUserAndFeedAndRewardTypeAndActiveStatus(
            @Param("user") com.cMall.feedShop.user.domain.model.User user,
            @Param("feed") com.cMall.feedShop.feed.domain.Feed feed,
            @Param("rewardType") RewardType rewardType);

    // 특정 사용자의 특정 피드에 대한 특정 기간 내 이벤트 존재 여부
    @Query("SELECT COUNT(fre) > 0 FROM FeedRewardEvent fre WHERE fre.user = :user " +
           "AND fre.feed = :feed AND fre.rewardType = :rewardType " +
           "AND fre.createdAt BETWEEN :startDate AND :endDate")
    boolean existsByUserAndFeedAndRewardTypeAndDateRange(
            @Param("user") com.cMall.feedShop.user.domain.model.User user,
            @Param("feed") com.cMall.feedShop.feed.domain.Feed feed,
            @Param("rewardType") RewardType rewardType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 처리 완료된 리워드 이벤트의 포인트 합계 조회
    @Query("SELECT COALESCE(SUM(fre.points), 0) FROM FeedRewardEvent fre " +
           "WHERE fre.user = :user AND fre.eventStatus = 'PROCESSED' " +
           "AND fre.createdAt BETWEEN :startDate AND :endDate")
    Integer sumProcessedPointsByUserAndDateRange(
            @Param("user") com.cMall.feedShop.user.domain.model.User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
