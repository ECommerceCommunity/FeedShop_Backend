package com.cMall.feedShop.feed.application.service;

import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 피드 서비스 공통 유틸리티
 * 여러 서비스에서 중복되는 로직을 공통화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FeedServiceUtils {
    
    private final UserRepository userRepository;
    
    /**
     * UserDetails에서 userId 추출
     * 여러 서비스에서 중복되는 로직을 공통화
     */
    public Long getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            log.warn("UserDetails가 null입니다.");
            return null;
        }
        String loginId = userDetails.getUsername();
        log.debug("UserDetails에서 사용자 정보 추출 완료");
        Optional<User> userOptional = userRepository.findByLoginId(loginId);
        if (userOptional.isEmpty()) {
            log.warn("login_id로 사용자를 찾을 수 없습니다");
            return null;
        }
        User user = userOptional.get();
        log.debug("사용자 ID 추출 완료: {}", user.getId());
        return user.getId();
    }
}
