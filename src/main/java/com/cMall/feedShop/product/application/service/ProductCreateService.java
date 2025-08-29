package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductCreateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductCreateResponse;
import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.store.domain.model.Store;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCreateService {

    private final ProductRepository productRepository;
    private final ProductHelper productHelper;

    public ProductCreateResponse createProduct(ProductCreateRequest request, List<MultipartFile> mainImages, List<MultipartFile> detailImages, String loginId) {
        // 1. 현재 사용자 정보 가져오기 및 권한 검증
        User currentUser = productHelper.getCurrentUser(loginId);

        // 2. 판매자 권한 검증
        productHelper.validateSellerRole(currentUser);

        // 3. 사용자 스토어 찾기
        Store userStore = productHelper.getUserStore(currentUser.getId());

        // 4. 카테고리 존재 확인
        Category category = productHelper.getCategory(request.getCategoryId());

        // 5. 상품명 중복 확인
        productHelper.validateProductNameDuplication(userStore, request.getName());

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
        productHelper.addImages(product, mainImages, ImageType.MAIN);
        productHelper.addImages(product, detailImages, ImageType.DETAIL);

        // 8. 옵션 추가
        productHelper.addOptions(product, request.getOptions());

        // 9. DB 저장
        Product savedProduct = productRepository.save(product);

        // 10. 응답값 리턴
        return ProductCreateResponse.of(savedProduct.getProductId());
    }
}