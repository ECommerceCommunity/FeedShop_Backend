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
    private Integer weight;
    private Integer footSize;
    private String profileImageUrl;

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
                .weight(userProfile != null ? userProfile.getWeight() : null)
                .footSize(userProfile != null ? userProfile.getFootSize() : null)
                .profileImageUrl(userProfile != null ? userProfile.getProfileImageUrl() : null)
                .build();
    }

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}