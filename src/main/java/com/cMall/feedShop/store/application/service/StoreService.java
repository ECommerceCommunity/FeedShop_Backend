package com.cMall.feedShop.store.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.store.application.dto.response.StoreDetailResponse;
import com.cMall.feedShop.store.domain.exception.StoreException;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.store.application.dto.response.StoreListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {
    private final StoreRepository storeRepository;

    /**
     * 판매자의 가게 상세 정보를 조회합니다.
     *
     * @param userId 판매자의 user ID
     * @return 판매자의 가게 상세 정보
     */
    public StoreDetailResponse getMyStoreDetail(Long userId) {
        log.debug("내 상점 상세 조회 - userId: {}", userId);
        
        Store store = getUserStore(userId);
        StoreDetailResponse response = StoreDetailResponse.from(store);
        
        log.debug("내 상점 상세 조회 완료 - storeId: {}, userId: {}", store.getStoreId(), userId);
        
        return response;
    }

    private Store getUserStore(Long userId) {
        return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> {
                    log.warn("상점을 찾을 수 없음 - userId: {}", userId);
                    return new StoreException(ErrorCode.STORE_NOT_FOUND);
                });
    }

    /**
     * 모든 상점 목록을 조회하는 서비스 메서드
     *
     * @return 상점 목록 응답 객체 리스트
     */
    public List<StoreListResponse> getAllStores() {
        log.debug("전체 상점 목록 조회 시작");
        
        List<StoreListResponse> stores = storeRepository.findAllStoresOrderByName().stream()
                .map(StoreListResponse::from)
                .toList();

        log.debug("전체 상점 목록 조회 완료 - 상점 수: {}", stores.size());
        
        return stores;
    }

}
