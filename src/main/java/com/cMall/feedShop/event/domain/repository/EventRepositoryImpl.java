package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
        return eventJpaRepository.findById(id);
    }

    @Override
    public Event save(Event event) {
        return eventJpaRepository.save(event);
    }

    @Override
    public void delete(Event event) {
        eventJpaRepository.delete(event);
    }

    @Override
    public List<Event> findAll() {
        return eventJpaRepository.findAll();
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        return eventJpaRepository.findAll(pageable);
    }

    @Override
    public Page<Event> searchEvents(EventListRequestDto requestDto, Pageable pageable) {
        return eventQueryRepository.searchEvents(requestDto, pageable);
    }
} 