package com.cMall.feedShop.common.storage;

import lombok.Getter;

@Getter
public enum UploadDirectory {
    REVIEWS("reviews"),
    PROFILES("profiles");

    private final String path;

    UploadDirectory(String path) {
        this.path = path;
    }
}
