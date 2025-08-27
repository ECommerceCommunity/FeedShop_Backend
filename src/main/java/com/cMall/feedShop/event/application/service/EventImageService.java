package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.common.dto.UploadResult;
import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.common.storage.StorageService;
import com.cMall.feedShop.common.storage.UploadDirectory;
import com.cMall.feedShop.common.validator.ImageValidator;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventImage;
import com.cMall.feedShop.event.domain.repository.EventImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventImageService {

    private final StorageService storageService;
    private final ImageValidator imageValidator;
    private final EventImageRepository eventImageRepository;

    /**
     * 이벤트 이미지 업로드
     * @param event 이벤트 엔티티
     * @param files 업로드할 이미지 파일 리스트
     */
    public void uploadImages(Event event, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        // 이미지 파일 검증
        imageValidator.validateAll(files, getCurrentImageCount(event));

        // 새 이미지 생성 및 GCP에 업로드
        List<EventImage> newImages = createEventImages(event, files);
        event.getImages().addAll(newImages);
    }

    /**
     * 이벤트 이미지 교체
     * @param event 이벤트 엔티티
     * @param files 새로 업로드할 이미지 파일 리스트
     */
    public void replaceImages(Event event, List<MultipartFile> files) {
        // 해당 이벤트의 기존 이미지들 가져오기
        List<EventImage> existingImages = getEventImages(event);

        // 교체 시에는 새 이미지만 검증 (기존 이미지는 삭제될 예정)
        imageValidator.validateFiles(files);

        try {
            // 새 이미지 생성 및 GCP에 업로드
            List<EventImage> newImages = createEventImages(event, files);

            // DB 업데이트
            event.getImages().removeAll(existingImages);
            event.getImages().addAll(newImages);

            // 기존 파일 삭제
            deleteImageFilesSafely(existingImages);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 교체에 실패했습니다");
        }
    }

    /**
     * 새 이미지 생성 및 GCP에 업로드
     */
    private List<EventImage> createEventImages(Event event, List<MultipartFile> files) {
        try {
            List<UploadResult> uploadResults =
                    storageService.uploadFilesWithDetails(files, UploadDirectory.EVENTS);

            return uploadResults.stream()
                    .map(result -> EventImage.builder()
                            .event(event)
                            .originalFilename(result.getOriginalFilename())
                            .storedFilename(result.getStoredFilename())
                            .filePath(result.getFilePath())
                            .fileSize(result.getFileSize())
                            .contentType(result.getContentType())
                            .imageOrder(getCurrentImageCount(event) + 1)
                            .build())
                    .toList();

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 업로드에 실패했습니다");
        }
    }

    /**
     * 이벤트 이미지 조회
     */
    private List<EventImage> getEventImages(Event event) {
        return eventImageRepository.findByEventIdOrderByImageOrderAsc(event.getId());
    }

    /**
     * 현재 이미지 개수 조회
     */
    private int getCurrentImageCount(Event event) {
        return (int) eventImageRepository.countByEventId(event.getId());
    }

    /**
     * 이미지 파일 삭제
     */
    private void deleteImageFilesSafely(List<EventImage> images) {
        images.forEach(image -> {
            try {
                storageService.deleteFile(storageService.getFullFilePath(image.getImageUrl()));
            } catch (Exception e) {
                log.warn("이미지 파일 삭제 실패: {} - {}", image.getImageUrl(), e.getMessage());
            }
        });
    }
}
