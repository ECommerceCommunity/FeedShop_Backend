package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.GcpStorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.common.validator.ImageValidator;
import com.cMall.feedShop.product.domain.enums.ImageType;
import com.cMall.feedShop.product.domain.exception.ProductException;
import com.cMall.feedShop.product.domain.model.Product;
import com.cMall.feedShop.product.domain.model.ProductImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final GcpStorageService gcpStorageService;
    private final ImageValidator imageValidator;

    /**
     * 상품 이미지 업로드
     * @param product 상품 엔티티
     * @param files 업로드할 이미지 파일 리스트
     * @param type 이미지 타입 (MAIN, DETAIL)
     * @return
     */
    public void uploadImages(Product product, List<MultipartFile> files, ImageType type) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 이미지 파일 검증
        imageValidator.validateAll(files, getProductImages(product, type).size());

        // 새 이미지 생성 및 GCP 에 업로드
        List<ProductImage> newImages = createProductImages(product, files, type);
        product.getProductImages().addAll(newImages);
    }

    /**
     * 상품 이미지 교체
     * @param product 상품 엔티티
     * @param files 새로 업로드할 이미지 파일 리스트
     * @param type 이미지 타입 (MAIN, DETAIL)
     */
    public void replaceImages(Product product, List<MultipartFile> files, ImageType type) {
        // 해당 타입 이미지들 가져오기
        List<ProductImage> existingImages = getProductImages(product, type);

        // 교체 시에는 새 이미지만 검증 (기존 이미지는 삭제될 예정)
        imageValidator.validateFiles(files);

        try {
            // 새 이미지 생성 및 GCP 에 업로드
            List<ProductImage> newImages = createProductImages(product, files, type);

            // DB 업데이트
            product.getProductImages().removeAll(existingImages);
            product.getProductImages().addAll(newImages);

            // 기존 파일 삭제
            deleteImageFilesSafely(existingImages);

        } catch (Exception e) {
            throw new ProductException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 교체에 실패했습니다");
        }
    }

    // 새 이미지 생성 및 GCP 에 업로드
    private List<ProductImage> createProductImages(Product product, List<MultipartFile> files, ImageType type) {
        try {
            List<UploadResult> uploadResults =
                    gcpStorageService.uploadFilesWithDetails(files, UploadDirectory.PRODUCTS);

            return uploadResults.stream()
                    .map(result -> new ProductImage(
                            gcpStorageService.extractObjectName(result.getFilePath()),
                            type,
                            product
                    ))
                    .toList();

        } catch (Exception e) {
            throw new ProductException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 업로드에 실패했습니다");
        }
    }

    // 상품 이미지 조회(해당 타입)
    private List<ProductImage> getProductImages(Product product, ImageType type) {
        return product.getProductImages().stream()
                .filter(image -> image.getType() == type)
                .toList();
    }

    // 이미지 파일 삭제
    private void deleteImageFilesSafely(List<ProductImage> images) {
        images.forEach(image -> {
            try {
                gcpStorageService.deleteFile(gcpStorageService.getFullFilePath(image.getUrl()));
            } catch (Exception e) {
                log.warn("이미지 파일 삭제 실패: {} - {}", image.getUrl(), e.getMessage());
            }
        });
    }
}
