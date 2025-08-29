package com.cMall.feedShop.product.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.product.application.dto.request.ProductCreateRequest;
import com.cMall.feedShop.product.application.dto.request.ProductUpdateRequest;
import com.cMall.feedShop.product.application.dto.response.ProductCreateResponse;
import com.cMall.feedShop.product.application.dto.response.ProductPageResponse;
import com.cMall.feedShop.product.application.service.ProductReadService;
import com.cMall.feedShop.product.application.service.ProductCreateService;
import com.cMall.feedShop.product.application.service.ProductUpdateService;
import com.cMall.feedShop.product.application.service.ProductDeleteService;
import com.cMall.feedShop.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "상품 (판매자)", description = "판매자가 상품을 등록, 수정, 삭제하는 API")
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class ProductSellerController {
    
    private final ProductReadService productReadService;
    private final ProductCreateService productCreateService;
    private final ProductUpdateService productUpdateService;
    private final ProductDeleteService productDeleteService;

    @Operation(
            summary = "판매자 상품 목록 조회",
            description = "판매자가 자신이 등록한 상품 목록을 페이지네이션으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProductPageResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음"
            )
    })
    @GetMapping("/products")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "상품 목록이 성공적으로 조회되었습니다.")
    public ApiResponse<ProductPageResponse> getProducts(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        ProductPageResponse data = productReadService.getSellerProductList(page, size, currentUser.getUsername());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "상품 등록",
            description = "새로운 상품을 등록합니다. 상품 기본 정보, 옵션, 메인 이미지, 상세 이미지를 포함할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "상품 등록 성공",
                    content = @Content(schema = @Schema(implementation = ProductCreateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류, 이미지 파일 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음"
            )
    })
    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponseFormat(message = "상품이 성공적으로 등록되었습니다.")
    public ApiResponse<ProductCreateResponse> createProduct(
            @Parameter(description = "상품 등록 요청 데이터 (JSON)", required = true)
            @RequestPart("product") @Valid ProductCreateRequest request,
            @Parameter(description = "메인 이미지 파일 목록")
            @RequestPart(value = "mainImages", required = false) List<MultipartFile> mainImages,
            @Parameter(description = "상세 설명 이미지 파일 목록")
            @RequestPart(value = "detailImages", required = false) List<MultipartFile> detailImages,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        ProductCreateResponse data = productCreateService.createProduct(request, mainImages, detailImages, currentUser.getLoginId());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "상품 수정",
            description = "기존 상품의 정보를 수정합니다. 상품 기본 정보, 옵션, 이미지를 변경할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류, 이미지 파일 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음 또는 다른 판매자의 상품에 접근 시도"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품을 찾을 수 없음"
            )
    })
    @PutMapping(value = "/products/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "상품이 성공적으로 수정되었습니다.")
    public ApiResponse<Void> updateProduct(
            @Parameter(description = "수정할 상품 ID", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(description = "상품 수정 요청 데이터 (JSON)", required = true)
            @RequestPart("product") @Valid ProductUpdateRequest request,
            @Parameter(description = "메인 이미지 파일 목록 (기존 이미지 교체)")
            @RequestPart(value = "mainImages", required = false) List<MultipartFile> mainImages,
            @Parameter(description = "상세 설명 이미지 파일 목록 (기존 이미지 교체)")
            @RequestPart(value = "detailImages", required = false) List<MultipartFile> detailImages,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = (User) userDetails;
        productUpdateService.updateProduct(productId, request, mainImages, detailImages, currentUser.getLoginId());
        return ApiResponse.success(null);
    }

    @Operation(
            summary = "상품 삭제",
            description = "기존 상품을 삭제합니다. 상품과 관련된 모든 데이터(이미지, 옵션 등)가 함께 삭제됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상품 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "판매자 권한 없음 또는 다른 판매자의 상품에 접근 시도"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "상품을 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "주문에 연결된 상품으로 삭제 불가"
            )
    })
    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    @ApiResponseFormat(message = "상품이 성공적으로 삭제되었습니다.")
    public ApiResponse<Void> deleteProduct(
            @Parameter(description = "삭제할 상품 ID", required = true, example = "1")
            @PathVariable Long productId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails)
    {
        User currentUser = (User) userDetails;
        productDeleteService.deleteProduct(productId, currentUser.getLoginId());
        return ApiResponse.success(null);
    }
}
