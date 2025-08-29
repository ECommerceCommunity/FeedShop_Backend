package com.cMall.feedShop.review.application.dto.request;

import com.cMall.feedShop.review.domain.enums.Cushion;
import com.cMall.feedShop.review.domain.enums.SizeFit;
import com.cMall.feedShop.review.domain.enums.Stability;
import com.cMall.feedShop.review.domain.validation.ValidReviewElements;
import com.cMall.feedShop.review.domain.validation.ReviewElements; // ✅ 추가
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonDeserialize(builder = ReviewCreateRequest.ReviewCreateRequestBuilder.class)
@ValidReviewElements
@Schema(description = "리뷰 작성 요청")
public class ReviewCreateRequest implements ReviewElements { // ✅ implements 추가

    @Schema(description = "리뷰 제목", example = "정말 편한 운동화에요!", maxLength = 100)
    @NotBlank(message = "리뷰 제목은 필수입니다.")
    @Size(max = 100, message = "리뷰 제목은 100자를 초과할 수 없습니다.")
    private final String title;

    @Schema(description = "평점 (1-5점)", example = "5", minimum = "1", maximum = "5")
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private final Integer rating;

    @Schema(description = "사이즈 착용감 (SMALL: 작음, NORMAL: 적당함, BIG: 큼)", example = "NORMAL")
    @NotNull(message = "사이즈 착용감은 필수입니다.")
    private final SizeFit sizeFit;

    @Schema(description = "쿠션감 (HARD: 딱딱함, MEDIUM: 보통, SOFT: 부드러움)", example = "MEDIUM")
    @NotNull(message = "쿠션감은 필수입니다.")
    private final Cushion cushion;

    @Schema(description = "안정성 (UNSTABLE: 불안정, NORMAL: 보통, STABLE: 안정적)", example = "STABLE")
    @NotNull(message = "안정성은 필수입니다.")
    private final Stability stability;

    @Schema(description = "리뷰 내용 (상세한 후기)", example = "3개월 동안 착용해봤는데 정말 편하고 내구성도 좋아요. 디자인도 깔끔하고 어떤 옷에나 잘 어울립니다.", minLength = 10, maxLength = 1000)
    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(min = 10, max = 1000, message = "리뷰 내용은 10자 이상 1000자 이하여야 합니다.")
    private final String content;

    @Schema(description = "리뷰를 작성할 상품 ID", example = "1")
    @NotNull(message = "상품 ID는 필수입니다.")
    private final Long productId;

    @Schema(description = "리뷰 이미지 파일들 (선택사항, 최대 5개)", type = "array", format = "binary")
    // 이미지는 별도 처리 (MultipartFile은 불변 객체가 아니므로)
    private final List<MultipartFile> images;

    // ✅ 인터페이스 메서드들은 Lombok이 자동으로 구현해줌 (getter가 이미 있음)

    @JsonPOJOBuilder(withPrefix = "")
    public static class ReviewCreateRequestBuilder {
        // Lombok이 자동 생성
    }
}