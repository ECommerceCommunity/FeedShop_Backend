package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.service.EventDeleteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventDeleteController 테스트")
class EventDeleteControllerTest {

    @Mock
    private EventDeleteService eventDeleteService;

    @InjectMocks
    private EventDeleteController eventDeleteController;

    @Test
    @DisplayName("이벤트 삭제 - 성공")
    void deleteEvent_Success() {
        // given
        Long eventId = 1L;

        // when
        ResponseEntity<ApiResponse<Void>> response = eventDeleteController.deleteEvent(eventId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("이벤트가 성공적으로 삭제되었습니다.");

        verify(eventDeleteService).deleteEvent(eq(eventId));
    }

    @Test
    @DisplayName("이벤트 삭제 - 다른 ID")
    void deleteEvent_WithDifferentId() {
        // given
        Long eventId = 999L;

        // when
        ResponseEntity<ApiResponse<Void>> response = eventDeleteController.deleteEvent(eventId);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("이벤트가 성공적으로 삭제되었습니다.");

        verify(eventDeleteService).deleteEvent(eq(eventId));
    }
}
