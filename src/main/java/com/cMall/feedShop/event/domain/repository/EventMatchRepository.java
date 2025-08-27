package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.EventMatch;
import com.cMall.feedShop.event.domain.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 이벤트 매치 Repository
 * 
 * @author FeedShop Team
 * @since 1.0
 */
public interface EventMatchRepository extends JpaRepository<EventMatch, Long> {
    
    /**
     * 이벤트별 매치 목록 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.event.id = :eventId ORDER BY em.matchGroup")
    List<EventMatch> findByEventId(@Param("eventId") Long eventId);
    
    /**
     * 이벤트별 매치 목록 조회 (페이징)
     */
    @Query("SELECT em FROM EventMatch em WHERE em.event.id = :eventId ORDER BY em.matchGroup")
    Page<EventMatch> findByEventId(@Param("eventId") Long eventId, Pageable pageable);
    
    /**
     * 이벤트별 상태별 매치 목록 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.event.id = :eventId AND em.status = :status ORDER BY em.matchGroup")
    List<EventMatch> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") MatchStatus status);
    
    /**
     * 특정 매치 그룹 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.event.id = :eventId AND em.matchGroup = :matchGroup")
    Optional<EventMatch> findByEventIdAndMatchGroup(@Param("eventId") Long eventId, @Param("matchGroup") Integer matchGroup);
    
    /**
     * 피드가 참여한 매치 목록 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.participant1.id = :feedId OR em.participant2.id = :feedId")
    List<EventMatch> findByFeedId(@Param("feedId") Long feedId);
    
    /**
     * 피드가 참여한 이벤트별 매치 목록 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.event.id = :eventId AND (em.participant1.id = :feedId OR em.participant2.id = :feedId)")
    List<EventMatch> findByEventIdAndFeedId(@Param("eventId") Long eventId, @Param("feedId") Long feedId);
    
    /**
     * 피드가 우승한 매치 목록 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.winner.id = :feedId")
    List<EventMatch> findByWinnerId(@Param("feedId") Long feedId);
    
    /**
     * 이벤트별 완료된 매치 목록 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.event.id = :eventId AND em.status = 'COMPLETED' ORDER BY em.completedAt")
    List<EventMatch> findCompletedByEventId(@Param("eventId") Long eventId);
    
    /**
     * 이벤트별 진행 중인 매치 목록 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.event.id = :eventId AND em.status = 'ACTIVE' ORDER BY em.startedAt")
    List<EventMatch> findActiveByEventId(@Param("eventId") Long eventId);
    
    /**
     * 이벤트별 대기 중인 매치 목록 조회
     */
    @Query("SELECT em FROM EventMatch em WHERE em.event.id = :eventId AND em.status = 'PENDING' ORDER BY em.matchGroup")
    List<EventMatch> findPendingByEventId(@Param("eventId") Long eventId);
    
    /**
     * 이벤트별 매치 수 조회
     */
    @Query("SELECT COUNT(em) FROM EventMatch em WHERE em.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);
    
    /**
     * 이벤트별 상태별 매치 수 조회
     */
    @Query("SELECT COUNT(em) FROM EventMatch em WHERE em.event.id = :eventId AND em.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") MatchStatus status);
    
    /**
     * 다음 매치 그룹 번호 조회
     */
    @Query("SELECT COALESCE(MAX(em.matchGroup), 0) + 1 FROM EventMatch em WHERE em.event.id = :eventId")
    Integer getNextMatchGroup(@Param("eventId") Long eventId);
}
