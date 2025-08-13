package com.cMall.feedShop.product.application.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PagingUtils {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    /**
     * 유효한 Pageable 객체 생성
     */
    public static Pageable createPageable(int page, int size) {
        int validPage = Math.max(page, DEFAULT_PAGE);
        int validSize = (size < 1 || size > MAX_SIZE) ? DEFAULT_SIZE : size;

        return PageRequest.of(validPage, validSize);
    }

    /**
     * 유효한 Pageable 객체 생성 (page overflow 처리)
     */
    public static Pageable createPageable(int page, int size, long totalElements) {
        int validPage = Math.max(page, DEFAULT_PAGE);
        int validSize = (size < 1 || size > MAX_SIZE) ? DEFAULT_SIZE : size;

        // Page Overflow 검사 및 조정
        if (totalElements > 0) {
            int maxPage = (int) Math.ceil((double) totalElements / validSize) - 1;
            maxPage = Math.max(maxPage, 0); // 최소 0페이지
            validPage = Math.min(validPage, maxPage); // 최대 페이지를 넘지 않도록 조정
        }

        return PageRequest.of(validPage, validSize);
    }

    public static boolean isPageOverflow(Page<?> page, int requestedPage) {
        return requestedPage > 0 && page.isEmpty() && page.getTotalElements() > 0;
    }
}
