package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.request.FeedUpdateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.entity.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.feed.application.service.FeedImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedUpdateService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FeedMapper feedMapper;
    private final FeedImageService feedImageService;

    /**
     * 본인 피드 수정(제목/내용/인스타그램/해시태그)
     */
    @Transactional
    public FeedDetailResponseDto updateFeed(Long feedId, FeedUpdateRequestDto requestDto, UserDetails userDetails) {
        log.info("피드 수정 요청 - feedId: {}", feedId);

        String loginId = userDetails != null ? userDetails.getUsername() : null;
        if (loginId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        User requester = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 상세 조회로 연관 로딩(해시태그 등)
        Feed feed = feedRepository.findDetailById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));

        if (feed.isDeleted()) {
            throw new FeedNotFoundException(feedId);
        }
        if (feed.getUser() == null || !feed.getUser().getId().equals(requester.getId())) {
            throw new FeedAccessDeniedException("본인의 피드만 수정할 수 있습니다.");
        }

        // 본문 데이터 수정
        feed.updateContent(requestDto.getTitle(), requestDto.getContent(), requestDto.getInstagramId());

        // 해시태그 업데이트: null이면 유지, 빈 리스트면 모두 제거, 값 있으면 재구성
        if (requestDto.getHashtags() != null) {
            // 정규화 & 중복 제거(입력 순서 유지)
            Set<String> normalized = requestDto.getHashtags().stream()
                    .map(this::normalizeTag)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // 기존 제거
            feed.getHashtags().clear();
            // 재추가
            if (!normalized.isEmpty()) {
                feed.addHashtags(List.copyOf(normalized));
            }
        }

        // 영속 컨텍스트에 의해 자동 반영됨
        return feedMapper.toFeedDetailResponseDto(feed);
    }

    /**
     * 본인 피드 수정(제목/내용/인스타그램/해시태그/이미지)
     */
    @Transactional
    public FeedDetailResponseDto updateFeedWithImages(Long feedId, FeedUpdateRequestDto requestDto, List<MultipartFile> newImages, UserDetails userDetails) {
        log.info("피드 수정 요청 (이미지 포함) - feedId: {}", feedId);

        String loginId = userDetails != null ? userDetails.getUsername() : null;
        if (loginId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        User requester = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 상세 조회로 연관 로딩(해시태그, 이미지 등)
        Feed feed = feedRepository.findDetailById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));

        if (feed.isDeleted()) {
            throw new FeedNotFoundException(feedId);
        }
        if (feed.getUser() == null || !feed.getUser().getId().equals(requester.getId())) {
            throw new FeedAccessDeniedException("본인의 피드만 수정할 수 있습니다.");
        }

        // 본문 데이터 수정
        feed.updateContent(requestDto.getTitle(), requestDto.getContent(), requestDto.getInstagramId());

        // 해시태그 업데이트: null이면 유지, 빈 리스트면 모두 제거, 값 있으면 재구성
        if (requestDto.getHashtags() != null) {
            // 정규화 & 중복 제거(입력 순서 유지)
            Set<String> normalized = requestDto.getHashtags().stream()
                    .map(this::normalizeTag)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // 기존 제거
            feed.getHashtags().clear();
            // 재추가
            if (!normalized.isEmpty()) {
                feed.addHashtags(List.copyOf(normalized));
            }
        }

        // 이미지 처리
        handleImageUpdates(feed, requestDto, newImages);

        // 영속 컨텍스트에 의해 자동 반영됨
        return feedMapper.toFeedDetailResponseDto(feed);
    }

    /**
     * 이미지 업데이트 처리
     */
    private void handleImageUpdates(Feed feed, FeedUpdateRequestDto requestDto, List<MultipartFile> newImages) {
        // 1. 삭제할 이미지 처리
        if (requestDto.getDeleteImageIds() != null && !requestDto.getDeleteImageIds().isEmpty()) {
            deleteSelectedImages(feed, requestDto.getDeleteImageIds());
        }

        // 2. 새 이미지 추가
        if (newImages != null && !newImages.isEmpty()) {
            try {
                feedImageService.uploadImages(feed, newImages);
                log.info("피드 이미지 추가 완료 - feedId: {}, imageCount: {}", feed.getId(), newImages.size());
            } catch (Exception e) {
                log.error("피드 이미지 추가 실패 - feedId: {}", feed.getId(), e);
                // 이미지 추가 실패가 피드 수정에 영향을 주지 않도록 예외를 던지지 않음
            }
        }
    }

    /**
     * 선택된 이미지들 삭제
     */
    private void deleteSelectedImages(Feed feed, List<Long> deleteImageIds) {
        List<com.cMall.feedShop.feed.domain.entity.FeedImage> imagesToDelete = feed.getImages().stream()
                .filter(image -> deleteImageIds.contains(image.getId()))
                .toList();

        if (!imagesToDelete.isEmpty()) {
            // FeedImageService를 통해 안전하게 삭제
            feedImageService.deleteImages(feed, imagesToDelete);
            log.info("피드 이미지 삭제 완료 - feedId: {}, deletedCount: {}", feed.getId(), imagesToDelete.size());
        }
    }

    private String normalizeTag(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed.trim();
    }
}
