package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EventQueryRepository {
    Page<Event> searchEvents(EventListRequestDto requestDto, Pageable pageable);
    Optional<Event> findDetailById(Long id);

    // [Phase 1] fetchJoin으로 N+1 제거 — eventDetail + rewards 한 번에 조회
    Page<Event> findAllWithDetails(Pageable pageable);
}