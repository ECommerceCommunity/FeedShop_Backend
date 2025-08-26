package com.cMall.feedShop.user.application.dto.request;
import com.cMall.feedShop.user.domain.enums.FootArchType;
import com.cMall.feedShop.user.domain.enums.FootWidth;
import com.cMall.feedShop.user.domain.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "프로필 수정 요청 DTO")
public class ProfileUpdateRequest {
    @Schema(description = "실명 (2-50자)", example = "홍길동")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    private String name;

    @Schema(description = "닉네임 (2-50자)", example = "길동이")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    private String nickname;

    @Schema(description = "전화번호 (20자 이하)", example = "010-1234-5678")
    @Size(max = 20, message = "전화번호는 20자 이하로 입력해주세요.")
    private String phone;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthDate;

    @Schema(description = "키 (cm)", example = "175")
    private Integer height;

    @Schema(description = "발 사이즈 (mm)", example = "260")
    private Integer footSize;

    @Schema(description = "몸무게 (kg)", example = "70")
    private Integer weight;

    @Schema(description = "발 너비", example = "NORMAL")
    private FootWidth footWidth;

    @Schema(description = "발 아치 타입", example = "NORMAL")
    private FootArchType footArchType;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    @Schema(description = "성별", example = "MALE")
    private Gender gender;
}