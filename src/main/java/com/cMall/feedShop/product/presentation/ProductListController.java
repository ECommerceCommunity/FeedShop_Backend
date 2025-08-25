package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.product.application.dto.request.ProductSearchRequest;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.application.service.ProductReadService;
import com.cMall.feedShop.product.application.validator.PriceValidator;
import com.cMall.feedShop.product.domain.enums.Color;
import com.cMall.feedShop.product.domain.enums.Gender;
import com.cMall.feedShop.product.domain.enums.ProductSortType;
import com.cMall.feedShop.product.domain.enums.Size;
import com.cMall.feedShop.product.domain.exception.ProductException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "상품 목록", description = "상품 목록 조회 및 검색 관련 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductListController {

    private final ProductReadService productReadService;

    @Operation(
            summary = "상품 목록 조회 및 검색",
            description = "다양한 필터와 검색 조건으로 상품 목록을 조회합니다. 키워드 검색, 카테고리별 필터, 가격 범위, 색상/사이즈/성별 필터, 재고 및 할인 상품 필터를 지원합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductPageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (가격 범위, 색상/사이즈/성별 값 오류)"
            )
    })
    @GetMapping
    @ApiResponseFormat(message = "상품 목록을 성공적으로 조회했습니다.")
    public ApiResponse<ProductPageResponse> getProductList(
            @Parameter(description = "상품명/브랜드명 검색 키워드", example = "나이키")
            @RequestParam(value = "q", required = false) String q,
            @Parameter(description = "카테고리 ID", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "최소 가격", example = "10000")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "최대 가격", example = "100000")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "가게 ID", example = "1")
            @RequestParam(required = false) Long storeId,
            @Parameter(description = "색상 필터 (여러개 선택 가능)", example = "[\"RED\", \"BLUE\"]")
            @RequestParam(required = false) List<String> colors,
            @Parameter(description = "사이즈 필터 (여러개 선택 가능)", example = "[\"S\", \"M\", \"L\"]")
            @RequestParam(required = false) List<String> sizes,
            @Parameter(description = "성별 필터 (여러개 선택 가능)", example = "[\"MALE\", \"FEMALE\", \"UNISEX\"]")
            @RequestParam(required = false) List<String> genders,
            @Parameter(description = "재고 있는 상품만 조회", example = "true")
            @RequestParam(required = false) Boolean inStockOnly,
            @Parameter(description = "할인 상품만 조회", example = "true")
            @RequestParam(required = false) Boolean discountedOnly,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(
                    description = "정렬 기준",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"latest", "oldest", "price_asc", "price_desc", "popular", "review"}
                    ),
                    example = "latest"
            )
            @RequestParam(defaultValue = "latest") String sort
    ) {
        // 1. 가격 범위 유효성을 검증한다.
        if (!PriceValidator.isValidPriceRange(minPrice, maxPrice)) {
            throw new ProductException(ErrorCode.INVALID_PRODUCT_FILTER_PRICE_RANGE);
        }

        List<Color> colorEnums = parseColors(colors);
        List<Size> sizeEnums = parseSizes(sizes);
        List<Gender> genderEnums = parseGenders(genders);

        // 2. 요청 객체를 생성한다.
        ProductSearchRequest request = ProductSearchRequest.builder()
                .keyword(q)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .storeId(storeId)
                .colors(colorEnums)
                .sizes(sizeEnums)
                .genders(genderEnums)
                .inStockOnly(inStockOnly)
                .discountedOnly(discountedOnly)
                .build();

        // 3. 정렬 타입 변환
        ProductSortType productSortType = ProductSortType.fromCode(sort);

        // 4. 통합 서비스 호출
        ProductPageResponse data = productReadService.getProductList(request, page, size, productSortType);

        return ApiResponse.success(data);
    }

    private List<Color> parseColors(List<String> colors) {
        if (colors == null || colors.isEmpty()) {
            return Collections.emptyList();
        }

        // 색상 문자열 리스트를 Color enum 리스트로 변환
        return colors.stream()
                .filter(color -> color != null && !color.trim().isEmpty())
                .map(color -> {
                    try {
                        return Color.valueOf(color.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new ProductException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 색상입니다." + color);
                    }
                })
                .collect(Collectors.toList());
    }

    private List<Size> parseSizes(List<String> sizes) {
        if (sizes == null || sizes.isEmpty()) {
            return Collections.emptyList();
        }

        // 사이즈 문자열 리스트를 Size enum 리스트로 변환
        return sizes.stream()
                .filter(size -> size != null && !size.trim().isEmpty())
                .map(size -> {
                    try {
                        return Size.fromValue(size.trim());
                    } catch (IllegalArgumentException e) {
                        throw new ProductException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 사이즈입니다." + size);
                    }
                })
                .collect(Collectors.toList());
    }

    private List<Gender> parseGenders(List<String> genders) {
        if (genders == null || genders.isEmpty()) {
            return Collections.emptyList();
        }

        return genders.stream()
                .filter(gender -> gender != null && !gender.trim().isEmpty())
                .map(gender -> {
                    try {
                        return Gender.valueOf(gender.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new ProductException(ErrorCode.INVALID_INPUT_VALUE, "잘못된 성별입니다." + gender);
                    }
                })
                .collect(Collectors.toList());
    }
}
