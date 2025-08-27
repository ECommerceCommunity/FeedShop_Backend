package com.cMall.feedShop.event.application.scheduler;

import com.cMall.feedShop.event.application.service.EventStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventStatusScheduler 테스트")
class EventStatusSchedulerTest {

    @Mock
    private EventStatusService eventStatusService;

    @InjectMocks
    private EventStatusScheduler eventStatusScheduler;

    @Test
    @DisplayName("일일 이벤트 상태 업데이트 스케줄러 - 성공")
    void updateAllEventStatusesDaily_Success() {
        // Given
        doNothing().when(eventStatusService).updateAllEventStatuses();

        // When
        eventStatusScheduler.updateAllEventStatusesDaily();

        // Then
        verify(eventStatusService, times(1)).updateAllEventStatuses();
    }

    @Test
    @DisplayName("일일 이벤트 상태 업데이트 스케줄러 - 예외 발생")
    void updateAllEventStatusesDaily_Exception() {
        // Given
        doThrow(new RuntimeException("테스트 예외")).when(eventStatusService).updateAllEventStatuses();

        // When & Then
        // 예외가 발생해도 스케줄러는 계속 동작해야 함
        eventStatusScheduler.updateAllEventStatusesDaily();

        verify(eventStatusService, times(1)).updateAllEventStatuses();
    }

    @Test
    @DisplayName("시간별 이벤트 상태 확인 스케줄러")
    void checkOngoingEventStatuses() {
        // When
        eventStatusScheduler.checkOngoingEventStatuses();

        // Then
        // 현재는 로그만 출력하는 메서드이므로 검증할 내용이 없음
        // 향후 구현 시 검증 로직 추가
    }

    @Test
    @DisplayName("이벤트 상태 요약 리포트 생성 스케줄러")
    void generateEventStatusReport() {
        // When
        eventStatusScheduler.generateEventStatusReport();

        // Then
        // 현재는 TODO 주석만 있는 메서드이므로 검증할 내용이 없음
        // 향후 구현 시 검증 로직 추가
    }
}
