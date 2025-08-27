package com.cMall.feedShop.user.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "이메일 요청 DTO")
public class EmailRequest {
    @Schema(description = "이메일 주소", example = "user@example.com", required = true)
    private String email;
}
