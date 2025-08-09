package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.common.exception.BusinessException;
import com.cMall.feedShop.common.exception.ErrorCode;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedDeleteService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    /**
     * 본인 피드 소프트 삭제
     *
     * @param feedId 삭제할 피드 ID
     * @param userDetails 인증 사용자 정보 (loginId 사용)
     */
    @Transactional
    public void deleteFeed(Long feedId, UserDetails userDetails) {
        log.info("피드 삭제 요청 - feedId: {}", feedId);

        // 인증 사용자 조회
        String loginId = userDetails != null ? userDetails.getUsername() : null;
        if (loginId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
        }
        User requester = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 피드 조회
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new FeedNotFoundException(feedId));

        // 이미 삭제된 경우 비노출 정책(404) 처리
        if (feed.isDeleted()) {
            throw new FeedNotFoundException(feedId);
        }

        // 소유권 확인
        if (feed.getUser() == null || !feed.getUser().getId().equals(requester.getId())) {
            throw new FeedAccessDeniedException("본인의 피드만 삭제할 수 있습니다.");
        }

        // 소프트 삭제
        feed.softDelete();
        log.info("피드 삭제 완료 - feedId: {}", feedId);
    }
}
