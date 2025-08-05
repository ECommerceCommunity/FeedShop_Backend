package com.cMall.feedShop.order.application.dto.request;

import com.cMall.feedShop.order.application.validator.ValidCardPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@ValidCardPayment
public class OrderCreateRequest {
    @NotBlank(message = "배송 주소는 필수입니다.")
    @Schema(description = "배송 주소", example = "서울시 영등포구", required = true)
    private String deliveryAddress;

    @NotBlank(message = "배송 상세 주소는 필수입니다.")
    @Schema(description = "배송 상세 주소", example = "새싹동 123-45", required = true)
    private String deliveryDetailAddress;

    @NotBlank(message = "우편번호는 필수입니다.")
    @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다.")
    @Schema(description = "우편번호", example = "75977", required = true)
    private String postalCode;

    @NotBlank(message = "수령인 이름은 필수입니다.")
    @Schema(description = "수령인 이름", example = "홍길동", required = true)
    private String recipientName;

    @NotBlank(message = "수령인 전화번호는 필수입니다.")
    @Pattern(regexp = "01[0-9]-\\d{4}-\\d{4}", message = "올바른 전화번호 형식이 아닙니다.")
    @Schema(description = "수령인 전화번호", example = "010-1234-5678", required = true)
    private String recipientPhone;

    @Min(value = 0, message = "사용할 포인트는 0 이상이어야 합니다.")
    @Schema(description = "사용할 포인트 (100포인트 단위)", example = "0", defaultValue = "0")
    private Integer usedPoints = 0;

    @Schema(description = "배송 메시지", example = "문 앞에 두고 벨 눌러주세요.")
    private String deliveryMessage;

    @NotNull(message = "배송비는 필수입니다.")
    @DecimalMin(value = "0", message = "배송비는 0원 이상이어야 합니다.")
    @Schema(description = "배송비", example = "3000", required = true)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @NotBlank(message = "결제 방법은 필수입니다.")
    @Schema(description = "결제 방법", example = "카드", allowableValues = {"카드", "무통장입금", "간편결제", "휴대폰결제"})
    private String paymentMethod;

    @Schema(description = "카드 번호 (카드 결제 시 필수, 10~14자리 숫자)", example = "1234567890123")
    private String cardNumber;

    @Schema(description = "카드 유효기간 (카드 결제 시 필수, 4자리 숫자", example = "0123")
    private String cardExpiry = "";

    @Schema(description = "카드 CVC (카드 결제 시 필수, 3자리 숫자", example = "123")
    private String cardCvc = "";
}
