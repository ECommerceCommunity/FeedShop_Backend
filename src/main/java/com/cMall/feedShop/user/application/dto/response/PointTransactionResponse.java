package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.PointTransactionType;
import com.cMall.feedShop.user.domain.model.PointTransaction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PointTransactionResponse {
    private Long transactionId;
    private PointTransactionType transactionType;
    private String transactionTypeDescription;
    private Integer points;
    private Integer balanceAfter;
    private String description;
    private Long relatedOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime expiryDate;
    private Long daysUntilExpiry;
    private boolean isExpired;

    public static PointTransactionResponse from(PointTransaction transaction) {
        return PointTransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .transactionType(transaction.getTransactionType())
                .transactionTypeDescription(transaction.getTransactionType().getDescription())
                .points(transaction.getPoints())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .relatedOrderId(transaction.getRelatedOrderId())
                .createdAt(transaction.getCreatedAt())
                .expiryDate(transaction.getExpiryDate())
                .daysUntilExpiry(transaction.getDaysUntilExpiry())
                .isExpired(transaction.isExpired())
                .build();
    }
}

