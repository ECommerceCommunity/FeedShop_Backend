package com.cMall.feedShop.feed.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedUpdateRequestDto {

    /**
     * 제목: 필수, 최대 100자
     */
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
    private String title;

    /**
     * 내용: 선택, 최대 2000자
     */
    @Size(max = 2000, message = "내용은 최대 2000자까지 가능합니다.")
    private String content;

    /**
     * 인스타그램 ID: 선택, 영문/숫자/밑줄/점, 1~30자
     */
    @Pattern(regexp = "^[a-zA-Z0-9._]{0,30}$", message = "인스타그램 ID는 영문/숫자/밑줄/점으로 30자 이내여야 합니다.")
    private String instagramId;
}
