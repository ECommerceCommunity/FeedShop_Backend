package com.cMall.feedShop.user.application.service;

import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.application.dto.response.*;
import com.cMall.feedShop.user.domain.enums.PointTransactionType;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.exception.UserException;
import com.cMall.feedShop.user.domain.model.PointTransaction;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserPoint;
import com.cMall.feedShop.user.domain.repository.PointTransactionRepository;
import com.cMall.feedShop.user.domain.repository.UserPointRepository;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 테스트")
class PointServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPointRepository userPointRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private PointService pointService;

    private User testUser;
    private UserPoint testUserPoint;
    private PointTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "test@test.com", "password", "test@test.com", UserRole.USER);
        testUserPoint = UserPoint.builder()
                .user(testUser)
                .currentPoints(1000)
                .build();
        
        testTransaction = PointTransaction.builder()
                .user(testUser)
                .transactionType(PointTransactionType.EARN)
                .points(100)
                .balanceAfter(1100)
                .description("테스트 적립")
                .relatedOrderId(1L)
                .expiryDate(LocalDateTime.now().plusYears(1))
                .build();
    }

    @Test
    @DisplayName("포인트 잔액 조회 성공")
    void getPointBalance_Success() {
        // given
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByLoginId("test@test.com")).thenReturn(Optional.of(testUser));
        when(userPointRepository.findByUser(testUser)).thenReturn(Optional.of(testUserPoint));
        when(pointTransactionRepository.sumValidEarnedPointsByUser(any(), any())).thenReturn(1500);
        when(pointTransactionRepository.sumUsedPointsByUser(testUser)).thenReturn(500);
        when(pointTransactionRepository.sumExpiredPointsByUser(testUser)).thenReturn(0);

        // when
        PointBalanceResponse response = pointService.getPointBalance(userDetails);

        // then
        assertThat(response.getCurrentPoints()).isEqualTo(1000);
        assertThat(response.getTotalEarnedPoints()).isEqualTo(1500);
        assertThat(response.getTotalUsedPoints()).isEqualTo(500);
        assertThat(response.getTotalExpiredPoints()).isEqualTo(0);
    }

    @Test
    @DisplayName("포인트 잔액 조회 실패 - 사용자 없음")
    void getPointBalance_Fail_UserNotFound() {
        // given
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByLoginId("test@test.com")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.getPointBalance(userDetails))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("포인트 거래 내역 조회 성공")
    void getPointTransactions_Success() {
        // given
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByLoginId("test@test.com")).thenReturn(Optional.of(testUser));
        
        Page<PointTransaction> transactionPage = new PageImpl<>(List.of(testTransaction));
        when(pointTransactionRepository.findByUserOrderByCreatedAtDesc(any(), any())).thenReturn(transactionPage);

        // when
        PointTransactionPageResponse response = pointService.getPointTransactions(userDetails, 0, 20);

        // then
        assertThat(response.getTransactions()).hasSize(1);
        assertThat(response.getCurrentPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(1);
    }

    @Test
    @DisplayName("특정 타입 포인트 거래 내역 조회 성공")
    void getPointTransactionsByType_Success() {
        // given
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByLoginId("test@test.com")).thenReturn(Optional.of(testUser));
        
        Page<PointTransaction> transactionPage = new PageImpl<>(List.of(testTransaction));
        when(pointTransactionRepository.findByUserAndTransactionTypeOrderByCreatedAtDesc(any(), any(), any())).thenReturn(transactionPage);

        // when
        PointTransactionPageResponse response = pointService.getPointTransactionsByType(userDetails, PointTransactionType.EARN, 0, 20);

        // then
        assertThat(response.getTransactions()).hasSize(1);
        assertThat(response.getTransactions().get(0).getTransactionType()).isEqualTo(PointTransactionType.EARN);
    }

    @Test
    @DisplayName("만료 예정 포인트 조회 성공")
    void getExpiringPoints_Success() {
        // given
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByLoginId("test@test.com")).thenReturn(Optional.of(testUser));
        
        when(pointTransactionRepository.findExpiringPointsByUser(any(), any(), any())).thenReturn(List.of(testTransaction));

        // when
        ExpiringPointResponse response = pointService.getExpiringPoints(userDetails);

        // then
        assertThat(response.getExpiringPoints()).hasSize(1);
        assertThat(response.getTotalExpiringPoints()).isEqualTo(100);
    }

    @Test
    @DisplayName("주문별 포인트 거래 내역 조회 성공")
    void getPointTransactionsByOrder_Success() {
        // given
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByLoginId("test@test.com")).thenReturn(Optional.of(testUser));
        
        when(pointTransactionRepository.findByUserAndRelatedOrderIdOrderByCreatedAtDesc(testUser, 1L)).thenReturn(List.of(testTransaction));

        // when
        List<PointTransactionResponse> response = pointService.getPointTransactionsByOrder(userDetails, 1L);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getRelatedOrderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("포인트 적립 성공")
    void earnPoints_Success() {
        // given
        when(userPointRepository.findByUser(testUser)).thenReturn(Optional.of(testUserPoint));
        when(userPointRepository.save(any(UserPoint.class))).thenReturn(testUserPoint);
        when(pointTransactionRepository.save(any(PointTransaction.class))).thenReturn(testTransaction);

        // when
        PointTransactionResponse response = pointService.earnPoints(testUser, 100, "테스트 적립", 1L);

        // then
        assertThat(response.getTransactionType()).isEqualTo(PointTransactionType.EARN);
        assertThat(response.getPoints()).isEqualTo(100);
        verify(userPointRepository).save(any(UserPoint.class));
        verify(pointTransactionRepository).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("포인트 적립 실패 - 유효하지 않은 포인트 금액")
    void earnPoints_Fail_InvalidAmount() {
        // when & then
        assertThatThrownBy(() -> pointService.earnPoints(testUser, -100, "테스트 적립", 1L))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT_AMOUNT);

        assertThatThrownBy(() -> pointService.earnPoints(testUser, 0, "테스트 적립", 1L))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT_AMOUNT);
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void usePoints_Success() {
        // given
        when(userPointRepository.findByUser(testUser)).thenReturn(Optional.of(testUserPoint));
        when(userPointRepository.save(any(UserPoint.class))).thenReturn(testUserPoint);
        
        // 실제 USE 타입의 트랜잭션을 반환하도록 설정
        PointTransaction useTransaction = PointTransaction.createUseTransaction(testUser, 100, 900, "테스트 사용", 1L);
        when(pointTransactionRepository.save(any(PointTransaction.class))).thenReturn(useTransaction);

        // when
        PointTransactionResponse response = pointService.usePoints(testUser, 100, "테스트 사용", 1L);

        // then
        assertThat(response.getTransactionType()).isEqualTo(PointTransactionType.USE);
        assertThat(response.getPoints()).isEqualTo(100);
        verify(userPointRepository).save(any(UserPoint.class));
        verify(pointTransactionRepository).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("포인트 사용 실패 - 포인트 부족")
    void usePoints_Fail_InsufficientPoints() {
        // given
        when(userPointRepository.findByUser(testUser)).thenReturn(Optional.of(testUserPoint));

        // when & then
        assertThatThrownBy(() -> pointService.usePoints(testUser, 2000, "테스트 사용", 1L))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.OUT_OF_POINT);
    }

    @Test
    @DisplayName("포인트 사용 실패 - 유효하지 않은 포인트 금액")
    void usePoints_Fail_InvalidAmount() {
        // when & then
        assertThatThrownBy(() -> pointService.usePoints(testUser, -100, "테스트 사용", 1L))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT_AMOUNT);

        assertThatThrownBy(() -> pointService.usePoints(testUser, 0, "테스트 사용", 1L))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT_AMOUNT);
    }

    @Test
    @DisplayName("포인트 취소 성공")
    void cancelPoints_Success() {
        // given
        when(userPointRepository.findByUser(testUser)).thenReturn(Optional.of(testUserPoint));
        when(userPointRepository.save(any(UserPoint.class))).thenReturn(testUserPoint);
        
        // 실제 CANCEL 타입의 트랜잭션을 반환하도록 설정
        PointTransaction cancelTransaction = PointTransaction.createCancelTransaction(testUser, 100, 1100, "테스트 취소", 1L);
        when(pointTransactionRepository.save(any(PointTransaction.class))).thenReturn(cancelTransaction);

        // when
        PointTransactionResponse response = pointService.cancelPoints(testUser, 100, "테스트 취소", 1L);

        // then
        assertThat(response.getTransactionType()).isEqualTo(PointTransactionType.CANCEL);
        assertThat(response.getPoints()).isEqualTo(100);
        verify(userPointRepository).save(any(UserPoint.class));
        verify(pointTransactionRepository).save(any(PointTransaction.class));
    }

    @Test
    @DisplayName("포인트 취소 실패 - 유효하지 않은 포인트 금액")
    void cancelPoints_Fail_InvalidAmount() {
        // when & then
        assertThatThrownBy(() -> pointService.cancelPoints(testUser, -100, "테스트 취소", 1L))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT_AMOUNT);

        assertThatThrownBy(() -> pointService.cancelPoints(testUser, 0, "테스트 취소", 1L))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_POINT_AMOUNT);
    }

    @Test
    @DisplayName("사용자 포인트 정보 없을 때 새로 생성")
    void getUserPoint_CreateNewWhenNotExists() {
        // given
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByLoginId("test@test.com")).thenReturn(Optional.of(testUser));
        when(userPointRepository.findByUser(testUser)).thenReturn(Optional.empty());
        when(userPointRepository.save(any(UserPoint.class))).thenReturn(testUserPoint);
        when(pointTransactionRepository.sumValidEarnedPointsByUser(any(), any())).thenReturn(0);
        when(pointTransactionRepository.sumUsedPointsByUser(testUser)).thenReturn(0);
        when(pointTransactionRepository.sumExpiredPointsByUser(testUser)).thenReturn(0);

        // when
        PointBalanceResponse response = pointService.getPointBalance(userDetails);

        // then
        verify(userPointRepository).save(any(UserPoint.class));
    }

    @Test
    @DisplayName("기간별 포인트 거래 내역 조회 성공")
    void getPointTransactionsByPeriod_Success() {
        // given
        when(userDetails.getUsername()).thenReturn("test@test.com");
        when(userRepository.findByLoginId("test@test.com")).thenReturn(Optional.of(testUser));
        
        Page<PointTransaction> transactionPage = new PageImpl<>(List.of(testTransaction));
        when(pointTransactionRepository.findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(any(), any(), any(), any())).thenReturn(transactionPage);

        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();

        // when
        PointTransactionPageResponse response = pointService.getPointTransactionsByPeriod(userDetails, startDate, endDate, 0, 20);

        // then
        assertThat(response.getTransactions()).hasSize(1);
    }
}
