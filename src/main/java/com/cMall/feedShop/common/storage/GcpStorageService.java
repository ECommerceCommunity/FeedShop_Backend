package com.cMall.feedShop.common.storage;

import com.cMall.feedShop.common.dto.UploadResult;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Google Cloud Storage 서비스 (gcloud CLI 인증 사용)
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Profile("prod")
public class GcpStorageService implements  StorageService {

    @Value("${spring.cloud.gcp.project-id:}")
    private String projectId;

    @Value("${spring.cloud.gcp.storage.bucket:}")
    private String bucketName;

    private Storage storage;

    /**
     * gcloud CLI 인증을 사용한 초기화
     */
    @PostConstruct
    public void init() {
        if (projectId == null || projectId.trim().isEmpty()) {
            log.warn("GCP 프로젝트 ID가 설정되지 않았습니다.");
            return;
        }

        if (bucketName == null || bucketName.trim().isEmpty()) {
            log.warn("GCP 버킷명이 설정되지 않았습니다.");
            return;
        }

        try {
            log.info("🚀 gcloud CLI 인증을 사용하여 GCP Storage 초기화 중...");

            // gcloud CLI 인증 사용
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();

            this.storage = StorageOptions.newBuilder()
                    .setProjectId(projectId)
                    .setCredentials(credentials)
                    .build()
                    .getService();

            log.info("🎉 GCP Storage 초기화 완료!");
            log.info("   📁 Project: {}", projectId);
            log.info("   🪣 Bucket: {}", bucketName);
            log.info("   🔐 인증: gcloud CLI");

        } catch (IOException e) {
            log.error("❌ GCP Storage 초기화 실패: {}", e.getMessage());
            log.error("💡 해결책:");
            log.error("   1. gcloud auth application-default login");
            log.error("   2. gcloud config set project {}", projectId);
            throw new RuntimeException("GCP Storage 초기화 실패", e);
        }
    }

    /**
     * 여러 파일을 GCP Storage에 업로드
     */
    public List<UploadResult> uploadFilesWithDetails(List<MultipartFile> files, String directory) {
        if (storage == null) {
            log.error("GCP Storage가 초기화되지 않았습니다.");
            throw new RuntimeException("GCP Storage가 초기화되지 않았습니다.");
        }

        List<UploadResult> results = new ArrayList<>();
        log.info("📤 GCP Storage 업로드 시작: {} 개의 파일", files.size());

        for (MultipartFile file : files) {
            try {
                UploadResult result = uploadSingleFile(file, directory);
                results.add(result);
                log.info("✅ 업로드 성공: {} -> {}", file.getOriginalFilename(), result.getFilePath());
            } catch (Exception e) {
                log.error("❌ 파일 업로드 실패: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
            }
        }

        log.info("🎉 모든 파일 업로드 완료: {} 개", results.size());
        return results;
    }

    /**
     * 단일 파일 업로드
     */
    private UploadResult uploadSingleFile(MultipartFile file, String directory) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + extension;

        // 🔥 경로 수정: images/{directory} 형태로 변경
        String objectName = "images/" + directory + "/" + storedFilename;

        // GCP Storage에 업로드
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        String filePath = String.format("gs://%s/%s", bucketName, objectName);

        return UploadResult.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .filePath(filePath)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    /**
     * 파일 삭제
     */
    public boolean deleteFile(String filePath) {
        if (storage == null) {
            log.error("GCP Storage가 초기화되지 않았습니다.");
            return false;
        }

        try {
            String objectName = extractObjectName(filePath);
            if (objectName == null) {
                log.error("잘못된 파일 경로: {}", filePath);
                return false;
            }

            BlobId blobId = BlobId.of(bucketName, objectName);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("🗑️ 파일 삭제 성공: {}", filePath);
            } else {
                log.warn("⚠️ 파일 삭제 실패: {}", filePath);
            }

            return deleted;

        } catch (Exception e) {
            log.error("❌ 파일 삭제 중 오류: {}", filePath, e);
            return false;
        }
    }

    /**
     * 파일 경로에서 객체명 추출
     * gs://feedshop-dev-bucket/images/reviews/filename.jpg -> images/reviews/filename.jpg
     */
    private String extractObjectName(String filePath) {
        if (filePath == null || !filePath.startsWith("gs://")) {
            return null;
        }

        String prefix = "gs://" + bucketName + "/";
        if (filePath.startsWith(prefix)) {
            return filePath.substring(prefix.length());
        }
        return null;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}