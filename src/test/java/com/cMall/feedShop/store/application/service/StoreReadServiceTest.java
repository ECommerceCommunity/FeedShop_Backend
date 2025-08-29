package com.cMall.feedShop.store.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.store.application.dto.response.StoreDetailResponse;
import com.cMall.feedShop.store.domain.exception.StoreException;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * StoreService 단위 테스트
 *
 * 테스트 목표:
 * 1. 정상적인 가게 상세 조회 성공
 * 2. 사용자 없음 예외 처리
 * 3. 가게 없음 예외 처리
 * 4. 응답 데이터 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 단위 테스트")
class StoreReadServiceTest {

    @Mock
    private StoreRepository storeRepository;


    @InjectMocks
    private StoreReadService storeReadService;

    // 테스트 데이터
    private Store store;
    private final Long USER_ID = 1L;
    private final Long STORE_ID = 1L;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    /**
     * 테스트 데이터 초기화
     */
    private void setupTestData() {
        // 가게 생성
        store = Store.builder()
                .storeName("테스트 스토어")
                .description("테스트 스토어입니다.")
                .logo("https://test-logo-url.com")
                .sellerId(USER_ID)
                .build();
        ReflectionTestUtils.setField(store, "storeId", STORE_ID);

        // 생성/수정 시간 설정
        LocalDateTime now = LocalDateTime.of(2025, 7, 10, 0, 0, 0);
        ReflectionTestUtils.setField(store, "createdAt", now);
        ReflectionTestUtils.setField(store, "updatedAt", now);
    }

    @Test
    @DisplayName("가게 상세 조회 성공")
    void getMyStoreDetail_Success() {
        // given
        given(storeRepository.findBySellerId(USER_ID))
                .willReturn(Optional.of(store));

        // when
        StoreDetailResponse result = storeReadService.getMyStoreDetail(USER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStoreId()).isEqualTo(STORE_ID);
        assertThat(result.getStoreName()).isEqualTo("테스트 스토어");
        assertThat(result.getDescription()).isEqualTo("테스트 스토어입니다.");
        assertThat(result.getLogo()).isEqualTo("https://test-logo-url.com");
        assertThat(result.getSellerId()).isEqualTo(USER_ID);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 7, 10, 0, 0, 0));
        assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 7, 10, 0, 0, 0));

        // 메서드 호출 검증
        verify(storeRepository, times(1)).findBySellerId(USER_ID);
    }


    @Test
    @DisplayName("가게를 찾을 수 없음 - StoreException 발생")
    void getMyStoreDetail_StoreNotFound() {
        // given
        given(storeRepository.findBySellerId(USER_ID))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeReadService.getMyStoreDetail(USER_ID))
                .isInstanceOf(StoreException.class)
                .hasMessage(ErrorCode.STORE_NOT_FOUND.getMessage());

        // 메서드 호출 검증
        verify(storeRepository, times(1)).findBySellerId(USER_ID);
    }


    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회 시도")
    void getMyStoreDetail_NonExistentUserId() {
        // given
        Long nonExistentUserId = 999L;
        given(storeRepository.findBySellerId(nonExistentUserId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeReadService.getMyStoreDetail(nonExistentUserId))
                .isInstanceOf(StoreException.class)
                .hasMessage(ErrorCode.STORE_NOT_FOUND.getMessage());

        verify(storeRepository, times(1)).findBySellerId(nonExistentUserId);
    }

    @Test
    @DisplayName("사용자는 존재하지만 가게가 없는 경우")
    void getMyStoreDetail_UserExistsButNoStore() {
        // given
        Long userIdWithoutStore = 100L;
        given(storeRepository.findBySellerId(userIdWithoutStore))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeReadService.getMyStoreDetail(userIdWithoutStore))
                .isInstanceOf(StoreException.class)
                .hasMessage(ErrorCode.STORE_NOT_FOUND.getMessage());

        verify(storeRepository, times(1)).findBySellerId(userIdWithoutStore);
    }

    @Test
    @DisplayName("일반 사용자(buyer)의 가게 조회 시도")
    void getMyStoreDetail_BuyerUser() {
        // given
        Long buyerId = 2L;
        given(storeRepository.findBySellerId(buyerId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeReadService.getMyStoreDetail(buyerId))
                .isInstanceOf(StoreException.class)
                .hasMessage(ErrorCode.STORE_NOT_FOUND.getMessage());

        verify(storeRepository, times(1)).findBySellerId(buyerId);
    }

    @Test
    @DisplayName("가게 정보가 null인 경우 처리")
    void getMyStoreDetail_StoreWithNullFields() {
        // given
        Store storeWithNulls = Store.builder()
                .storeName("테스트 스토어")
                .description(null) // null 설명
                .logo(null) // null 로고
                .sellerId(USER_ID)
                .build();
        ReflectionTestUtils.setField(storeWithNulls, "storeId", STORE_ID);

        given(storeRepository.findBySellerId(USER_ID))
                .willReturn(Optional.of(storeWithNulls));

        // when
        StoreDetailResponse result = storeReadService.getMyStoreDetail(USER_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStoreId()).isEqualTo(STORE_ID);
        assertThat(result.getStoreName()).isEqualTo("테스트 스토어");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getLogo()).isNull();
        assertThat(result.getSellerId()).isEqualTo(USER_ID);
    }
}

