package com.cMall.feedShop.common.storage;

import com.cMall.feedShop.common.dto.UploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Profile("dev")
public class MockStorageService implements StorageService {

    @Value("${app.cdn.base-url}")
    private String cdnBaseUrl;

    @Override
    public List<UploadResult> uploadFilesWithDetails(List<MultipartFile> files, UploadDirectory directory) {
        log.info("📢 Mocking GCP Storage: 파일 업로드 로직 실행");

        // 리스트가 비어있거나 null인 경우 빈 리스트를 반환
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<UploadResult> results = new ArrayList<>();
        String directoryPath = directory.getPath();

        for (MultipartFile file : files) {
            try {
                String originalFilename = file.getOriginalFilename();
                String extension = getFileExtension(originalFilename);
                String storedFilename = "mock-" + UUID.randomUUID().toString() + extension;
                String filePath = cdnBaseUrl + "/images/" + directoryPath + "/" + storedFilename;

                UploadResult mockResult = UploadResult.builder()
                        .originalFilename(originalFilename)
                        .storedFilename(storedFilename)
                        .filePath(filePath)
                        .fileSize(file.getSize())
                        .contentType(file.getContentType())
                        .build();

                results.add(mockResult);
                log.info("Mock 업로드 성공: {} -> {}", originalFilename, storedFilename);

            } catch (Exception e) {
                log.error("Mock 파일 처리 실패: {}", file.getOriginalFilename(), e);
                // 실제 환경에서는 예외를 던질 수도 있지만, Mock에서는 로그만 남기고 계속 진행
            }
        }

        return results;
    }

    @Override
    public boolean deleteFile(String filePath) {
        log.info("📢 Mocking GCP Storage: 파일 삭제 로직 실행 - {}", filePath);
        // 실제 삭제 로직 없이 항상 true를 반환
        return true;
    }

    @Override
    public String extractObjectName(String filePath) {
        // GcpStorageService와 동일한 로직 구현
        if (filePath == null || !filePath.contains("/")) {
            return null;
        }
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }

    @Override
    public String getFullFilePath(String objectName) {
        // 개발환경용 경로 생성 로직
        return cdnBaseUrl + "/mock/" + objectName;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}