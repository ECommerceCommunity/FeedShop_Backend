package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.response.*;
import com.cMall.feedShop.user.domain.enums.PointTransactionType;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.PointTransaction;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserPoint;
import com.cMall.feedShop.user.domain.repository.PointTransactionRepository;
import com.cMall.feedShop.user.domain.repository.UserPointRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final UserRepository userRepository;
    private final UserPointRepository userPointRepository;
    private final PointTransactionRepository pointTransactionRepository;

    /**
     * 사용자의 포인트 잔액 조회
     */
    public PointBalanceResponse getPointBalance(UserDetails userDetails) {
        User user = validateUser(userDetails);
        UserPoint userPoint = getUserPoint(user);
        
        // 통계 정보 조회
        Integer totalEarned = pointTransactionRepository.sumValidEarnedPointsByUser(user, LocalDateTime.now());
        Integer totalUsed = pointTransactionRepository.sumUsedPointsByUser(user);
        Integer totalExpired = pointTransactionRepository.sumExpiredPointsByUser(user);
        
        return PointBalanceResponse.from(userPoint, totalEarned, totalUsed, totalExpired);
    }

    /**
     * 포인트 거래 내역 조회 (페이징)
     */
    public PointTransactionPageResponse getPointTransactions(UserDetails userDetails, int page, int size) {
        User user = validateUser(userDetails);
        
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        Page<PointTransactionResponse> responsePage = transactions.map(PointTransactionResponse::from);
        return PointTransactionPageResponse.from(responsePage);
    }

    /**
     * 특정 타입의 포인트 거래 내역 조회
     */
    public PointTransactionPageResponse getPointTransactionsByType(UserDetails userDetails, PointTransactionType type, int page, int size) {
        User user = validateUser(userDetails);
        
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserAndTransactionTypeOrderByCreatedAtDesc(user, type, pageable);
        
        Page<PointTransactionResponse> responsePage = transactions.map(PointTransactionResponse::from);
        return PointTransactionPageResponse.from(responsePage);
    }

    /**
     * 특정 기간의 포인트 거래 내역 조회
     */
    public PointTransactionPageResponse getPointTransactionsByPeriod(UserDetails userDetails, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        User user = validateUser(userDetails);
        
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<PointTransaction> transactions = pointTransactionRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startDate, endDate, pageable);
        
        Page<PointTransactionResponse> responsePage = transactions.map(PointTransactionResponse::from);
        return PointTransactionPageResponse.from(responsePage);
    }

    /**
     * 만료 예정 포인트 조회 (30일 이내)
     */
    public ExpiringPointResponse getExpiringPoints(UserDetails userDetails) {
        User user = validateUser(userDetails);
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusDays(30);
        
        List<PointTransaction> expiringPoints = pointTransactionRepository.findExpiringPointsByUser(user, now, expiryDate);
        
        return ExpiringPointResponse.from(expiringPoints);
    }

    /**
     * 주문 관련 포인트 거래 내역 조회
     */
    public List<PointTransactionResponse> getPointTransactionsByOrder(UserDetails userDetails, Long orderId) {
        User user = validateUser(userDetails);
        
        List<PointTransaction> transactions = pointTransactionRepository.findByUserAndRelatedOrderIdOrderByCreatedAtDesc(user, orderId);
        
        return transactions.stream()
                .map(PointTransactionResponse::from)
                .toList();
    }

    /**
     * 포인트 적립
     */
    @Transactional
    public PointTransactionResponse earnPoints(User user, Integer points, String description, Long orderId) {
        if (points == null || points <= 0) {
            throw new UserException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        UserPoint userPoint = getUserPoint(user);
        userPoint.earnPoints(points);
        userPointRepository.save(userPoint);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.createEarnTransaction(
                user, points, userPoint.getCurrentPoints(), description, orderId);
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        log.info("포인트 적립 완료: 사용자 ID={}, 적립 포인트={}, 설명={}", user.getId(), points, description);
        
        return PointTransactionResponse.from(savedTransaction);
    }

    /**
     * 포인트 사용
     */
    @Transactional
    public PointTransactionResponse usePoints(User user, Integer points, String description, Long orderId) {
        if (points == null || points <= 0) {
            throw new UserException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        UserPoint userPoint = getUserPoint(user);
        
        // 포인트 사용 가능 여부 확인
        if (!userPoint.canUsePoints(points)) {
            throw new UserException(ErrorCode.OUT_OF_POINT);
        }

        userPoint.usePoints(points);
        userPointRepository.save(userPoint);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.createUseTransaction(
                user, points, userPoint.getCurrentPoints(), description, orderId);
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        log.info("포인트 사용 완료: 사용자 ID={}, 사용 포인트={}, 설명={}", user.getId(), points, description);
        
        return PointTransactionResponse.from(savedTransaction);
    }

    /**
     * 포인트 취소 (주문 취소 시)
     */
    @Transactional
    public PointTransactionResponse cancelPoints(User user, Integer points, String description, Long orderId) {
        if (points == null || points <= 0) {
            throw new UserException(ErrorCode.INVALID_POINT_AMOUNT);
        }

        UserPoint userPoint = getUserPoint(user);
        userPoint.earnPoints(points); // 취소된 포인트를 다시 적립
        userPointRepository.save(userPoint);

        // 거래 내역 생성
        PointTransaction transaction = PointTransaction.createCancelTransaction(
                user, points, userPoint.getCurrentPoints(), description, orderId);
        PointTransaction savedTransaction = pointTransactionRepository.save(transaction);

        log.info("포인트 취소 완료: 사용자 ID={}, 취소 포인트={}, 설명={}", user.getId(), points, description);
        
        return PointTransactionResponse.from(savedTransaction);
    }

    /**
     * 만료된 포인트 처리 (PointScheduler에서 매일 자정에 호출)
     */
    @Transactional
    public void processExpiredPoints() {
        LocalDateTime now = LocalDateTime.now();
        
        // 모든 사용자에 대해 만료된 포인트 처리
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            try {
                List<PointTransaction> expiredTransactions = pointTransactionRepository.findExpiredPointsByUser(user, now);

                if (!expiredTransactions.isEmpty()) {
                    UserPoint userPoint = getUserPoint(user);
                    int totalExpiredPoints = expiredTransactions.stream().mapToInt(PointTransaction::getPoints).sum();

                    userPoint.usePoints(totalExpiredPoints);
                    userPointRepository.save(userPoint);

                    // 만료 거래 내역 생성
                    PointTransaction expireTransaction = PointTransaction.createExpireTransaction(
                            user, totalExpiredPoints, userPoint.getCurrentPoints(), 
                            "포인트 만료 처리");
                    pointTransactionRepository.save(expireTransaction);

                    expiredTransactions.forEach(transaction -> {
                        // PointTransaction 모델에 만료 상태를 표시하는 메소드가 있다고 가정합니다.
                        transaction.markAsExpired();
                    });
                    pointTransactionRepository.saveAll(expiredTransactions);

                    log.info("포인트 만료 처리 완료: 사용자 ID={}, 만료 포인트={}", user.getId(), totalExpiredPoints);
                }
            } catch (Exception e) {
                log.error("포인트 만료 처리 중 오류 발생: 사용자 ID={}", user.getId(), e);
            }
        }
    }

    /**
     * 사용자 검증
     */
    private User validateUser(UserDetails userDetails) {
        return userRepository.findByLoginId(userDetails.getUsername())
                .orElseThrow(() -> {
                    log.warn("포인트 조회 시 사용자를 찾을 수 없음. username: {}", userDetails.getUsername());
                    return new UserException(ErrorCode.USER_NOT_FOUND);
                });
    }

    /**
     * 사용자 포인트 정보 조회 (없으면 생성)
     */
    private UserPoint getUserPoint(User user) {
        Optional<UserPoint> userPointOpt = userPointRepository.findByUser(user);
        
        if (userPointOpt.isPresent()) {
            return userPointOpt.get();
        } else {
            // 포인트 정보가 없으면 새로 생성
            UserPoint newUserPoint = UserPoint.builder()
                    .user(user)
                    .currentPoints(0)
                    .build();
            return userPointRepository.save(newUserPoint);
        }
    }
}
