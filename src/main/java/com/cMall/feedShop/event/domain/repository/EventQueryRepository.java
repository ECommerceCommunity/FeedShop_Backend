package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventQueryRepository {
    Page<Event> searchEvents(EventListRequestDto requestDto, Pageable pageable);
}