package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventListResponseDto;
import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import com.cMall.feedShop.event.application.dto.response.EventDetailResponseDto;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventReadService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    /**
     * 전체 이벤트 목록 조회 (페이징)
     */
    public EventListResponseDto getAllEvents(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page != null ? page - 1 : 0, size != null ? size : 20);
        Page<Event> eventPage = eventRepository.findAll(pageable);
        List<EventSummaryDto> content = eventPage.getContent().stream()
                .map(eventMapper::toSummaryDto)
                .toList();
        return EventListResponseDto.builder()
                .content(content)
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .build();
    }

    /**
     * 검색 (조건 기반, QueryDSL)
     */
    public EventListResponseDto searchEvents(EventListRequestDto requestDto) {
        int page = requestDto.getPage() != null ? requestDto.getPage() - 1 : 0;
        int size = requestDto.getSize() != null ? requestDto.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventRepository.searchEvents(requestDto, pageable);
        List<EventSummaryDto> content = eventPage.getContent().stream()
                .map(eventMapper::toSummaryDto)
                .toList();
        return EventListResponseDto.builder()
                .content(content)
                .page(page + 1)
                .size(size)
                .totalElements(eventPage.getTotalElements())
                .totalPages(eventPage.getTotalPages())
                .build();
    }

    public EventDetailResponseDto getEventDetail(Long eventId) {
        Event event = eventRepository.findDetailById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        return eventMapper.toDetailDto(event);
    }
} 