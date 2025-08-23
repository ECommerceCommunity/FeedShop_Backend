package com.cMall.feedShop.user.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.user.application.dto.request.MfaCompleteRequest;
import com.cMall.feedShop.user.application.dto.request.MfaEnableRequest;
import com.cMall.feedShop.user.application.dto.request.MfaSetupRequest;
import com.cMall.feedShop.user.application.dto.request.MfaVerifyRequest;
import com.cMall.feedShop.user.application.dto.response.MfaSetupResponse;
import com.cMall.feedShop.user.application.dto.response.MfaStatusResponse;
import com.cMall.feedShop.user.application.dto.response.UserLoginResponse;
import com.cMall.feedShop.user.application.service.MfaService;
import com.cMall.feedShop.user.application.service.UserAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/mfa")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MfaController {
    private final MfaService mfaService;
    private final UserAuthService userAuthService;

    @PostMapping("/setup")
    @ApiResponseFormat
    public ApiResponse<MfaSetupResponse> setupMfa(@Valid @RequestBody MfaSetupRequest request) {
        MfaSetupResponse response = mfaService.setupMfa(request.getEmail());
        return ApiResponse.success(response);
    }

    @PostMapping("/verify")
    @ApiResponseFormat
    public ApiResponse<Boolean> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        boolean isValid = mfaService.verifyMfaToken(request.getEmail(), request.getToken());
        return ApiResponse.success(isValid);
    }

    @PostMapping("/enable")
    @ApiResponseFormat
    public ApiResponse<Boolean> enableMfa(@Valid @RequestBody MfaEnableRequest request) {
        boolean enabled = mfaService.enableMfa(request.getEmail(), request.getToken());
        return ApiResponse.success(enabled);
    }

    @DeleteMapping("/disable/{email}")
    @ApiResponseFormat
    public ApiResponse<Void> disableMfa(@PathVariable String email) {
        mfaService.disableMfa(email);
        // 데이터가 없는 성공 응답 반환
        return ApiResponse.success(null);
    }

    @GetMapping("/status/{email}")
    @ApiResponseFormat
    public ApiResponse<MfaStatusResponse> getMfaStatus(@PathVariable String email) {
        MfaStatusResponse status = mfaService.getMfaStatus(email);
        return ApiResponse.success(status);
    }

    @PostMapping("/complete")
    @ApiResponseFormat
    public ApiResponse<UserLoginResponse> completeMfaLogin(@Valid @RequestBody MfaCompleteRequest request) throws BadRequestException {
        // MFA 토큰 검증
        boolean isValid = mfaService.verifyMfaToken(request.getEmail(), request.getToken());

        if (isValid) {
            // 최종 로그인 완료 - 실제 로그인 토큰 발급
            UserLoginResponse finalLoginResponse = userAuthService.completeMfaLogin(request.getEmail());
            return ApiResponse.success(finalLoginResponse);
        } else {
            throw new BadRequestException("잘못된 인증 코드입니다.");
        }
    }
}
