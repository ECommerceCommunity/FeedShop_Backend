package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventListResponseDto;
import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import com.cMall.feedShop.event.application.dto.response.EventDetailResponseDto;
import com.cMall.feedShop.event.application.exception.EventNotFoundException;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
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

    /**
     * 이벤트 상세 조회
     */
    public EventDetailResponseDto getEventDetail(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
        return eventMapper.toDetailDto(event);
    }

    /**
     * 피드 생성에 참여 가능한 이벤트 목록 조회
     * - 진행중인 이벤트만 조회 (실시간 상태 계산)
     * - 삭제되지 않은 이벤트만 조회
     * - 이벤트 종료일이 현재 날짜보다 미래인 이벤트만 조회
     * - 캐싱 적용 (5분간 캐시)
     */
    @Cacheable(value = "availableEvents", key = "'feed-available'", unless = "#result.isEmpty()")
    public List<EventSummaryDto> getFeedAvailableEvents() {
        LocalDate currentDate = LocalDate.now();
        
        // DB에서 필터링된 이벤트만 가져옴
        List<Event> availableEvents = eventRepository.findAvailableEvents(currentDate);
        
        return availableEvents.stream()
                // 이 부분은 calculateStatus()가 DB에서 처리할 수 없는 비즈니스 로직이라면 유지
                .filter(event -> event.calculateStatus() == EventStatus.ONGOING)
                .map(eventMapper::toSummaryDto)
                .toList();
    }
} 