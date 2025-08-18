package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.model.PointTransaction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ExpiringPointResponse {
    private List<PointTransactionResponse> expiringPoints;
    private Integer totalExpiringPoints;
    private LocalDateTime earliestExpiryDate;

    public static ExpiringPointResponse from(List<PointTransaction> expiringPoints) {
        List<PointTransactionResponse> responses = expiringPoints.stream()
                .map(PointTransactionResponse::from)
                .toList();

        Integer totalExpiringPoints = expiringPoints.stream()
                .mapToInt(PointTransaction::getPoints)
                .sum();

        LocalDateTime earliestExpiryDate = expiringPoints.stream()
                .map(PointTransaction::getExpiryDate)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        return ExpiringPointResponse.builder()
                .expiringPoints(responses)
                .totalExpiringPoints(totalExpiringPoints)
                .earliestExpiryDate(earliestExpiryDate)
                .build();
    }
}

