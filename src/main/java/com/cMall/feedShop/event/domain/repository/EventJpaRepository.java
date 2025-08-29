package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventJpaRepository extends JpaRepository<Event, Long> {
    // Spring Data JPA 기본 메서드 사용
    
    // 소프트 딜리트 관련 메서드들
    Optional<Event> findByIdAndDeletedAtIsNull(Long id);
    List<Event> findAllByDeletedAtIsNull();
    Page<Event> findAllByDeletedAtIsNull(Pageable pageable);
    
    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL")
    java.util.List<Event> findAllActive();

    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL")
    Page<Event> findAllActive(Pageable pageable);
    
    /**
     * 피드 생성에 사용 가능한 이벤트 조회
     * - 삭제되지 않은 이벤트
     * - 종료일이 현재 날짜와 같거나 미래인 이벤트 (종료일 포함)
     */
    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL AND e.eventDetail.eventEndDate >= :currentDate")
    List<Event> findAvailableEvents(@Param("currentDate") LocalDate currentDate);
} 