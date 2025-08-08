package com.cMall.feedShop.user.application.dto.response;

import com.cMall.feedShop.user.domain.enums.Gender;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserProfile;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private Gender gender;
    private LocalDate birthDate;
    private Integer height;
    private Integer footSize;
    private String profileImageUrl;

    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드 (권장되는 패턴)
    public static UserProfileResponse from(User user, UserProfile userProfile) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(userProfile != null ? userProfile.getName() : null)
                .nickname(userProfile != null ? userProfile.getNickname() : null)
                .phone(userProfile != null ? userProfile.getPhone() : null)
                .gender(userProfile != null ? userProfile.getGender() : null)
                .birthDate(userProfile != null ? userProfile.getBirthDate() : null)
                .height(userProfile != null ? userProfile.getHeight() : null)
                .footSize(userProfile != null ? userProfile.getFootSize() : null)
                .profileImageUrl(userProfile != null ? userProfile.getProfileImageUrl() : null)
                .build();
    }

    // 또는 User 엔티티만으로도 만들 수 있도록 오버로드
    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}