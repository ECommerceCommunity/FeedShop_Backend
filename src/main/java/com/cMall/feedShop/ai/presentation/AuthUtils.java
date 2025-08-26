package com.cMall.feedShop.ai.presentation;

import com.cMall.feedShop.ai.domain.exception.AIException;
import com.cMall.feedShop.common.exception.ErrorCode;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;

final class AuthUtils {
    public static User extractUser(UserDetails userDetails, UserRepository userRepository) {
        if (userDetails == null) {
            throw new AIException(ErrorCode.USER_NOT_FOUND);
        }

        if (userDetails instanceof User user) {
            return (User) userDetails;
        }

        String userEmail = userDetails.getUsername();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AIException(ErrorCode.USER_NOT_FOUND));
    }

    private AuthUtils() { }
}
