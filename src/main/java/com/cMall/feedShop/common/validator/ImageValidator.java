package com.cMall.feedShop.common.validator;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.image")
@Data
public class ImageValidator {

    private long maxFileSize = 10 * 1024 * 1024L; // 10MB
    private int maxImageCount = 10;
    private List<String> allowedContentTypes = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * 이미지 개수 포함 전체 검증
     */
    public void validateAll(List<MultipartFile> files, int currentCount) {
        if (files == null || files.isEmpty()) {
            return;
        }

        validateImageCount(currentCount, files.size());
        validateFiles(files);
    }

    /**
     * 파일 자체만 검증 (개수 검증 제외)
     */
    public void validateFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        if (files.size() > maxImageCount) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("이미지는 최대 %d개까지 업로드 가능합니다", maxImageCount));
        }

        files.forEach(this::validateSingleFile);
    }

    private void validateSingleFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "이미지 파일이 필요합니다");
        }

        if (file.getSize() > maxFileSize) {
            long maxSizeMB = maxFileSize / (1024 * 1024);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("이미지 크기는 %dMB 이하여야 합니다", maxSizeMB));
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "지원하지 않는 이미지 형식입니다");
        }
    }

    private void validateImageCount(int currentCount, int newCount) {
        if (currentCount + newCount > maxImageCount) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    String.format("이미지는 최대 %d개까지 업로드 가능합니다", maxImageCount));
        }
    }
}
