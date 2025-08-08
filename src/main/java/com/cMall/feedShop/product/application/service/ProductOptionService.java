package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.response.info.ProductOptionInfo;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductOptionService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    /**
     * 상품 ID로 상품 옵션 정보를 조회하는 서비스 메서드
     *
     * @param productId 상품 ID
     * @param userDetails 사용자 정보 (판매자)
     * @return 상품 옵션 정보 리스트
     */
    @Transactional(readOnly = true)
    public List<ProductOptionInfo> getProductOptions(Long productId, UserDetails userDetails) {
        // 1. 현재 사용자를 가져오고 권한을 검증한다.
        User currentUser = getCurrentUser(userDetails);

        // 2. 판매자 권한을 검증한다.
        validateSellerRole(currentUser);

        // 3. 상품 소유권을 검증하고 상품 정보를 가져온다.
        Product product = getProductOwnership(productId, currentUser.getId());

        // 4. 상품 옵션 정보를 조회하여 반환한다.
        return ProductOptionInfo.fromList(product.getProductOptions());
    }

    private User getCurrentUser(UserDetails userDetails) {
        // 사용자 정보에서 로그인 ID를 가져온다.
        String loginId = userDetails.getUsername();

        // 로그인 ID로 사용자 정보를 조회한다.
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ProductException(ErrorCode.USER_NOT_FOUND));

        // 사용자가 SELLER 권한을 가지고 있는지 확인
        if (user.getRole() != UserRole.SELLER) {
            throw new ProductException(ErrorCode.FORBIDDEN);
        }

        return user;
    }

    private Product getProductOwnership(Long productId, Long currentUserId) {
        // 상품을 조회한다
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

        // 현재 사용자의 가게를 조회한다
        Store userStore = getUserStore(currentUserId);

        // 상품이 해당 가게에 속하는지 확인한다
        if (!product.getStore().getStoreId().equals(userStore.getStoreId())) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_BELONG_TO_STORE);
        }

        return product;
    }

    // 스토어 조회
    private Store getUserStore(Long userId) {
        // 내 가게를 찾는다.
        return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new ProductException(ErrorCode.STORE_NOT_FOUND));
    }

    private void validateSellerRole(User user) {
        if (user.getRole() != UserRole.SELLER) {
            throw new ProductException(ErrorCode.FORBIDDEN);
        }
    }
}
