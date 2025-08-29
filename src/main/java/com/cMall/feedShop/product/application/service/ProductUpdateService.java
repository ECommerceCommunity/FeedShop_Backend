package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.product.application.dto.request.ProductUpdateRequest;
import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.model.Category;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.repository.ProductRepository;
import com.cMall.feedShop.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductUpdateService {

    private final ProductRepository productRepository;
    private final ProductHelper productHelper;
    private final ProductImageService productImageService;

    public void updateProduct(Long productId, ProductUpdateRequest request, List<MultipartFile> mainImages, List<MultipartFile> detailImages, String loginId) {
        List<String> uploadedImageUrls = new ArrayList<>();

        try {
            // 1. 현재 사용자 정보 가져오기 및 권한 검증
            User currentUser = productHelper.getCurrentUser(loginId);

            // 2. 판매자 권한 검증
            productHelper.validateSellerRole(currentUser);

            // 3. 상품 조회 (소유권 검증 포함)
            Product product = productHelper.getProductOwnership(productId, currentUser.getId());

            // 4. 카테고리 존재 확인
            Category category = null;
            if (request.getCategoryId() != null) {
                category = productHelper.getCategory(request.getCategoryId());
            }

            // 5. 상품명 중복 확인
            productHelper.validateNameChange(product, request.getName(), productId);

            // 6. 이미지 업데이트
            if (mainImages != null) {
                List<String> mainImageUrls = productImageService.uploadImagesOnly(mainImages, ImageType.MAIN);
                uploadedImageUrls.addAll(mainImageUrls);
            }

            if (detailImages != null) {
                List<String> detailImageUrls = productImageService.uploadImagesOnly(detailImages, ImageType.DETAIL);
                uploadedImageUrls.addAll(detailImageUrls);
            }

            // 7. DB 트랜잭션 시작
            updateProductInTransaction(product, request, category, mainImages, detailImages, uploadedImageUrls);
        } catch (Exception e) {
            // 4. 실패 시 업로드된 이미지 삭제 (보상 트랜잭션)
            if (!uploadedImageUrls.isEmpty()) {
                productImageService.deleteUploadedImages(uploadedImageUrls);
                log.info("업로드된 이미지 삭제 완료: count={}", uploadedImageUrls.size());
            }
            throw e;
        }
    }

    @Transactional
    public void updateProductInTransaction(Product product, ProductUpdateRequest request, Category category,
                                            List<MultipartFile> mainImages, List<MultipartFile> detailImages,
                                            List<String> uploadedImageUrls)
    {

        // 기본 정보 업데이트
        productHelper.updateBasicInfo(product, request, category);

        // 이미지 DB 정보 업데이트 (이미 업로드된 파일 기준)
        if (mainImages != null) {
            productImageService.replaceImageRecords(product, uploadedImageUrls.subList(0, mainImages.size()), ImageType.MAIN);
        }

        if (detailImages != null) {
            int mainImageCount = mainImages != null ? mainImages.size() : 0;
            productImageService.replaceImageRecords(product,
                    uploadedImageUrls.subList(mainImageCount, uploadedImageUrls.size()), ImageType.DETAIL);
        }

        // 옵션 업데이트
        if (request.getOptions() != null) {
            productHelper.replaceOptions(product, request.getOptions());
        }

        // DB 저장
        productRepository.save(product);
    }
}