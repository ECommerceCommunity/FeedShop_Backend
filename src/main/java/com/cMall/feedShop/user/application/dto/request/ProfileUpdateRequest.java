package com.cMall.feedShop.user.application.dto.request;

import com.cMall.feedShop.user.domain.enums.FootArchType;
import com.cMall.feedShop.user.domain.enums.FootWidth;
import com.cMall.feedShop.user.domain.enums.Gender; // Import the correct Gender enum

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ProfileUpdateRequest {

    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하로 입력해주세요.")
    private String name;

    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
    private String nickname;

    @Size(max = 20, message = "전화번호는 20자 이하로 입력해주세요.")
    private String phone;

    private LocalDate birthDate;

    private Integer height;

    private Integer weight;

    private Integer footSize;

    private FootWidth footWidth;

    private FootArchType footArchType;

    private String profileImageUrl;

    private Gender gender;
}