package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.application.dto.response.PurchasedItemListResponse;
import com.cMall.feedShop.order.application.dto.response.info.PurchasedItemInfo;
import com.cMall.feedShop.order.infrastructure.repository.OrderItemQueryRepository;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchasedItemService {

    private final UserRepository userRepository;
    private final OrderItemQueryRepository orderItemQueryRepository;

    public PurchasedItemListResponse getPurchasedItems(String loginId) {
        // 현재 사용자 조회
        User currentUser = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    log.warn("구매 상품 목록 조회 시 사용자를 찾을 수 없음.");
                    return new UserException(ErrorCode.USER_NOT_FOUND);
                });

        // 구매한 상품 목록 조회
        List<PurchasedItemInfo> items = orderItemQueryRepository.findPurchasedItemsByUserId(currentUser.getId());

        return PurchasedItemListResponse.from(items);
    }
}
