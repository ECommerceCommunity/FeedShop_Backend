package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.EventParticipant;
import com.cMall.feedShop.event.domain.enums.ParticipationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 이벤트 참여자 Repository
 * 
 * @author FeedShop Team
 * @since 1.0
 */
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {
    
    /**
     * 이벤트별 참여자 목록 조회
     */
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.event.id = :eventId")
    List<EventParticipant> findByEventId(@Param("eventId") Long eventId);
    
    /**
     * 이벤트별 참여자 목록 조회 (페이징)
     */
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.event.id = :eventId")
    Page<EventParticipant> findByEventId(@Param("eventId") Long eventId, Pageable pageable);
    
    /**
     * 이벤트별 활성 참여자 목록 조회
     */
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.event.id = :eventId AND ep.status = :status")
    List<EventParticipant> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ParticipationStatus status);
    
    /**
     * 사용자별 참여 이벤트 목록 조회
     */
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.user.id = :userId")
    List<EventParticipant> findByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자별 참여 이벤트 목록 조회 (페이징)
     */
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.user.id = :userId")
    Page<EventParticipant> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 특정 이벤트에 사용자가 참여했는지 확인
     */
    @Query("SELECT COUNT(ep) > 0 FROM EventParticipant ep WHERE ep.event.id = :eventId AND ep.user.id = :userId")
    boolean existsByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);
    
    /**
     * 특정 이벤트와 사용자로 참여자 조회
     */
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.event.id = :eventId AND ep.user.id = :userId")
    Optional<EventParticipant> findByEventIdAndUserId(@Param("eventId") Long eventId, @Param("userId") Long userId);
    
    /**
     * 특정 이벤트와 피드로 참여자 조회
     */
    @Query("SELECT ep FROM EventParticipant ep WHERE ep.event.id = :eventId AND ep.feed.id = :feedId")
    Optional<EventParticipant> findByEventIdAndFeedId(@Param("eventId") Long eventId, @Param("feedId") Long feedId);
    
    /**
     * 이벤트별 참여자 수 조회
     */
    @Query("SELECT COUNT(ep) FROM EventParticipant ep WHERE ep.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);
    
    /**
     * 이벤트별 활성 참여자 수 조회
     */
    @Query("SELECT COUNT(ep) FROM EventParticipant ep WHERE ep.event.id = :eventId AND ep.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ParticipationStatus status);
    
    /**
     * 상태별 참여자 수 조회
     */
    @Query("SELECT COUNT(ep) FROM EventParticipant ep WHERE ep.status = :status")
    long countByStatus(@Param("status") ParticipationStatus status);
}
