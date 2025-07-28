package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventJpaRepository extends JpaRepository<Event, Long> {
    // Spring Data JPA 기본 메서드 사용
    
    // 소프트 딜리트 관련 메서드들
    Optional<Event> findByIdAndDeletedAtIsNull(Long id);
    List<Event> findAllByDeletedAtIsNull();
    Page<Event> findAllByDeletedAtIsNull(Pageable pageable);
} 