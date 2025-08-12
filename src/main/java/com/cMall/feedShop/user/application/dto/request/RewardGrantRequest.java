package com.cMall.feedShop.user.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RewardGrantRequest {

    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "포인트 금액은 필수입니다.")
    @Min(value = 1, message = "포인트는 1 이상이어야 합니다.")
    private Integer points;

    @NotBlank(message = "지급 사유는 필수입니다.")
    private String description;
}
