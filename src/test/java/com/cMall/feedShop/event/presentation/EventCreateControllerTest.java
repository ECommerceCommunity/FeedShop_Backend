package com.cMall.feedShop.event.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.event.application.dto.request.EventCreateRequestDto;
import com.cMall.feedShop.event.application.dto.response.EventCreateResponseDto;
import com.cMall.feedShop.event.application.service.EventCreateService;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventCreateController 테스트")
class EventCreateControllerTest {

    @Mock
    private EventCreateService eventCreateService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventCreateController eventCreateController;

    private User testUser;
    private UserDetails testUserDetails;
    private EventCreateRequestDto testRequestDto;
    private EventCreateResponseDto testResponseDto;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .loginId("admin")
                .email("admin@example.com")
                .role(UserRole.ADMIN)
                .build();

        // 테스트용 UserDetails 생성
        testUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("admin")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        // 테스트용 요청 DTO 생성
        testRequestDto = EventCreateRequestDto.builder()
                .type(EventType.RANKING)
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명입니다.")
                .participationMethod("참여 방법")
                .selectionCriteria("선정 기준")
                .precautions("주의사항")
                .purchaseStartDate(LocalDate.of(2024, 1, 1))
                .purchaseEndDate(LocalDate.of(2024, 12, 31))
                .eventStartDate(LocalDate.of(2024, 1, 1))
                .eventEndDate(LocalDate.of(2024, 12, 31))
                .announcement(LocalDate.of(2024, 12, 31))
                .maxParticipants(100)
                .rewards(Arrays.asList(
                    EventCreateRequestDto.EventRewardRequestDto.builder()
                        .conditionValue("1등")
                        .rewardValue("100")
                        .build()
                ))
                .build();

        // 테스트용 응답 DTO 생성
        testResponseDto = EventCreateResponseDto.builder()
                .eventId(1L)
                .title("테스트 이벤트")
                .type("RANKING")
                .build();
    }

    @Test
    @DisplayName("이벤트 생성 (JSON) - 성공")
    void createEvent_Success() {
        // given
        given(eventCreateService.createEvent(any(EventCreateRequestDto.class)))
                .willReturn(testResponseDto);

        // when
        ResponseEntity<ApiResponse<EventCreateResponseDto>> response = 
                eventCreateController.createEvent(testRequestDto);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("이벤트가 성공적으로 생성되었습니다.");
        assertThat(response.getBody().getData()).isEqualTo(testResponseDto);

        verify(eventCreateService).createEvent(testRequestDto);
    }

    @Test
    @DisplayName("이벤트 생성 (JSON) - 실패")
    void createEvent_Failure() {
        // given
        given(eventCreateService.createEvent(any(EventCreateRequestDto.class)))
                .willThrow(new RuntimeException("이벤트 생성 실패"));

        // when
        ResponseEntity<ApiResponse<EventCreateResponseDto>> response = 
                eventCreateController.createEvent(testRequestDto);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("이벤트 생성에 실패했습니다");

        verify(eventCreateService).createEvent(testRequestDto);
    }

    @Test
    @DisplayName("이벤트 생성 (Multipart - 이미지 포함) - 성공")
    void createEventWithImages_Success() {
        // given
        List<MockMultipartFile> mockImages = Arrays.asList(
                new MockMultipartFile("images", "test1.jpg", "image/jpeg", "test image 1".getBytes()),
                new MockMultipartFile("images", "test2.jpg", "image/jpeg", "test image 2".getBytes())
        );
        @SuppressWarnings("unchecked")
        List<org.springframework.web.multipart.MultipartFile> images = 
                (List<org.springframework.web.multipart.MultipartFile>) (List<?>) mockImages;

        given(eventCreateService.createEventWithImages(any(EventCreateRequestDto.class), any()))
                .willReturn(testResponseDto);

        // when
        ResponseEntity<ApiResponse<EventCreateResponseDto>> response = 
                eventCreateController.createEventWithImages(
                        "RANKING", "테스트 이벤트", "테스트 설명", "참여 방법", "선정 기준", "주의사항",
                        "2024-01-01", "2024-12-31", "2024-01-01", "2024-12-31", "2024-12-31",
                        100, "[{\"conditionValue\":\"1등\",\"rewardValue\":\"100\"}]", images);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("이벤트가 성공적으로 생성되었습니다.");
        assertThat(response.getBody().getData()).isEqualTo(testResponseDto);
    }

    @Test
    @DisplayName("이벤트 생성 (Multipart - 이미지 없음) - 성공")
    void createEventWithImages_NoImages_Success() {
        // given
        given(eventCreateService.createEventWithImages(any(EventCreateRequestDto.class), any()))
                .willReturn(testResponseDto);

        // when
        ResponseEntity<ApiResponse<EventCreateResponseDto>> response = 
                eventCreateController.createEventWithImages(
                        "RANKING", "테스트 이벤트", "테스트 설명", "참여 방법", "선정 기준", "주의사항",
                        "2024-01-01", "2024-12-31", "2024-01-01", "2024-12-31", "2024-12-31",
                        100, "[{\"conditionValue\":\"1등\",\"rewardValue\":\"100\"}]", null);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo(testResponseDto);
    }

    @Test
    @DisplayName("이벤트 생성 (Multipart) - 실패")
    void createEventWithImages_Failure() {
        // given
        given(eventCreateService.createEventWithImages(any(EventCreateRequestDto.class), any()))
                .willThrow(new RuntimeException("이벤트 생성 실패"));

        // when
        ResponseEntity<ApiResponse<EventCreateResponseDto>> response = 
                eventCreateController.createEventWithImages(
                        "RANKING", "테스트 이벤트", "테스트 설명", "참여 방법", "선정 기준", "주의사항",
                        "2024-01-01", "2024-12-31", "2024-01-01", "2024-12-31", "2024-12-31",
                        100, "[{\"conditionValue\":\"1등\",\"rewardValue\":\"100\"}]", null);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).contains("이벤트 생성에 실패했습니다");
    }
}
