package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.user.application.dto.response.*;
import com.cMall.feedShop.user.application.service.PointService;
import com.cMall.feedShop.user.domain.enums.PointTransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "포인트 관리", description = "포인트 잔액 조회, 거래 내역 조회 API")
@RestController
@RequestMapping("/api/users/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 잔액 조회", description = "현재 사용자의 포인트 잔액과 통계 정보를 조회합니다.")
    @GetMapping("/balance")
    public ResponseEntity<PointBalanceResponse> getPointBalance(
            @AuthenticationPrincipal UserDetails userDetails) {
        PointBalanceResponse response = pointService.getPointBalance(userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "포인트 거래 내역 조회", description = "현재 사용자의 포인트 거래 내역을 페이징으로 조회합니다.")
    @GetMapping("/transactions")
    public ResponseEntity<PointTransactionPageResponse> getPointTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        PointTransactionPageResponse response = pointService.getPointTransactions(userDetails, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 타입 포인트 거래 내역 조회", description = "현재 사용자의 특정 타입 포인트 거래 내역을 조회합니다.")
    @GetMapping("/transactions/type/{type}")
    public ResponseEntity<PointTransactionPageResponse> getPointTransactionsByType(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "거래 타입", example = "EARN")
            @PathVariable PointTransactionType type,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        PointTransactionPageResponse response = pointService.getPointTransactionsByType(userDetails, type, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기간별 포인트 거래 내역 조회", description = "현재 사용자의 특정 기간 포인트 거래 내역을 조회합니다.")
    @GetMapping("/transactions/period")
    public ResponseEntity<PointTransactionPageResponse> getPointTransactionsByPeriod(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "시작 날짜", example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜", example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        PointTransactionPageResponse response = pointService.getPointTransactionsByPeriod(userDetails, startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "만료 예정 포인트 조회", description = "현재 사용자의 30일 이내 만료 예정 포인트를 조회합니다.")
    @GetMapping("/expiring")
    public ResponseEntity<ExpiringPointResponse> getExpiringPoints(
            @AuthenticationPrincipal UserDetails userDetails) {
        ExpiringPointResponse response = pointService.getExpiringPoints(userDetails);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주문별 포인트 거래 내역 조회", description = "현재 사용자의 특정 주문 관련 포인트 거래 내역을 조회합니다.")
    @GetMapping("/transactions/order/{orderId}")
    public ResponseEntity<List<PointTransactionResponse>> getPointTransactionsByOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "주문 ID", example = "1")
            @PathVariable Long orderId) {
        List<PointTransactionResponse> response = pointService.getPointTransactionsByOrder(userDetails, orderId);
        return ResponseEntity.ok(response);
    }
}

