package com.cMall.feedShop.review.domain.validation;

import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;

/**
 * 리뷰 3요소를 가지는 객체의 공통 인터페이스
 * - 검증 로직을 공통화하기 위해 사용
 */
public interface ReviewElements {

    /**
     * 사이즈 착용감
     */
    SizeFit getSizeFit();

    /**
     * 쿠션감
     */
    Cushion getCushion();

    /**
     * 안정성
     */
    Stability getStability();
}