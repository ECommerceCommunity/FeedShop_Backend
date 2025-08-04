package com.cMall.feedShop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gcp.storage")
@Data
public class GcpStorageConfig {

        private String bucketName;
        private String projectId;
        private String credentialsPath;

        // 또는 환경변수로 관리하려면
        // private String credentialsJson; // JSON 문자열로 받기

}
