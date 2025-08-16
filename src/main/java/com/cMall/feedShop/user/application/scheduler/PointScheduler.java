package com.cMall.feedShop.user.application.scheduler;

import com.cMall.feedShop.user.application.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointScheduler {

    private final PointService pointService;

    /**
     * 매일 자정에 만료된 포인트 처리
     * cron 표현식: 초 분 시 일 월 요일
     * 0 0 0 * * * = 매일 자정 (00:00:00)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void processExpiredPoints() {
        log.info("포인트 만료 처리 스케줄러 시작");
        
        try {
            pointService.processExpiredPoints();
            log.info("포인트 만료 처리 스케줄러 완료");
        } catch (Exception e) {
            log.error("포인트 만료 처리 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 매주 일요일 새벽 2시에 만료 예정 포인트 알림 처리 (향후 확장용)
     * cron 표현식: 0 0 2 * * 0 = 매주 일요일 02:00:00
     */
    @Scheduled(cron = "0 0 2 * * 0")
    public void processExpiringPointsNotification() {
        log.info("만료 예정 포인트 알림 처리 스케줄러 시작");
        
        try {
            // TODO: 만료 예정 포인트가 있는 사용자에게 알림 발송
            // pointService.sendExpiringPointsNotification();
            log.info("만료 예정 포인트 알림 처리 스케줄러 완료");
        } catch (Exception e) {
            log.error("만료 예정 포인트 알림 처리 스케줄러 실행 중 오류 발생", e);
        }
    }
}
