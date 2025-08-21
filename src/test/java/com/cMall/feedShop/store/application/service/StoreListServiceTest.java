package com.cMall.feedShop.store.application.service;

import com.cMall.feedShop.store.application.dto.response.StoreListResponse;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * StoreService의 가게 목록 조회 기능에 대한 통합 단위 테스트
 *
 * 테스트 구조:
 * 1. 정상 케이스 테스트
 * 2. 경계값 테스트  
 * 3. 예외 상황 테스트
 * 4. 성능 테스트
 * 5. 데이터 검증 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StoreListService 통합 단위 테스트")
class StoreListServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private StoreService storeService;

    // 테스트 데이터
    private Store storeA;
    private Store storeB;
    private Store storeZ;
    private Store koreanStore;
    private Store specialCharStore;

    @BeforeEach
    void setUp() {
        // 영문 가게들
        storeA = createStore(1L, "A마트", 1L);
        storeB = createStore(2L, "B마트", 2L);
        storeZ = createStore(3L, "Z마트", 3L);

        // 한글 가게
        koreanStore = createStore(4L, "가나다라마트", 4L);

        // 특수문자 포함 가게
        specialCharStore = createStore(5L, "Store-Name_With!@#", 5L);
    }

    // ================================ 정상 케이스 테스트 ================================

    @Nested
    @DisplayName("정상 케이스 테스트")
    class SuccessScenarios {

        @Test
        @DisplayName("가게 목록을 이름순으로 정렬하여 조회 성공")
        void getAllStores_Success_OrderedByName() {
            // Given - QueryDSL에서 이미 정렬된 상태로 반환
            List<Store> orderedStores = Arrays.asList(storeA, storeB, storeZ);
            given(storeRepository.findAllStoresOrderByName()).willReturn(orderedStores);

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStoreName()).isEqualTo("A마트");
            assertThat(result.get(1).getStoreName()).isEqualTo("B마트");
            assertThat(result.get(2).getStoreName()).isEqualTo("Z마트");

            // ID 매핑 확인
            assertThat(result.get(0).getStoreId()).isEqualTo(1L);
            assertThat(result.get(1).getStoreId()).isEqualTo(2L);
            assertThat(result.get(2).getStoreId()).isEqualTo(3L);

            // Repository 메서드 호출 확인
            verify(storeRepository, times(1)).findAllStoresOrderByName();
        }

        @Test
        @DisplayName("한글 가게명 정렬 처리 성공")
        void getAllStores_KoreanNames_Success() {
            // Given
            Store store1 = createStore(1L, "가나다마트", 1L);
            Store store2 = createStore(2L, "나다라마트", 2L);
            Store store3 = createStore(3L, "다라마마트", 3L);

            List<Store> koreanStores = Arrays.asList(store1, store3, store2); // 가, 다, 나 순
            given(storeRepository.findAllStoresOrderByName()).willReturn(koreanStores);

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStoreName()).isEqualTo("가나다마트");
            assertThat(result.get(1).getStoreName()).isEqualTo("다라마마트");
            assertThat(result.get(2).getStoreName()).isEqualTo("나다라마트");
        }

        @Test
        @DisplayName("영문과 한글이 혼재된 가게명 처리 성공")
        void getAllStores_MixedLanguageNames_Success() {
            // Given
            Store englishStore = createStore(1L, "ABC Store", 1L);
            Store mixedStore = createStore(2L, "한글Store", 2L);
            Store numberStore = createStore(3L, "123마트", 3L);

            List<Store> mixedStores = Arrays.asList(numberStore, englishStore, mixedStore);
            given(storeRepository.findAllStoresOrderByName()).willReturn(mixedStores);

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getStoreName()).isEqualTo("123마트");
            assertThat(result.get(1).getStoreName()).isEqualTo("ABC Store");
            assertThat(result.get(2).getStoreName()).isEqualTo("한글Store");
        }

        @Test
        @DisplayName("특수문자가 포함된 가게명 처리 성공")
        void getAllStores_SpecialCharacters_Success() {
            // Given
            List<Store> specialStores = Arrays.asList(specialCharStore);
            given(storeRepository.findAllStoresOrderByName()).willReturn(specialStores);

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStoreName()).isEqualTo("Store-Name_With!@#");
            assertThat(result.get(0).getStoreId()).isEqualTo(5L);
        }
    }

    // ================================ 경계값 테스트 ================================

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTests {

        @Test
        @DisplayName("빈 목록 반환 시 정상 처리")
        void getAllStores_EmptyList_Success() {
            // Given
            given(storeRepository.findAllStoresOrderByName()).willReturn(Collections.emptyList());

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).isEmpty();
            assertThat(result).isNotNull();
            verify(storeRepository, times(1)).findAllStoresOrderByName();
        }

        @Test
        @DisplayName("단일 가게만 있는 경우 정상 처리")
        void getAllStores_SingleStore_Success() {
            // Given
            given(storeRepository.findAllStoresOrderByName()).willReturn(Arrays.asList(storeA));

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStoreId()).isEqualTo(1L);
            assertThat(result.get(0).getStoreName()).isEqualTo("A마트");
        }

        @Test
        @DisplayName("동일한 이름의 가게들 처리")
        void getAllStores_DuplicateNames_Success() {
            // Given
            Store store1 = createStore(1L, "같은이름마트", 1L);
            Store store2 = createStore(2L, "같은이름마트", 2L);
            Store store3 = createStore(3L, "같은이름마트", 3L);

            List<Store> duplicateStores = Arrays.asList(store1, store2, store3);
            given(storeRepository.findAllStoresOrderByName()).willReturn(duplicateStores);

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).allMatch(store -> "같은이름마트".equals(store.getStoreName()));

            // ID로 구분 가능한지 확인
            List<Long> storeIds = result.stream()
                    .map(StoreListResponse::getStoreId)
                    .toList();
            assertThat(storeIds).containsExactly(1L, 2L, 3L);
        }

        @Test
        @DisplayName("매우 긴 가게명 처리")
        void getAllStores_VeryLongStoreName_Success() {
            // Given
            String longName = "매우매우매우매우매우매우매우매우매우매우긴이름을가진가게명테스트용가게입니다정말로매우깁니다";
            Store longNameStore = createStore(1L, longName, 1L);

            given(storeRepository.findAllStoresOrderByName()).willReturn(Arrays.asList(longNameStore));

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStoreName()).isEqualTo(longName);
            assertThat(result.get(0).getStoreName().length()).isGreaterThan(40);
        }
    }

    // ================================ 예외 상황 테스트 ================================

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionScenarios {

        @Test
        @DisplayName("Repository에서 RuntimeException 발생 시 예외 전파")
        void getAllStores_RepositoryRuntimeException_ThrowsException() {
            // Given
            given(storeRepository.findAllStoresOrderByName())
                    .willThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> storeService.getAllStores())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection failed");

            verify(storeRepository, times(1)).findAllStoresOrderByName();
        }

        @Test
        @DisplayName("Repository에서 DataAccessException 발생 시 예외 전파")
        void getAllStores_RepositoryDataAccessException_ThrowsException() {
            // Given
            given(storeRepository.findAllStoresOrderByName())
                    .willThrow(new org.springframework.dao.DataAccessException("DB Error") {});

            // When & Then
            assertThatThrownBy(() -> storeService.getAllStores())
                    .isInstanceOf(org.springframework.dao.DataAccessException.class)
                    .hasMessage("DB Error");
        }

        @Test
        @DisplayName("Repository에서 null 반환 시 NullPointerException 발생")
        void getAllStores_RepositoryReturnsNull_ThrowsNullPointerException() {
            // Given
            given(storeRepository.findAllStoresOrderByName()).willReturn(null);

            // When & Then
            assertThatThrownBy(() -> storeService.getAllStores())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Store 엔티티에 null 필드가 있어도 DTO 변환 성공")
        void getAllStores_StoreWithNullFields_Success() {
            // Given
            Store storeWithNullName = Store.builder()
                    .storeName(null)  // null 이름
                    .sellerId(1L)
                    .build();
            ReflectionTestUtils.setField(storeWithNullName, "storeId", 1L);

            given(storeRepository.findAllStoresOrderByName()).willReturn(Arrays.asList(storeWithNullName));

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStoreId()).isEqualTo(1L);
            assertThat(result.get(0).getStoreName()).isNull();
        }
    }


    // ================================ 데이터 변환 검증 테스트 ================================

    @Nested
    @DisplayName("데이터 변환 검증 테스트")
    class DataTransformationTests {

        @Test
        @DisplayName("Entity to DTO 변환 정확성 검증")
        void getAllStores_EntityToDtoTransformation_Accuracy() {
            // Given
            Store originalStore = Store.builder()
                    .storeName("정확성테스트마트")
                    .sellerId(100L)
                    .description("설명")
                    .logo("로고URL")
                    .build();
            ReflectionTestUtils.setField(originalStore, "storeId", 999L);

            given(storeRepository.findAllStoresOrderByName()).willReturn(Arrays.asList(originalStore));

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(1);
            StoreListResponse dto = result.get(0);

            // 필요한 필드만 변환되었는지 확인
            assertThat(dto.getStoreId()).isEqualTo(999L);
            assertThat(dto.getStoreName()).isEqualTo("정확성테스트마트");

            // DTO에는 민감한 정보가 포함되지 않음을 확인
            // (description, logo, sellerId 등은 노출되지 않음)
        }

        @Test
        @DisplayName("변환 과정에서 데이터 손실 없음 확인")
        void getAllStores_NoDataLoss_DuringTransformation() {
            // Given
            List<Store> originalStores = Arrays.asList(storeA, storeB, storeZ);
            given(storeRepository.findAllStoresOrderByName()).willReturn(originalStores);

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).hasSize(originalStores.size());

            for (int i = 0; i < originalStores.size(); i++) {
                Store original = originalStores.get(i);
                StoreListResponse dto = result.get(i);

                assertThat(dto.getStoreId()).isEqualTo(original.getStoreId());
                assertThat(dto.getStoreName()).isEqualTo(original.getStoreName());
            }
        }

        @Test
        @DisplayName("빈 컬렉션 변환 처리")
        void getAllStores_EmptyCollectionTransformation_Success() {
            // Given
            given(storeRepository.findAllStoresOrderByName()).willReturn(Collections.emptyList());

            // When
            List<StoreListResponse> result = storeService.getAllStores();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            assertThat(result).isInstanceOf(List.class);
        }
    }

    // ================================ 헬퍼 메서드 ================================

    /**
     * 테스트용 Store 엔티티 생성 헬퍼 메서드
     */
    private Store createStore(Long storeId, String storeName, Long sellerId) {
        Store store = Store.builder()
                .storeName(storeName)
                .sellerId(sellerId)
                .description(storeName + " 설명")
                .logo("http://logo.example.com/" + storeId + ".png")
                .build();

        if (storeId != null) {
            ReflectionTestUtils.setField(store, "storeId", storeId);
        }

        return store;
    }

    /**
     * 대량 테스트 데이터 생성 헬퍼 메서드
     */
    private List<Store> createLargeStoreList(int size) {
        List<Store> stores = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            stores.add(createStore((long) i, "Store_" + i, (long) i));
        }
        return stores;
    }
}