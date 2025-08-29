package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cMall.feedShop.event.application.dto.request.EventListRequestDto;


@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {
    private final EventJpaRepository eventJpaRepository;
    private final EventQueryRepository eventQueryRepository;

    @Override
    public Optional<Event> findById(Long id) {
        return eventJpaRepository.findByIdAndDeletedAtIsNull(id);
    }

    @Override
    public Optional<Event> findDetailById(Long id) {
        return eventQueryRepository.findDetailById(id);
    }

    @Override
    public Event save(Event event) {
        return eventJpaRepository.save(event);
    }

    @Override
    public void delete(Event event) {
        // 하드 딜리트 대신 소프트 딜리트 사용
        event.softDelete();
        eventJpaRepository.save(event);
    }

    @Override
    public List<Event> findAll() {
        return eventJpaRepository.findAllByDeletedAtIsNull();
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        return eventJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public Page<Event> findAllByDeletedAtIsNull(Pageable pageable) {
        return eventJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public Page<Event> searchEvents(EventListRequestDto requestDto, Pageable pageable) {
        return eventQueryRepository.searchEvents(requestDto, pageable);
    }
    
    @Override
    public List<Event> findAvailableEvents(LocalDate currentDate) {
        return eventJpaRepository.findAvailableEvents(currentDate);
    }
} 