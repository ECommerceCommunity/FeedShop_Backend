package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Optional<Event> findById(Long id);
    Optional<Event> findDetailById(Long id);
    Event save(Event event);
    void delete(Event event);
    List<Event> findAll();
    // 전체 조회(페이징)
    Page<Event> findAll(Pageable pageable);
    // 검색/동적 쿼리
    Page<Event> searchEvents(EventListRequestDto requestDto, Pageable pageable);
    
    /**
     * 피드 생성에 사용 가능한 이벤트 조회
     * - 삭제되지 않은 이벤트
     * - 종료일이 현재 날짜보다 미래인 이벤트
     */
    @Query("SELECT e FROM Event e WHERE e.deletedAt IS NULL AND e.eventDetail.eventEndDate > :currentDate")
    List<Event> findAvailableEvents(@Param("currentDate") LocalDate currentDate);
}
