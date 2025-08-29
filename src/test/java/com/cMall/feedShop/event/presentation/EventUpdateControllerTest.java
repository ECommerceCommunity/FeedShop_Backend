package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.dto.request.EventUpdateRequestDto;
import com.cMall.feedShop.event.application.service.EventUpdateService;
import com.cMall.feedShop.event.domain.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventUpdateController 테스트")
class EventUpdateControllerTest {

    @Mock
    private EventUpdateService eventUpdateService;

    @InjectMocks
    private EventUpdateController eventUpdateController;

    private EventUpdateRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        // 테스트용 요청 DTO 생성
        testRequestDto = EventUpdateRequestDto.builder()
                .eventId(1L)
                .type(EventType.RANKING)
                .title("수정된 테스트 이벤트")
                .description("수정된 테스트 이벤트 설명입니다.")
                .participationMethod("수정된 참여 방법")
                .selectionCriteria("수정된 선정 기준")
                .precautions("수정된 주의사항")
                .purchaseStartDate(LocalDate.of(2024, 1, 1))
                .purchaseEndDate(LocalDate.of(2024, 12, 31))
                .eventStartDate(LocalDate.of(2024, 1, 1))
                .eventEndDate(LocalDate.of(2024, 12, 31))
                .announcement(LocalDate.of(2024, 12, 31))
                .maxParticipants(100)
                .rewards("[{\"conditionValue\":\"1등\",\"rewardValue\":\"100\"}]")
                .build();
    }

    @Test
    @DisplayName("이벤트 수정 (JSON) - 성공")
    void updateEvent_Success() {
        // given
        Long eventId = 1L;

        // when
        ResponseEntity<ApiResponse<Void>> response = eventUpdateController.updateEvent(eventId, testRequestDto);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("이벤트가 성공적으로 수정되었습니다.");

        verify(eventUpdateService).updateEvent(testRequestDto);
        assertThat(testRequestDto.getEventId()).isEqualTo(eventId);
    }

    @Test
    @DisplayName("이벤트 수정 (Multipart - 이미지 포함) - 성공")
    void updateEventWithImages_Success() {
        // given
        Long eventId = 1L;
        List<MockMultipartFile> mockImages = Arrays.asList(
                new MockMultipartFile("images", "test1.jpg", "image/jpeg", "test image 1".getBytes()),
                new MockMultipartFile("images", "test2.jpg", "image/jpeg", "test image 2".getBytes())
        );
        @SuppressWarnings("unchecked")
        List<org.springframework.web.multipart.MultipartFile> images = 
                (List<org.springframework.web.multipart.MultipartFile>) (List<?>) mockImages;

        // when
        ResponseEntity<ApiResponse<Void>> response = eventUpdateController.updateEventWithImages(
                eventId, "RANKING", "수정된 테스트 이벤트", "수정된 설명", "참여 방법", "선정 기준", 
                "주의사항", "2024-01-01", "2024-12-31", "2024-01-01", "2024-12-31", "2024-12-31", 
                "100", "[{\"conditionValue\":\"1등\",\"rewardValue\":\"100\"}]", images);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("이벤트가 성공적으로 수정되었습니다.");

        verify(eventUpdateService).updateEventWithImages(any(EventUpdateRequestDto.class), eq(images));
    }

    @Test
    @DisplayName("이벤트 수정 (Multipart - 이미지 없음) - 성공")
    void updateEventWithImages_NoImages_Success() {
        // given
        Long eventId = 1L;

        // when
        ResponseEntity<ApiResponse<Void>> response = eventUpdateController.updateEventWithImages(
                eventId, "RANKING", "수정된 테스트 이벤트", "수정된 설명", "참여 방법", "선정 기준", 
                "주의사항", "2024-01-01", "2024-12-31", "2024-01-01", "2024-12-31", "2024-12-31", 
                "100", "[{\"conditionValue\":\"1등\",\"rewardValue\":\"100\"}]", null);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("이벤트가 성공적으로 수정되었습니다.");

        verify(eventUpdateService).updateEventWithImages(any(EventUpdateRequestDto.class), eq(null));
    }
}
