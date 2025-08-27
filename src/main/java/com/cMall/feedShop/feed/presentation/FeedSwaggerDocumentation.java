package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.common.dto.PaginatedResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.request.FeedUpdateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.dto.response.FeedListResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 피드 도메인 Swagger 문서
 * 
 * 이 클래스는 피드 관련 모든 API의 Swagger 문서를 정의합니다.
 * 실제 컨트롤러에는 영향을 주지 않으며, 문서화 목적으로만 사용됩니다.
 */
@Tag(name = "피드 (Feed)", description = "피드 생성, 조회, 수정, 삭제, 좋아요, 투표 관련 API")
public class FeedSwaggerDocumentation {

    // ==================== 피드 생성 API ====================
    
    @Operation(
            summary = "피드 생성 (이미지 포함)",
            description = "이미지와 함께 새로운 피드를 생성합니다. multipart/form-data 형식으로 요청하며, 이미지는 선택사항입니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 생성 성공",
                    content = @Content(schema = @Schema(implementation = FeedCreateResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류, 이미지 파일 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주문 상품을 찾을 수 없음"
            )
    })
    public ApiResponse<FeedCreateResponseDto> createFeedWithImages(
            @Parameter(description = "피드 생성 요청 데이터 (JSON)", required = true)
            FeedCreateRequestDto requestDto,
            @Parameter(description = "피드 이미지 파일 목록 (최대 10개)")
            List<MultipartFile> images) {
        return null; // 문서화 목적
    }

    @Operation(
            summary = "피드 생성 (텍스트만)",
            description = "이미지 없이 텍스트만으로 새로운 피드를 생성합니다. application/json 형식으로 요청합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 생성 성공",
                    content = @Content(schema = @Schema(implementation = FeedCreateResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "주문 상품을 찾을 수 없음"
            )
    })
    public ApiResponse<FeedCreateResponseDto> createFeed(
            @Parameter(description = "피드 생성 요청 데이터", required = true)
            FeedCreateRequestDto requestDto) {
        return null; // 문서화 목적
    }

    // ==================== 피드 조회 API ====================
    
    @Operation(
            summary = "피드 전체 목록 조회",
            description = "전체 피드 목록을 페이지네이션으로 조회합니다. 피드 타입별 필터링과 정렬이 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 피드 타입"
            )
    })
    public ApiResponse<PaginatedResponse<FeedListResponseDto>> getFeeds(
            @Parameter(description = "피드 타입 (DAILY, EVENT, RANKING)", example = "DAILY")
            String feedType,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            int size,
            @Parameter(description = "정렬 방식 (latest: 최신순, popular: 인기순)", example = "latest")
            String sort) {
        return null; // 문서화 목적
    }

    @Operation(
            summary = "피드 타입별 조회",
            description = "특정 타입의 피드 목록을 페이지네이션으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 타입별 조회 성공",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 피드 타입"
            )
    })
    public ApiResponse<PaginatedResponse<FeedListResponseDto>> getFeedsByType(
            @Parameter(description = "피드 타입 (DAILY, EVENT, RANKING)", required = true, example = "DAILY")
            String feedType,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            int size,
            @Parameter(description = "정렬 방식 (latest: 최신순, popular: 인기순)", example = "latest")
            String sort) {
        return null; // 문서화 목적
    }

    @Operation(
            summary = "사용자별 피드 목록 조회",
            description = "특정 사용자가 작성한 피드 목록을 페이지네이션으로 조회합니다. 피드 타입별 필터링이 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "사용자별 피드 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 피드 타입"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    public ApiResponse<PaginatedResponse<FeedListResponseDto>> getUserFeeds(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            Long userId,
            @Parameter(description = "피드 타입 (DAILY, EVENT, RANKING)", example = "DAILY")
            String feedType,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            int size,
            @Parameter(description = "정렬 방식 (latest: 최신순, popular: 인기순)", example = "latest")
            String sort) {
        return null; // 문서화 목적
    }

    @Operation(
            summary = "피드 상세 조회",
            description = "특정 피드의 상세 정보를 조회합니다. 피드 내용, 작성자 정보, 이미지, 좋아요/투표 상태 등을 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = FeedDetailResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "피드를 찾을 수 없음"
            )
    })
    public ApiResponse<FeedDetailResponseDto> getFeedDetail(
            @Parameter(description = "피드 ID", required = true, example = "1")
            Long feedId) {
        return null; // 문서화 목적
    }

    // ==================== 피드 수정 API ====================
    
    @Operation(
            summary = "피드 수정 (이미지 포함)",
            description = "피드 내용과 이미지를 함께 수정합니다. multipart/form-data 형식으로 요청합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 수정 성공",
                    content = @Content(schema = @Schema(implementation = FeedDetailResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류, 이미지 파일 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (본인이 작성한 피드만 수정 가능)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "피드를 찾을 수 없음"
            )
    })
    public ApiResponse<FeedDetailResponseDto> updateFeedWithImages(
            @Parameter(description = "피드 ID", required = true, example = "1")
            Long feedId,
            @Parameter(description = "피드 수정 요청 데이터 (JSON)", required = true)
            FeedUpdateRequestDto request,
            @Parameter(description = "새로 추가할 이미지 파일 목록")
            List<MultipartFile> newImages) {
        return null; // 문서화 목적
    }

    @Operation(
            summary = "피드 수정 (텍스트만)",
            description = "피드 내용만 수정합니다. application/json 형식으로 요청합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 수정 성공",
                    content = @Content(schema = @Schema(implementation = FeedDetailResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (본인이 작성한 피드만 수정 가능)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "피드를 찾을 수 없음"
            )
    })
    public ApiResponse<FeedDetailResponseDto> updateFeed(
            @Parameter(description = "피드 ID", required = true, example = "1")
            Long feedId,
            @Parameter(description = "피드 수정 요청 데이터", required = true)
            FeedUpdateRequestDto request) {
        return null; // 문서화 목적
    }

    // ==================== 피드 삭제 API ====================
    
    @Operation(
            summary = "피드 삭제",
            description = "피드를 삭제합니다. 본인이 작성한 피드만 삭제할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (본인이 작성한 피드만 삭제 가능)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "피드를 찾을 수 없음"
            )
    })
    public ApiResponse<Void> deleteFeed(
            @Parameter(description = "피드 ID", required = true, example = "1")
            Long feedId) {
        return null; // 문서화 목적
    }

    // ==================== 피드 좋아요 API ====================
    
    @Operation(
            summary = "피드 좋아요/좋아요 취소",
            description = "피드에 좋아요를 추가하거나 취소합니다. 이미 좋아요를 누른 상태라면 취소됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "좋아요 처리 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "피드를 찾을 수 없음"
            )
    })
    public ApiResponse<Void> toggleFeedLike(
            @Parameter(description = "피드 ID", required = true, example = "1")
            Long feedId) {
        return null; // 문서화 목적
    }

    // ==================== 피드 투표 API ====================
    
    @Operation(
            summary = "피드 투표",
            description = "이벤트 피드에 투표합니다. 이벤트 진행 중인 피드에만 투표할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "투표 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 투표한 피드, 이벤트 종료 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "피드를 찾을 수 없음"
            )
    })
    public ApiResponse<Void> voteFeed(
            @Parameter(description = "피드 ID", required = true, example = "1")
            Long feedId) {
        return null; // 문서화 목적
    }

    // ==================== 내 피드 조회 API ====================
    
    @Operation(
            summary = "내 피드 목록 조회",
            description = "로그인한 사용자가 작성한 피드 목록을 페이지네이션으로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 피드 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자"
            )
    })
    public ApiResponse<PaginatedResponse<FeedListResponseDto>> getMyFeeds(
            @Parameter(description = "피드 타입 (DAILY, EVENT, RANKING)", example = "DAILY")
            String feedType,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            int size,
            @Parameter(description = "정렬 방식 (latest: 최신순, popular: 인기순)", example = "latest")
            String sort) {
        return null; // 문서화 목적
    }

    // ==================== 피드 검색 API ====================
    
    @Operation(
            summary = "피드 검색",
            description = "키워드로 피드를 검색합니다. 제목, 내용, 해시태그에서 검색됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "피드 검색 성공",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 검색 키워드"
            )
    })
    public ApiResponse<PaginatedResponse<FeedListResponseDto>> searchFeeds(
            @Parameter(description = "검색 키워드", required = true, example = "맛있는 음식")
            String keyword,
            @Parameter(description = "피드 타입 (DAILY, EVENT, RANKING)", example = "DAILY")
            String feedType,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            int page,
            @Parameter(description = "페이지 크기 (1-100)", example = "20")
            int size) {
        return null; // 문서화 목적
    }
}
