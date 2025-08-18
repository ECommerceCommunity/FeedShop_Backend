package com.cMall.feedShop.store.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.store.application.dto.response.StoreDetailResponse;
import com.cMall.feedShop.store.domain.exception.StoreException;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.store.application.dto.response.StoreListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Store store = getUserStore(userId);
        return StoreDetailResponse.from(store);
    }

    private Store getUserStore(Long userId) {
        return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));
    }

    /**
     * 모든 상점 목록을 조회하는 서비스 메서드
     *
     * @return 상점 목록 응답 객체 리스트
     */
    public List<StoreListResponse> getAllStores() {
        List<StoreListResponse> stores = storeRepository.findAllStoresOrderByName().stream()
                .map(StoreListResponse::from)
                .toList();

        return stores;
    }

}
