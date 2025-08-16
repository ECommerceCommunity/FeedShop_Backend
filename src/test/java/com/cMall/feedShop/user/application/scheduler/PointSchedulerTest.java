package com.cMall.feedShop.user.application.scheduler;

import com.cMall.feedShop.user.application.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointScheduler 테스트")
class PointSchedulerTest {

    @Mock
    private PointService pointService;

    @InjectMocks
    private PointScheduler pointScheduler;

    @Test
    @DisplayName("포인트 만료 처리 스케줄러 실행 성공")
    void processExpiredPoints_Success() {
        // given
        doNothing().when(pointService).processExpiredPoints();

        // when
        pointScheduler.processExpiredPoints();

        // then
        verify(pointService, times(1)).processExpiredPoints();
    }

    @Test
    @DisplayName("포인트 만료 처리 스케줄러 실행 중 예외 발생 시 로그 기록")
    void processExpiredPoints_Exception_LogsError() {
        // given
        RuntimeException exception = new RuntimeException("테스트 예외");
        doThrow(exception).when(pointService).processExpiredPoints();

        // when
        pointScheduler.processExpiredPoints();

        // then
        verify(pointService, times(1)).processExpiredPoints();
        // 예외가 발생해도 스케줄러는 계속 동작해야 함
    }

    @Test
    @DisplayName("만료 예정 포인트 알림 처리 스케줄러 실행 성공")
    void processExpiringPointsNotification_Success() {
        // when
        pointScheduler.processExpiringPointsNotification();

        // then
        // 현재는 TODO 상태이므로 아무 동작도 하지 않음
        // 향후 구현 시 pointService.sendExpiringPointsNotification() 호출 검증
    }
}
