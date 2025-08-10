package com.cMall.feedShop.common.storage;

import com.cMall.feedShop.common.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StorageService {
        List<UploadResult> uploadFilesWithDetails(List<MultipartFile> files, UploadDirectory directory);
    boolean deleteFile(String filePath);
}
