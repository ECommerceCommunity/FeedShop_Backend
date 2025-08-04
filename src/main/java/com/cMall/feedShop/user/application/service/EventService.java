package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.user.application.dto.response.CouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventApiService {
    // 이벤트 도메인의 CouponRepository를 주입받아 데이터베이스에 접근합니다.
    private final CouponRepository couponRepository;

    public List<Coupon> getCouponsByUserId(Long userId) {
        // userId에 해당하는 쿠폰 목록을 데이터베이스에서 찾아 반환합니다.
        // 실제 구현은 CouponRepository에 메서드를 정의해야 합니다.
        return couponRepository.findByUserId(userId);
    }

    // 다른 이벤트 관련 비즈니스 로직 (쿠폰 발급, 사용 등)
}
