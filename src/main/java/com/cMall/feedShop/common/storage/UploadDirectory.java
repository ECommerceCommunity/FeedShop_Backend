package com.cMall.feedShop.common.storage;

import lombok.Getter;

@Getter
public enum UploadDirectory {
    REVIEWS("reviews"),
    PROFILES("profiles"),
    PRODUCTS("products"),
    FEEDS("feeds"),
    EVENTS("events");

    private final String path;

    UploadDirectory(String path) {
        this.path = path;
    }
}
