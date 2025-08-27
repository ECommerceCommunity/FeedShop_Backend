package com.cMall.feedShop.event.application.scheduler;

import com.cMall.feedShop.event.application.service.EventStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Event 상태 업데이트 스케줄러
 * 
 * <p>이벤트의 상태를 주기적으로 자동 업데이트하는 스케줄러입니다.</p>
 * 
 * @author FeedShop Team
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventStatusService eventStatusService;

    /**
     * 매일 자정에 모든 이벤트 상태 자동 업데이트
     * cron 표현식: 초 분 시 일 월 요일
     * 0 0 0 * * * = 매일 자정 (00:00:00)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void updateAllEventStatusesDaily() {
        log.info("일일 이벤트 상태 업데이트 스케줄러 시작");
        
        try {
            eventStatusService.updateAllEventStatuses();
            log.info("일일 이벤트 상태 업데이트 스케줄러 완료");
        } catch (Exception e) {
            log.error("일일 이벤트 상태 업데이트 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 매시간 정각에 진행 중인 이벤트 상태 확인
     * cron 표현식: 0 0 * * * * = 매시간 정각
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkOngoingEventStatuses() {
        log.debug("시간별 이벤트 상태 확인 스케줄러 시작");
        
        try {
            // 진행 중인 이벤트들의 상태를 확인하고 필요시 업데이트
            // 이는 더 세밀한 상태 관리가 필요한 경우를 위한 예비 구현
            log.debug("시간별 이벤트 상태 확인 스케줄러 완료");
        } catch (Exception e) {
            log.error("시간별 이벤트 상태 확인 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 매일 오전 9시에 이벤트 상태 요약 리포트 생성
     * cron 표현식: 0 0 9 * * * = 매일 오전 9시
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void generateEventStatusReport() {
        log.info("이벤트 상태 요약 리포트 생성 스케줄러 시작");
        
        try {
            // TODO: 이벤트 상태 요약 리포트 생성 및 관리자에게 전송
            // - UPCOMING: 예정된 이벤트 수
            // - ONGOING: 진행 중인 이벤트 수
            // - ENDED: 종료된 이벤트 수
            log.info("이벤트 상태 요약 리포트 생성 스케줄러 완료");
        } catch (Exception e) {
            log.error("이벤트 상태 요약 리포트 생성 스케줄러 실행 중 오류 발생", e);
        }
    }
}
