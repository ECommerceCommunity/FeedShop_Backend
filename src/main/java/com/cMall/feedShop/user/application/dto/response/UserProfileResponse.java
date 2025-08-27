package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.Gender;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@Schema(description = "사용자 프로필 응답 DTO")
public class UserProfileResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    @Schema(description = "로그인 ID", example = "user123")
    private String loginId;
    
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;
    
    @Schema(description = "실명", example = "홍길동")
    private String name;
    
    @Schema(description = "닉네임", example = "길동이")
    private String nickname;
    
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;
    
    @Schema(description = "성별", example = "MALE")
    private Gender gender;
    
    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthDate;
    
    @Schema(description = "키 (cm)", example = "175")
    private Integer height;
    
    @Schema(description = "몸무게 (kg)", example = "70")
    private Integer weight;
    
    @Schema(description = "발 사이즈 (mm)", example = "260")
    private Integer footSize;
    
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    public static UserProfileResponse from(User user, UserProfile userProfile) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .name(userProfile != null ? userProfile.getName() : null)
                .nickname(userProfile != null ? userProfile.getNickname() : null)
                .phone(userProfile != null ? userProfile.getPhone() : null)
                .gender(userProfile != null ? userProfile.getGender() : null)
                .birthDate(userProfile != null ? userProfile.getBirthDate() : null)
                .height(userProfile != null ? userProfile.getHeight() : null)
                .weight(userProfile != null ? userProfile.getWeight() : null)
                .footSize(userProfile != null ? userProfile.getFootSize() : null)
                .profileImageUrl(userProfile != null ? userProfile.getProfileImageUrl() : null)
                .build();
    }

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .loginId(user.getLoginId())
                .email(user.getEmail())
                .build();
    }
}