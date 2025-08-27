package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.common.validator.ImageValidator;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.entity.FeedImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedImageService {

    private final StorageService storageService;
    private final ImageValidator imageValidator;

    /**
     * 피드 이미지 업로드
     * @param feed 피드 엔티티
     * @param files 업로드할 이미지 파일 리스트
     */
    public void uploadImages(Feed feed, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 이미지 파일 검증
        imageValidator.validateAll(files, getCurrentImageCount(feed));

        // 새 이미지 생성 및 GCP에 업로드
        List<FeedImage> newImages = createFeedImages(feed, files);
        feed.getImages().addAll(newImages);
    }

    /**
     * 피드 이미지 교체
     * @param feed 피드 엔티티
     * @param files 새로 업로드할 이미지 파일 리스트
     */
    public void replaceImages(Feed feed, List<MultipartFile> files) {
        // 기존 이미지들 가져오기
        List<FeedImage> existingImages = feed.getImages();

        // 교체 시에는 새 이미지만 검증 (기존 이미지는 삭제될 예정)
        imageValidator.validateFiles(files);

        try {
            // 새 이미지 생성 및 GCP에 업로드
            List<FeedImage> newImages = createFeedImages(feed, files);

            // DB 업데이트
            feed.getImages().clear();
            feed.getImages().addAll(newImages);

            // 기존 파일 삭제
            deleteImageFilesSafely(existingImages);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 교체에 실패했습니다");
        }
    }

    /**
     * 새 이미지 생성 및 GCP에 업로드
     */
    private List<FeedImage> createFeedImages(Feed feed, List<MultipartFile> files) {
        try {
            List<UploadResult> uploadResults =
                    storageService.uploadFilesWithDetails(files, UploadDirectory.FEEDS);

            return uploadResults.stream()
                    .map(result -> FeedImage.builder()
                            .feed(feed)
                            .imageUrl(storageService.extractObjectName(result.getFilePath()))
                            .sortOrder(getCurrentImageCount(feed) + 1)
                            .build())
                    .toList();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 업로드에 실패했습니다");
        }
    }

    /**
     * 현재 피드의 이미지 개수 조회
     */
    private int getCurrentImageCount(Feed feed) {
        return feed.getImages().size();
    }

    /**
     * 선택된 이미지들 삭제
     * @param feed 피드 엔티티
     * @param imagesToDelete 삭제할 이미지 리스트
     */
    public void deleteImages(Feed feed, List<FeedImage> imagesToDelete) {
        if (imagesToDelete == null || imagesToDelete.isEmpty()) {
            return;
        }

        // DB에서 이미지 제거
        feed.getImages().removeAll(imagesToDelete);

        // 스토리지에서 파일 삭제
        deleteImageFilesSafely(imagesToDelete);
    }

    /**
     * 이미지 파일 삭제
     */
    private void deleteImageFilesSafely(List<FeedImage> images) {
        images.forEach(image -> {
            try {
                storageService.deleteFile(storageService.getFullFilePath(image.getImageUrl()));
            } catch (Exception e) {
                log.warn("이미지 파일 삭제 실패: {} - {}", image.getImageUrl(), e.getMessage());
            }
        });
    }
}
