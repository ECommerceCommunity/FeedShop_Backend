package com.cMall.feedShop.feed.presentation;

import com.cMall.feedShop.common.aop.ApiResponseFormat;
import com.cMall.feedShop.common.dto.ApiResponse;
import com.cMall.feedShop.feed.application.dto.request.FeedCreateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedCreateResponseDto;
import com.cMall.feedShop.feed.application.service.FeedCreateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedCreateController {

    private final FeedCreateService feedCreateService;

    /**
     * 피드 생성 API (이미지 포함)
     * POST /api/feeds
     * 로그인한 사용자만 피드 생성 가능
     */
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat(message = "피드가 성공적으로 생성되었습니다.")
    public ApiResponse<FeedCreateResponseDto> createFeedWithImages(
            @RequestPart("feedData") @Valid FeedCreateRequestDto requestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = (User) userDetails;
        FeedCreateResponseDto response = feedCreateService.createFeedWithImages(requestDto, images, currentUser.getLoginId());
        return ApiResponse.success(response);
    }

    /**
     * 피드 생성 API (이미지 없음)
     * POST /api/feeds/text-only
     * 로그인한 사용자만 피드 생성 가능
     */
    @PostMapping(value = "/text-only", consumes = {"application/json"})
    @PreAuthorize("hasRole('USER')")
    @ApiResponseFormat(message = "피드가 성공적으로 생성되었습니다.")
    public ApiResponse<FeedCreateResponseDto> createFeed(
            @Valid @RequestBody FeedCreateRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User currentUser = (User) userDetails;
        FeedCreateResponseDto response = feedCreateService.createFeed(requestDto, currentUser.getLoginId());
        return ApiResponse.success(response);
    }
} 