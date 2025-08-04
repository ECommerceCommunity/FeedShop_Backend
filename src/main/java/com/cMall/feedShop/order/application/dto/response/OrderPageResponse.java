package com.cMall.feedShop.order.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPageResponse {
    private List<OrderListResponse> content;
    private long totalElement;
    private int totalPage;
    private int size;
    private int number;

    public static OrderPageResponse of(Page<OrderListResponse> page) {
        return OrderPageResponse.builder()
                .content(page.getContent())
                .totalElement(page.getTotalElements())
                .totalPage(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .build();
    }
}
