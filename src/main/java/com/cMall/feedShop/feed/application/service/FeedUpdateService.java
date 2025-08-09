package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.feed.application.dto.request.FeedUpdateRequestDto;
import com.cMall.feedShop.feed.application.dto.response.FeedDetailResponseDto;
import com.cMall.feedShop.feed.application.exception.FeedAccessDeniedException;
import com.cMall.feedShop.feed.application.exception.FeedNotFoundException;
import com.cMall.feedShop.feed.domain.Feed;
import com.cMall.feedShop.feed.domain.repository.FeedRepository;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedUpdateService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final FeedMapper feedMapper;

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

    private String normalizeTag(String raw) {
        if (raw == null) return "";
        String trimmed = raw.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed.trim();
    }
}
