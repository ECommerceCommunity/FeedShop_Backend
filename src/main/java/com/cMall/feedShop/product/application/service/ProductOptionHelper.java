package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.repository.OrderItemRepository;
import com.cMall.feedShop.product.application.dto.request.ProductOptionCreateRequest;
import com.cMall.feedShop.product.application.dto.request.ProductOptionUpdateRequest;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductOptionHelper {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final OrderItemRepository orderItemRepository;

    public User getCurrentUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ProductException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateSellerRole(User user) {
        if (user.getRole() != UserRole.SELLER) {
            throw new ProductException(ErrorCode.FORBIDDEN);
        }
    }

    public Product getProductOwnership(Long productId, Long currentUserId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

        Store userStore = getUserStore(currentUserId);

        if (!product.getStore().getStoreId().equals(userStore.getStoreId())) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_BELONG_TO_STORE);
        }

        return product;
    }

    public Store getUserStore(Long userId) {
        return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new ProductException(ErrorCode.STORE_NOT_FOUND));
    }

    public void validateDuplicateOption(Product product, ProductOptionCreateRequest request) {
        boolean isDuplicate = productOptionRepository.existsByProduct_ProductIdAndGenderAndSizeAndColor(
                product.getProductId(),
                request.getGender(),
                request.getSize(),
                request.getColor()
        );

        if (isDuplicate) {
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT_OPTION);
        }
    }

    public ProductOption createNewOption(ProductOptionCreateRequest request, Product product) {
        return new ProductOption(
                request.getGender(),
                request.getSize(),
                request.getColor(),
                request.getStock(),
                product
        );
    }

    public ProductOption getProductOption(Long optionId) {
        return productOptionRepository.findByOptionId(optionId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));
    }

    public void validateSellerPermission(User currentUser, ProductOption productOption) {
        Long sellerId = productOption.getProduct().getStore().getSellerId();

        if (!currentUser.getId().equals(sellerId)) {
            throw new ProductException(ErrorCode.FORBIDDEN, "해당 상품 옵션에 대한 권한이 없습니다.");
        }
    }

    public void updateOptionInfo(ProductOption productOption, ProductOptionUpdateRequest request) {
        productOption.updateStock(request.getStock());

        if (request.getGender() != null && !request.getGender().trim().isEmpty()) {
            try {
                Gender gender = Gender.valueOf(request.getGender().toUpperCase());
                productOption.updateGender(gender);
            } catch (IllegalArgumentException e) {
                throw new ProductException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 성별 값입니다: " + request.getGender());
            }
        }

        if (request.getSize() != null && !request.getSize().trim().isEmpty()) {
            try {
                Size size = Size.fromValue(request.getSize().toUpperCase());
                productOption.updateSize(size);
            } catch (IllegalArgumentException e) {
                throw new ProductException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 사이즈 값입니다: " + request.getSize());
            }
        }

        if (request.getColor() != null && !request.getColor().trim().isEmpty()) {
            try {
                Color color = Color.valueOf(request.getColor().toUpperCase());
                productOption.updateColor(color);
            } catch (IllegalArgumentException e) {
                throw new ProductException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 색상 값입니다: " + request.getColor());
            }
        }
    }

    public void validateNotOrderedOption(ProductOption productOption) {
        if (orderItemRepository.existsByProductOption(productOption)) {
            throw new ProductException(ErrorCode.PRODUCT_IN_ORDER);
        }
    }
}