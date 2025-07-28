package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Optional<Event> findById(Long id);
    Event save(Event event);
    void delete(Event event);
    List<Event> findAll();
    // 전체 조회(페이징)
    Page<Event> findAll(Pageable pageable);
    // 검색/동적 쿼리
    Page<Event> searchEvents(EventListRequestDto requestDto, Pageable pageable);
}
