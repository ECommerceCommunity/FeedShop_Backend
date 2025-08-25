package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.repository.OrderItemRepository;
import com.cMall.feedShop.product.application.dto.request.ProductCreateRequest;
import com.cMall.feedShop.product.application.dto.request.ProductOptionRequest;
import com.cMall.feedShop.product.application.dto.request.ProductUpdateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductCreateResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductOptionRepository productOptionRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageService productImageService;

    // 상품 등록
    @Transactional
    public ProductCreateResponse createProduct(ProductCreateRequest request, List<MultipartFile> mainImages, List<MultipartFile> detailImages, String loginId) {
        log.info("상품 등록 시작 - 상품명: {}, 가격: {}", request.getName(), request.getPrice());
        
        // 1. 현재 사용자 정보 가져오기 및 권한 검증
        User currentUser = getCurrentUser(loginId);

        // 2. 판매자 권한 검증
        validateSellerRole(currentUser);

        // 3. 사용자 스토어 찾기
        Store userStore = getUserStore(currentUser.getId());

        // 4. 카테고리 존재 확인
        Category category = getCategory(request.getCategoryId());

        // 5. 상품명 중복 확인
        // (등록 시에는 아직 DB에 저장되지 않은 상태이므로
        // 현재 DB 같은 스토어에 같은 이름이 있는지만 확인)
        validateProductNameDuplication(userStore, request.getName());

        // 6. 상품 생성
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .store(userStore)
                .category(category)
                .build();

        // 7. 이미지 추가
        addImages(product, mainImages, ImageType.MAIN);
        addImages(product, detailImages, ImageType.DETAIL);

        // 8. 옵션 추가
        addOptions(product, request.getOptions());

        // 9. DB 저장
        Product savedProduct = productRepository.save(product);

        // 10. 응답값 리턴
        ProductCreateResponse response = ProductCreateResponse.of(savedProduct.getProductId());
        log.info("상품 등록 완료 - productId: {}, userId: {}, 상품명: {}", 
                savedProduct.getProductId(), currentUser.getId(), request.getName());
        
        return response;
    }

    // 상품 수정
    @Transactional
    public void updateProduct(Long productId, ProductUpdateRequest request, List<MultipartFile> mainImages, List<MultipartFile> detailImages, String loginId) {
        log.info("상품 수정 시작 - productId: {}", productId);
        
        // 1. 현재 사용자 정보 가져오기 및 권한 검증
        User currentUser = getCurrentUser(loginId);

        // 2. 판매자 권한 검증
        validateSellerRole(currentUser);

        // 3. 상품 조회 (소유권 검증 포함)
        Product product = getProductOwnership(productId, currentUser.getId());

        // 4. 카테고리 존재 확인
        Category category = null;
        if (request.getCategoryId() != null) {
            category = getCategory(request.getCategoryId());
        }

        // 5. 상품명 중복 확인
        // 상품명을 변경했을 경우에만 중복 확인
        // (수정 시에는 DB에 저장된 상품과 비교하는데 자기 상품과는 비교하지 않아야 한다.)
        validateNameChange(product, request.getName(), productId);

        // 6. 상품 필드 업데이트
        updateBasicInfo(product, request, category);

        // 7. 이미지 업데이트

        // 메인 이미지
        if (mainImages != null) {
            productImageService.replaceImages(product, mainImages, ImageType.MAIN);
        }

        // 상세 이미지
        if (detailImages != null) {
            productImageService.replaceImages(product, detailImages, ImageType.DETAIL);
        }

        // 8. 옵션 업데이트
        if (request.getOptions() != null) {
            replaceOptions(product, request.getOptions());
        }

        // 9. DB 저장
        productRepository.save(product);
        
        log.info("상품 수정 완료 - productId: {}, userId: {}", productId, currentUser.getId());
    }

    // 상품 삭제
    @Transactional
    public void deleteProduct(Long productId, String loginId) {
        log.info("상품 삭제 시작 - productId: {}", productId);
        
        // 1. 현재 사용자 정보 가져오기 및 권한 검증
        User currentUser = getCurrentUser(loginId);

        // 2. 판매자 권한 검증
        validateSellerRole(currentUser);

        // 3. 상품 조회 (소유권 검증 포함)
        Product product = getProductOwnership(productId, currentUser.getId());

        // 4. 주문에 포함된 상품인지 확인
        validateProductNotInOrder(product);

        // 5. DB 에서 삭제 (CASCADE DELETE)
        productRepository.delete(product);
        
        log.info("상품 삭제 완료 - productId: {}, userId: {}", productId, currentUser.getId());
    }

    // JWT 에서 현재 사용자 추출
    private User getCurrentUser(String loginId) {
        return userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ProductException(ErrorCode.USER_NOT_FOUND));
    }

    // 스토어 조회
    private Store getUserStore(Long userId) {
        // 내 가게를 찾는다.
        return storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new ProductException(ErrorCode.STORE_NOT_FOUND));
    }

    // 카테고리 조회
    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ProductException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // 상품 조회 및 소유권 검증
    private Product getProductOwnership(Long productId, Long currentUserId) {
        // 상품을 찾는다.
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

        // 상품 소유권 검증
        Store store = product.getStore();
        if (!store.isManagedBy(currentUserId)) {
            throw new ProductException(ErrorCode.STORE_FORBIDDEN);
        }

        return product;
    }

    // 판매자 권한을 검증한다.
    private void validateSellerRole(User user) {
        if (user.getRole() != UserRole.SELLER) {
            throw new ProductException(ErrorCode.FORBIDDEN);
        }
    }

    // 상품명 중복 확인 (상품 수정 시)
    private void validateNameChange(Product product, String newName, Long productId) {
        String currentName = product.getName();
        if (newName != null && !newName.equals(currentName)) {
            // 상품명을 변경했을 경우에만 중복 확인
            validateProductNameDuplicationForUpdate(product.getStore(), newName, productId);
        }
    }

    // 상품명 중복 확인 (상품 등록 시)
    private void validateProductNameDuplication(Store store, String productName) {
        if (productRepository.existsByStoreAndName(store, productName)) {
            log.warn("상품명 중복 - storeId: {}, 상품명: {}", store.getStoreId(), productName);
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }
    }

    // 상품명 중복 확인 (상품 수정 시)
    private void validateProductNameDuplicationForUpdate(Store store, String productName, Long productId) {
        if (productRepository.existsByStoreAndNameAndProductIdNot(store, productName, productId)) {
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT_NAME);
        }
    }

    // 주문에 포함된 상품인지 확인 (추후 리팩토링으로 soft delete로 변경 예정)
    private void validateProductNotInOrder(Product product) {
        // 해당 상품의 모든 옵션들을 가져와서 주문에 포함되었는지 확인
        List<ProductOption> productOptions = product.getProductOptions();

        // 상품 옵션들 중 하나라도 주문에 포함되어 있다면 삭제 불가
        for (ProductOption option : productOptions) {
            if (orderItemRepository.existsByProductOption(option)) {
                log.warn("주문에 포함된 상품이라 삭제 불가 - productId: {}, optionId: {}", 
                        product.getProductId(), option.getOptionId());
                throw new ProductException(ErrorCode.PRODUCT_IN_ORDER);
            }
        }
    }

    // 상품 필드 업데이트
    private void updateBasicInfo(Product product, ProductUpdateRequest request, Category category) {
        // 기본 필드 업데이트
        product.updateInfo(request.getName(), request.getPrice(), request.getDescription());

        // 할인 정보 업데이트
        product.updateDiscount(request.getDiscountType(), request.getDiscountValue());

        // 카테고리 업데이트
        if (category != null) {
            product.updateCategory(category);
        }
    }

    // 이미지 추가
    private void addImages(Product product, List<MultipartFile> files, ImageType type) {
        if (files == null || files.isEmpty()) {
            return;
        }

        productImageService.uploadImages(product, files, type);
    }

    // 옵션 추가
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

    // 옵션 교체
    private void replaceOptions(Product product, List<ProductOptionRequest> requests) {
        List<ProductOption> existingOptions = product.getProductOptions();
        if (!existingOptions.isEmpty()) {
            // 기존 옵션 삭제
            productOptionRepository.deleteAll(existingOptions);
            product.getProductOptions().clear();
        }

        // 새 옵션 추가
        addOptions(product, requests);
    }
}