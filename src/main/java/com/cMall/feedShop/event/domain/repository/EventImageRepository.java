package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    /**
     * 이벤트 ID로 이미지 목록 조회 (순서대로)
     */
    @Query("SELECT ei FROM EventImage ei WHERE ei.event.id = :eventId ORDER BY ei.imageOrder ASC")
    List<EventImage> findByEventIdOrderByImageOrderAsc(@Param("eventId") Long eventId);

    /**
     * 이벤트 ID로 이미지 개수 조회
     */
    @Query("SELECT COUNT(ei) FROM EventImage ei WHERE ei.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);

    /**
     * 이벤트 ID로 이미지 삭제
     */
    void deleteByEventId(Long eventId);
}
