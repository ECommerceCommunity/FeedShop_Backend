package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJpaRepository extends JpaRepository<Event, Long> {
    // Spring Data JPA 기본 메서드 사용
    
} 