package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.repository.OrderItemRepository;
import com.cMall.feedShop.product.application.dto.request.ProductOptionRequest;
import com.cMall.feedShop.product.application.dto.request.ProductUpdateRequest;
import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductOption;
import com.cMall.feedShop.product.domain.repository.CategoryRepository;
import com.cMall.feedShop.product.domain.repository.ProductOptionRepository;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.store.domain.repository.StoreRepository;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductHelper {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductOptionRepository productOptionRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageService productImageService;

    public User getCurrentUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ProductException(ErrorCode.USER_NOT_FOUND));
    }

    public void validateSellerRole(User user) {
        if (user.getRole() != UserRole.SELLER) {
            throw new ProductException(ErrorCode.FORBIDDEN);
        }
    }

    public Store getUserStore(Long userId) {
        return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new ProductException(ErrorCode.STORE_NOT_FOUND));
    }

    public Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ProductException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    public Product getProductOwnership(Long productId, Long currentUserId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

        Store store = product.getStore();
        if (!store.isManagedBy(currentUserId)) {
            throw new ProductException(ErrorCode.STORE_FORBIDDEN);
        }

        return product;
    }

    public void validateProductNameDuplication(Store store, String productName) {
        if (productRepository.existsByStoreAndName(store, productName)) {
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }
    }

    public void validateNameChange(Product product, String newName, Long productId) {
        String currentName = product.getName();
        if (newName != null && !newName.equals(currentName)) {
            validateProductNameDuplicationForUpdate(product.getStore(), newName, productId);
        }
    }

    public void validateProductNameDuplicationForUpdate(Store store, String productName, Long productId) {
        if (productRepository.existsByStoreAndNameAndProductIdNot(store, productName, productId)) {
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }
    }

    public void validateProductNotInOrder(Product product) {
        List<ProductOption> productOptions = product.getProductOptions();

        for (ProductOption option : productOptions) {
            if (orderItemRepository.existsByProductOption(option)) {
                throw new ProductException(ErrorCode.PRODUCT_IN_ORDER);
            }
        }
    }

    public void updateBasicInfo(Product product, ProductUpdateRequest request, Category category) {
        product.updateInfo(request.getName(), request.getPrice(), request.getDescription());
        product.updateDiscount(request.getDiscountType(), request.getDiscountValue());

        if (category != null) {
            product.updateCategory(category);
        }
    }

    public void addImages(Product product, List<MultipartFile> files, ImageType type) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<String> uploadedUrls = productImageService.uploadImagesOnly(files, type);
        productImageService.replaceImageRecords(product, uploadedUrls, type);
    }

    public void addOptions(Product product, List<ProductOptionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<ProductOption> options = requests.stream()
                .map(request -> new ProductOption(
                        request.getGender(),
                        request.getSize(),
                        request.getColor(),
                        request.getStock(),
                        product
                ))
                .toList();

        product.getProductOptions().addAll(options);
    }

    public void replaceOptions(Product product, List<ProductOptionRequest> requests) {
        List<ProductOption> existingOptions = product.getProductOptions();
        if (!existingOptions.isEmpty()) {
            productOptionRepository.deleteAll(existingOptions);
            product.getProductOptions().clear();
        }

        addOptions(product, requests);
    }
}