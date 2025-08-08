package com.cMall.feedShop.review.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.review.infrastructure.config.ReviewImageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewImageUploadService {

    private final ReviewImageProperties imageProperties;

    public ReviewImageUploadInfo uploadImage(MultipartFile file) {
        validateImage(file);

        try {
            String storedFilename = generateStoredFilename(file.getOriginalFilename());
            String datePath = generateDatePath();
            String fullPath = imageProperties.getUploadPath() + "/" + datePath;

            // 디렉토리 생성
            createDirectoryIfNotExists(fullPath);

            // 파일 저장
            Path filePath = Paths.get(fullPath, storedFilename);
            Files.copy(file.getInputStream(), filePath);

            String relativePath = datePath + "/" + storedFilename;

            log.info("이미지 업로드 완료: {}", relativePath);

            return ReviewImageUploadInfo.builder()
                    .originalFilename(file.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .filePath(relativePath)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .build();

        } catch (IOException e) {
            log.error("이미지 업로드 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "이미지 업로드에 실패했습니다.");
        }
    }

    public void deleteImage(String filePath) {
        try {
            Path fullPath = Paths.get(imageProperties.getUploadPath(), filePath);
            Files.deleteIfExists(fullPath);
            log.info("이미지 삭제 완료: {}", filePath);
        } catch (IOException e) {
            log.error("이미지 삭제 실패: {}", filePath, e);
            // 삭제 실패는 로그만 남기고 예외를 던지지 않음
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미지 파일이 필요합니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > imageProperties.getMaxFileSize()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("이미지 크기는 %dMB를 초과할 수 없습니다.",
                            imageProperties.getMaxFileSize() / (1024 * 1024)));
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일명이 없습니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!imageProperties.getAllowedExtensions().contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "지원하지 않는 이미지 형식입니다. 지원 형식: " + imageProperties.getAllowedExtensions());
        }

        // Content-Type 검증
        String contentType = file.getContentType();
        if (contentType == null || !imageProperties.getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "올바른 이미지 파일이 아닙니다.");
        }
    }

    public void validateImageCount(int currentCount, int newCount) {
        int totalCount = currentCount + newCount;
        if (totalCount > imageProperties.getMaxImageCount()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("이미지는 최대 %d개까지만 업로드할 수 있습니다.", imageProperties.getMaxImageCount()));
        }
    }

    private String generateStoredFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + "." + extension;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "파일 확장자가 없습니다.");
        }
        return filename.substring(lastDotIndex + 1);
    }

    private String generateDatePath() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    }

    private void createDirectoryIfNotExists(String path) throws IOException {
        File directory = new File(path);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                throw new IOException("디렉토리 생성에 실패했습니다: " + path);
            }
        }
    }

    // 업로드 정보를 담는 내부 클래스
    public static class ReviewImageUploadInfo {
        private final String originalFilename;
        private final String storedFilename;
        private final String filePath;
        private final Long fileSize;
        private final String contentType;

        @lombok.Builder
        public ReviewImageUploadInfo(String originalFilename, String storedFilename,
                                     String filePath, Long fileSize, String contentType) {
            this.originalFilename = originalFilename;
            this.storedFilename = storedFilename;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.contentType = contentType;
        }

        // Getters
        public String getOriginalFilename() { return originalFilename; }
        public String getStoredFilename() { return storedFilename; }
        public String getFilePath() { return filePath; }
        public Long getFileSize() { return fileSize; }
        public String getContentType() { return contentType; }
    }
}