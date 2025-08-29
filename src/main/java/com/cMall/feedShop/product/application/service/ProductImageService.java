package com.cMall.feedShop.product.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.StorageService;
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

    private final StorageService storageService;
    private final ImageValidator imageValidator;

    /**
     * 이미지 파일만 업로드 (상품과 연관 없음)
     * @param files 업로드할 이미지 파일 리스트
     * @param type 이미지 타입 (MAIN, DETAIL)
     * @return 업로드된 이미지 파일 경로 리스트
     */
    public List<String> uploadImagesOnly(List<MultipartFile> files, ImageType type) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        // 이미지 파일 검증
        imageValidator.validateFiles(files);

        try {
            // 새 이미지 생성 및 GCP 에 업로드
            List<UploadResult> uploadResults = storageService.uploadFilesWithDetails(files, UploadDirectory.PRODUCTS);

            return uploadResults.stream()
                    .map(UploadResult::getFilePath)
                    .toList();

        } catch (Exception e) {
            log.warn("이미지 업로드 실패: {}", e.getMessage());
            throw new ProductException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 업로드에 실패했습니다");
        }
    }

    /**
     * 이미지 파일만 교체 (상품과 연관 없음)
     * @param imageUrls 기존 이미지 URL 리스트
     * @param type 이미지 타입 (MAIN, DETAIL)
     * @return 교체된 이미지 파일 경로 리스트
     */
    public void replaceImageRecords(Product product, List<String> imageUrls, ImageType type) {
        // 기존 이미지 삭제
        List<ProductImage> existingImages = getProductImages(product, type);
        product.getProductImages().removeAll(existingImages);
        deleteImageFilesSafely(existingImages);

        // 새 이미지 DB 기록
        for (String imageUrl : imageUrls) {
            String objectName = storageService.extractObjectName(imageUrl);
            ProductImage newImage = new ProductImage(objectName, type, product);
            product.getProductImages().add(newImage);
        }
    }

    /**
     * 업로드된 이미지 파일 삭제 (상품과 연관 없음)
     * @param imageUrls 삭제할 이미지 파일 경로 리스트
     */
    public void deleteUploadedImages(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                storageService.deleteFile(imageUrl);
                log.debug("이미지 파일 삭제 성공: {}", imageUrl);
            } catch (Exception e) {
                log.warn("이미지 파일 삭제 실패: {} - {}", imageUrl, e.getMessage());
            }
        }
    }

    // 새 이미지 생성 및 GCP 에 업로드
    private List<ProductImage> createProductImages(Product product, List<MultipartFile> files, ImageType type) {
        try {
            List<UploadResult> uploadResults =
                    storageService.uploadFilesWithDetails(files, UploadDirectory.PRODUCTS);

            return uploadResults.stream()
                    .map(result -> new ProductImage(
                            storageService.extractObjectName(result.getFilePath()),
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
                storageService.deleteFile(storageService.getFullFilePath(image.getUrl()));
            } catch (Exception e) {
                log.warn("이미지 파일 삭제 실패: {} - {}", image.getUrl(), e.getMessage());
            }
        });
    }
}
