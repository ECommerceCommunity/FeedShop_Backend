package com.cMall.feedShop.order.application.dto.response.info;

import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDetailInfo {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private Long optionId;
    private OptionDetails optionDetails;
    private Long imageId;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal totalPrice;
    private BigDecimal finalPrice;
    private LocalDateTime orderedAt;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionDetails {
        private Gender gender;
        private Size size;
        private Color color;
    }
}
