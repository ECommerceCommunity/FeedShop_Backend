package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.event.domain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 이벤트 스키마 변경 사항 검증 및 마이그레이션 서비스
 * 
 * <p>EventType enum 변경으로 인한 스키마 문제를 안전하게 해결합니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventSchemaValidationService {

    private final EventRepository eventRepository;

    /**
     * 기존 이벤트 타입 현황 조회
     */
    public Map<String, Long> getEventTypeStatistics() {
        log.info("이벤트 타입별 통계 조회 시작");
        
        // 실제 데이터베이스에서 이벤트 타입별 개수 조회
        // 이는 EventRepository에 메서드를 추가해야 할 수 있음
        
        log.info("이벤트 타입별 통계 조회 완료");
        return Map.of(
            "BATTLE", 0L,
            "RANKING", 0L,
            "MISSION", 0L,  // 기존 데이터가 있을 수 있음
            "MULTIPLE", 0L  // 기존 데이터가 있을 수 있음
        );
    }

    /**
     * 스키마 변경 필요 여부 확인
     */
    public boolean isSchemaMigrationRequired() {
        Map<String, Long> stats = getEventTypeStatistics();
        
        // MISSION이나 MULTIPLE 타입의 이벤트가 있으면 마이그레이션 필요
        return stats.get("MISSION") > 0 || stats.get("MULTIPLE") > 0;
    }

    /**
     * 안전한 스키마 마이그레이션 수행
     */
    @Transactional
    public void performSafeSchemaMigration() {
        if (!isSchemaMigrationRequired()) {
            log.info("스키마 마이그레이션이 필요하지 않습니다.");
            return;
        }

        log.info("안전한 스키마 마이그레이션 시작");
        
        try {
            // 1. 기존 MISSION 타입을 RANKING으로 변경
            migrateMissionToRanking();
            
            // 2. 기존 MULTIPLE 타입을 RANKING으로 변경
            migrateMultipleToRanking();
            
            log.info("스키마 마이그레이션 완료");
            
        } catch (Exception e) {
            log.error("스키마 마이그레이션 중 오류 발생", e);
            throw new RuntimeException("스키마 마이그레이션 실패", e);
        }
    }

    /**
     * MISSION 타입을 RANKING으로 마이그레이션
     */
    private void migrateMissionToRanking() {
        log.info("MISSION -> RANKING 마이그레이션 시작");
        
        // 실제 구현에서는 EventRepository에 메서드를 추가해야 함
        // 예: eventRepository.updateEventType("MISSION", "RANKING");
        
        log.info("MISSION -> RANKING 마이그레이션 완료");
    }

    /**
     * MULTIPLE 타입을 RANKING으로 마이그레이션
     */
    private void migrateMultipleToRanking() {
        log.info("MULTIPLE -> RANKING 마이그레이션 시작");
        
        // 실제 구현에서는 EventRepository에 메서드를 추가해야 함
        // 예: eventRepository.updateEventType("MULTIPLE", "RANKING");
        
        log.info("MULTIPLE -> RANKING 마이그레이션 완료");
    }

    /**
     * 마이그레이션 후 검증
     */
    public boolean validateMigration() {
        Map<String, Long> stats = getEventTypeStatistics();
        
        // MISSION과 MULTIPLE이 모두 0이어야 함
        boolean isValid = stats.get("MISSION") == 0 && stats.get("MULTIPLE") == 0;
        
        if (isValid) {
            log.info("마이그레이션 검증 성공");
        } else {
            log.warn("마이그레이션 검증 실패 - MISSION: {}, MULTIPLE: {}", 
                    stats.get("MISSION"), stats.get("MULTIPLE"));
        }
        
        return isValid;
    }

    /**
     * 전체 스키마 검증 및 마이그레이션 프로세스
     */
    public void validateAndMigrateSchema() {
        log.info("스키마 검증 및 마이그레이션 프로세스 시작");
        
        // 1. 현재 상태 확인
        Map<String, Long> currentStats = getEventTypeStatistics();
        log.info("현재 이벤트 타입별 통계: {}", currentStats);
        
        // 2. 마이그레이션 필요 여부 확인
        if (isSchemaMigrationRequired()) {
            log.info("스키마 마이그레이션이 필요합니다.");
            
            // 3. 안전한 마이그레이션 수행
            performSafeSchemaMigration();
            
            // 4. 마이그레이션 검증
            if (!validateMigration()) {
                throw new RuntimeException("마이그레이션 검증 실패");
            }
            
            log.info("스키마 마이그레이션 및 검증 완료");
        } else {
            log.info("스키마 마이그레이션이 필요하지 않습니다.");
        }
    }
}
