package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.EventResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 이벤트 결과 Repository
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Repository
public interface EventResultRepository extends JpaRepository<EventResult, Long> {
    
    /**
     * 이벤트 ID로 결과 조회
     */
    Optional<EventResult> findByEventId(Long eventId);
    
    /**
     * 이벤트 ID로 결과 존재 여부 확인
     */
    boolean existsByEventId(Long eventId);
    
    /**
     * 이벤트 타입별 결과 조회
     */
    @Query("SELECT er FROM EventResult er WHERE er.event.type = :eventType")
    List<EventResult> findByEventType(@Param("eventType") String eventType);
    
    /**
     * 특정 기간 내 발표된 결과 조회
     */
    @Query("SELECT er FROM EventResult er WHERE er.announcedAt BETWEEN :startDate AND :endDate")
    List<EventResult> findByAnnouncedAtBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                              @Param("endDate") java.time.LocalDateTime endDate);
}
