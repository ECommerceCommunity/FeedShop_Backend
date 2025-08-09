package com.cMall.feedShop.order.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.order.domain.exception.OrderException;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserPoint;
import com.cMall.feedShop.user.domain.repository.UserPointRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.cMall.feedShop.order.application.constants.OrderConstants.*;

/**
 * 주문 관련 공통 서비스
 * - 사용자 검증, 포인트 사용 및 적립 처리 등을 담당
 */
@Component
@RequiredArgsConstructor
public class OrderCommonService {

    private final UserRepository userRepository;
    private final UserPointRepository userPointRepository;

    /**
     * 주문을 위한 사용자 검증
     * - 주문을 요청한 사용자가 존재하는지 확인
     * - 주문을 요청한 사용자의 권한이 USER인지 확인
     *
     * @param userDetails 주문을 요청한 사용자 정보
     * @return 검증된 사용자 정보
     */
    public User validateUser(UserDetails userDetails) {
        User user = userRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.USER) {
            throw new OrderException(ErrorCode.ORDER_FORBIDDEN);
        }

        return user;
    }

    /**
     * 사용자 포인트 조회
     * - 주문을 요청한 사용자의 포인트 정보를 조회
     *
     * @param user 주문을 요청한 사용자 정보
     * @return 사용자 포인트 정보
     */
    public void validatePointUsage(User user, Integer usedPoints) {
        // 사용할 포인트 검증
        if (usedPoints == null || usedPoints == 0) {
            return;
        }

        // 유효값 및 100 포인트 단위 검증 (100 포인트 단위여야 한다)
        if (usedPoints < 0 || usedPoints % 100 != 0) {
            throw new OrderException(ErrorCode.INVALID_POINT);
        }

        // 사용자 포인트 검증
        UserPoint userPoint = getUserPoint(user);
        if (!userPoint.canUsePoints(usedPoints)) {
            throw new OrderException(ErrorCode.OUT_OF_POINT);
        }
    }

    /**
     * 사용자 포인트 사용 및 적립 처리
     * - 주문 완료 후 사용자 포인트를 사용하고 적립하는 로직
     *
     * @param user 주문을 요청한 사용자 정보
     * @param usedPoints 사용한 포인트
     * @param earnedPoints 적립한 포인트
     */
    public void processUserPoints(User user, Integer usedPoints, Integer earnedPoints) {
        // UserPoint를 조회한다.
        UserPoint userPoint = getUserPoint(user);

        // 포인트 사용
        if (usedPoints != null && usedPoints > 0) {
            userPoint.usePoints(usedPoints);
        }

        // 포인트 적립
        if (earnedPoints != null && earnedPoints > 0) {
            userPoint.earnPoints(earnedPoints);
        }

        // DB에 저장
        userPointRepository.save(userPoint);
    }

    /**
     * 사용자 포인트 조회
     * - 사용자가 존재하지 않을 경우 새로 생성
     *
     * @param user 주문을 요청한 사용자 정보
     * @return 사용자 포인트 정보
     */
    private UserPoint getUserPoint(User user) {
        return userPointRepository.findByUser(user)
                .orElse(UserPoint.builder()
                        .user(user)
                        .currentPoints(0)
                        .build());
    }

    // 사용 가능한 포인트 계산
    public Integer calculateActualUsedPoints(BigDecimal totalAmount, Integer requestedPoints) {
        if (requestedPoints == null || requestedPoints <= 0) {
            return 0;
        }

        BigDecimal maxPointUsage = totalAmount.multiply(POINT_USAGE_RATE)
                .setScale(0, RoundingMode.DOWN);

        BigDecimal requestedPointAmount = BigDecimal.valueOf(requestedPoints);

        return requestedPointAmount.compareTo(maxPointUsage) <= 0
                ? requestedPoints
                : maxPointUsage.intValue();
    }

    // 포인트 차감 후 최종 금액 계산
    public BigDecimal calculateFinalAmount(BigDecimal totalAmount, Integer usedPoints) {
        BigDecimal pointDeduction = BigDecimal.valueOf(usedPoints);
        BigDecimal finalAmount = totalAmount.subtract(pointDeduction);

        return finalAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalAmount;
    }

    // 구매 후 얻을 포인트를 계산한다.
    public Integer calculateEarnedPoints(BigDecimal finalAmount) {
        if (finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        BigDecimal units = finalAmount.divide(POINT_REWARD_THRESHOLD, 0, RoundingMode.DOWN);

        // 10,000원 단위로 50 포인트를 적립한다.
        return units.multiply(POINT_REWARD_AMOUNT).intValue();
    }
}
