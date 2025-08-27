package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.ProfileUpdateRequest;
import com.cMall.feedShop.user.application.dto.request.UserWithdrawRequest;
import com.cMall.feedShop.user.application.dto.response.UserProfileResponse;
import com.cMall.feedShop.user.application.service.UserProfileService;
import com.cMall.feedShop.user.application.service.UserService;
import com.cMall.feedShop.user.domain.model.DailyPoints;
import com.cMall.feedShop.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "사용자", description = "사용자 프로필 관리 및 회원 탈퇴 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;
    private final UserService userService;

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다. 이름, 전화번호, 프로필 이미지 등의 정보를 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다"
            )
    })
    @GetMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "프로필 정보를 성공적으로 조회했습니다.")
    public ApiResponse<UserProfileResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("User not authenticated.");
        }
        if (!(userDetails instanceof User)) {
            throw new IllegalStateException("Principal is not a User object.");
        }
        User currentUser = (User) userDetails;
        UserProfileResponse data = userProfileService.getUserProfile(currentUser.getId());
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "특정 사용자 프로필 조회",
            description = "특정 사용자의 프로필 정보를 조회합니다. 본인 또는 관리자만 조회 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한이 없습니다 (본인 또는 관리자만 조회 가능)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없습니다"
            )
    })
    @GetMapping("/{userId}/profile")
    @ApiResponseFormat(message = "사용자 프로필 정보를 성공적으로 조회했습니다.")
    public ApiResponse<UserProfileResponse> getUserProfile(
            @Parameter(description = "조회할 사용자 ID", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = (User) userDetails;
        Long currentUserId = currentUser.getId();

        if (!currentUserId.equals(userId)) {
            boolean isAdmin = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                throw new AccessDeniedException("You do not have permission to view this profile.");
            }
        }

        UserProfileResponse data = userProfileService.getUserProfile(userId);
        return ApiResponse.success(data);
    }

    @Operation(
            summary = "내 프로필 정보 수정",
            description = "현재 로그인한 사용자의 프로필 정보를 수정합니다. 이름, 전화번호 등의 정보를 변경할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력 값 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다"
            )
    })
    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "프로필 정보가 성공적으로 수정되었습니다.")
    public ApiResponse<Void> updateMyProfile(
            @Parameter(description = "프로필 수정 요청 데이터", required = true)
            @RequestBody ProfileUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = (User) userDetails;
        userProfileService.updateUserProfile(currentUser.getId(), request);
        return ApiResponse.success(null);
    }

    @Operation(
            summary = "내 프로필 이미지 업로드",
            description = "현재 로그인한 사용자의 프로필 이미지를 업로드합니다. 기존 이미지는 새로운 이미지로 교체됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 이미지 업로드 성공",
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 이미지 파일"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다"
            )
    })
    @PostMapping("/me/profile/image")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "프로필 이미지가 성공적으로 업로드되었습니다.")
    public ApiResponse<String> updateMyProfileImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestParam("image") MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        User currentUser = (User) userDetails;
        String imageUrl = userProfileService.updateProfileImage(currentUser.getId(), image);
        return ApiResponse.success(imageUrl);
    }

    @Operation(
            summary = "관리자 회원 탈퇴 처리",
            description = "관리자가 이메일을 통해 특정 사용자의 회원 탈퇴를 처리합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 처리 완료"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 권한이 필요합니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없습니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 탈퇴 처리된 계정입니다"
            )
    })
    @DeleteMapping("/admin/by-email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponseFormat(message = "사용자 탈퇴 처리가 완료되었습니다.")
    public ApiResponse<String> adminWithdrawUserByEmail(
            @Parameter(description = "탈퇴할 사용자의 이메일", required = true, example = "user@example.com")
            @PathVariable String email) {
        userService.adminWithdrawUserByEmail(email);
        return ApiResponse.success("사용자 탈퇴 처리 완료 (관리자)");
    }

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 사용자가 본인 계정을 탈퇴합니다. 이메일과 비밀번호 확인이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴 완료"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 형식 오류, 비밀번호 길이 오류)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요하거나 비밀번호가 일치하지 않습니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "다른 사용자의 계정을 탈퇴할 수 없습니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없습니다"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 탈퇴 처리된 계정입니다"
            )
    })
    @DeleteMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "회원 탈퇴가 완료되었습니다.")
    public ApiResponse<String> withdrawUser(
            @Parameter(description = "회원 탈퇴 요청 데이터 (이메일, 비밀번호)", required = true)
            @RequestBody UserWithdrawRequest request) {
        userService.withdrawCurrentUserWithPassword(request.getEmail(), request.getPassword());
        return ApiResponse.success("회원 탈퇴 처리 완료");
    }

    @Operation(
            summary = "일별 활동 점수 통계 조회",
            description = "현재 로그인한 사용자의 일별 활동 점수 통계를 조회합니다. 기본적으로 최근 30일간의 데이터를 제공합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "일별 활동 점수 통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = DailyPoints.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인이 필요합니다"
            )
    })
    @GetMapping("/me/activity/daily-points")
    @PreAuthorize("isAuthenticated()")
    @ApiResponseFormat(message = "일별 활동 점수 통계를 성공적으로 조회했습니다.")
    public ApiResponse<List<DailyPoints>> getDailyPoints(
            @Parameter(description = "조회 시작 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)", required = false, example = "2024-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = (User) userDetails;

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }

        List<DailyPoints> data = userService.getDailyPointsStatisticsForUser(currentUser, startDate);
        return ApiResponse.success(data);
    }

    // 특정 사용자의 일별 활동 점수 통계 조회
    @GetMapping("/me/activity/daily-points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DailyPoints>> getDailyPoints(@AuthenticationPrincipal UserDetails userDetails,
                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate) {

        // 현재 로그인한 사용자 정보 가져오기
        User currentUser = (User) userDetails;

        // startDate가 없으면 기본값 설정 (예: 30일 전)
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }

        // 서비스 레이어의 비즈니스 로직 호출
        List<DailyPoints> dailyStats = userService.getDailyPointsStatisticsForUser(currentUser, startDate);

        // 결과를 HTTP 200 OK와 함께 반환
        return ResponseEntity.ok(dailyStats);
    }
}