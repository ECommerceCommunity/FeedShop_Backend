package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.user.application.service.UserCouponService;
import com.cMall.feedShop.user.application.dto.response.CouponResponse;
import com.cMall.feedShop.user.domain.enums.UserCouponStatus;
import com.cMall.feedShop.user.application.dto.request.CouponIssueRequest;
import com.cMall.feedShop.user.application.dto.request.CouponUseRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class UserCouponController {

    private final UserCouponService userCouponService;

    /**
     * 사용자의 모든 쿠폰 목록을 조회합니다.
     * GET /api/coupons
     *
     * @param email  사용자 이메일 (일반적으로 JWT 토큰에서 추출)
     * @param status 쿠폰 상태 필터 (ACTIVE, USED, EXPIRED 등)
     * @return 쿠폰 응답 DTO 목록
     */
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getUserCoupons(
            @RequestParam String email,
            @RequestParam(required = false) UserCouponStatus status) {
        List<CouponResponse> coupons = userCouponService.getUserCouponsByEmail(email, status);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 사용자의 쿠폰 목록을 페이지네이션하여 조회합니다.
     * GET /api/coupons/pages
     *
     * @param email    사용자 이메일 (JWT 토큰에서 추출)
     * @param status   쿠폰 상태 필터
     * @param pageable 페이지네이션 정보 (page, size, sort)
     * @return 쿠폰 응답 DTO 페이지
     */
    @GetMapping("/pages")
    public ResponseEntity<Page<CouponResponse>> getUserCouponsWithPage(
            @RequestParam String email,
            @RequestParam(required = false) UserCouponStatus status,
            Pageable pageable) {
        Page<CouponResponse> coupons = userCouponService.getUserCouponsByEmail(email, status, pageable);
        return ResponseEntity.ok(coupons);
    }

    /**
     * 특정 사용자에게 쿠폰을 발급합니다. (관리자 권한 필요)
     * POST /api/coupons/issue
     *
     * @param request 쿠폰 발급 요청 DTO
     * @return 발급된 쿠폰 정보
     */
    @PostMapping("/issue")
    public ResponseEntity<CouponResponse> issueCoupon(@RequestBody CouponIssueRequest request) {
        CouponResponse newCoupon = userCouponService.issueCoupon(
                request.getEmail(),
                request.getCouponCode(),
                request.getCouponName(),
                request.getDiscountType(),
                request.getDiscountValue(),
                request.isFreeShipping(),
                request.getExpiresAt()
        );
        return ResponseEntity.ok(newCoupon);
    }

    /**
     * 사용자가 특정 쿠폰을 사용합니다.
     * POST /api/coupons/use
     *
     * @param request 쿠폰 사용 요청 DTO
     * @return 사용 처리된 쿠폰 정보
     */
    @PostMapping("/use")
    public ResponseEntity<CouponResponse> useCoupon(@RequestBody CouponUseRequest request) {
        CouponResponse usedCoupon = userCouponService.useCoupon(request.getEmail(), request.getCouponCode());
        return ResponseEntity.ok(usedCoupon);
    }
}
