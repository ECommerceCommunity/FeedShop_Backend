package com.cMall.feedShop.review.application.dto.request;

import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.validation.ValidReviewElements;
import com.cMall.feedShop.review.domain.validation.ReviewElements; // ✅ 추가
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 리뷰 수정 요청 DTO
 *
 * 🔍 설명:
 * - 이 클래스는 사용자가 리뷰를 수정할 때 보내는 데이터를 담는 그릇입니다
 * - @ValidReviewElements로 3요소(사이즈, 쿠션, 안정성)가 모두 있는지 검증합니다
 * - 검증 어노테이션들(@NotBlank, @Size 등)이 잘못된 데이터를 미리 막아줍니다
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = ReviewUpdateRequest.ReviewUpdateRequestBuilder.class)
@ValidReviewElements
public class ReviewUpdateRequest implements ReviewElements { // ✅ implements 추가

    @NotBlank(message = "리뷰 제목은 필수입니다.")
    @Size(max = 100, message = "리뷰 제목은 100자를 초과할 수 없습니다.")
    private final String title;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private final Integer rating;

    @NotNull(message = "사이즈 착용감은 필수입니다.")
    private final SizeFit sizeFit;

    @NotNull(message = "쿠션감은 필수입니다.")
    private final Cushion cushion;

    @NotNull(message = "안정성은 필수입니다.")
    private final Stability stability;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 10, max = 1000, message = "리뷰 내용은 10자 이상 1000자 이하여야 합니다.")
    private final String content;

    // 새로 추가할 이미지들
    private final List<MultipartFile> newImages;

    // 삭제할 기존 이미지 ID 목록
    private final List<Long> deleteImageIds;

    // ✅ 인터페이스 메서드들은 Lombok이 자동으로 구현해줌 (getter가 이미 있음)

    @JsonPOJOBuilder(withPrefix = "")
    public static class ReviewUpdateRequestBuilder {
        // Lombok이 자동 생성
    }
}