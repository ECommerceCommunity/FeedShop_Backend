package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserSocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 소셜 로그인 제공자 정보 리포지토리
 */
public interface UserSocialProviderRepository extends JpaRepository<UserSocialProvider, Long> {

    /**
     * 제공자와 제공자 사용자 ID로 소셜 로그인 정보 조회
     */
    Optional<UserSocialProvider> findByProviderAndProviderSocialUserId(String provider, String providerSocialUserId);

    /**
     * 사용자의 모든 소셜 로그인 연동 정보 조회
     */
    List<UserSocialProvider> findByUser(User user);

    /**
     * 사용자 ID로 모든 소셜 로그인 연동 정보 조회
     */
    List<UserSocialProvider> findByUserId(Long userId);

    /**
     * 특정 사용자의 특정 제공자 연동 정보 조회
     */
    Optional<UserSocialProvider> findByUserAndProvider(User user, String provider);

    /**
     * 제공자와 제공자 사용자 ID 존재 여부 확인
     */
    boolean existsByProviderAndProviderSocialUserId(String provider, String providerSocialUserId);

    /**
     * 소셜 이메일로 조회
     */
    Optional<UserSocialProvider> findBySocialEmail(String socialEmail);

    /**
     * 사용자의 특정 제공자 연동 여부 확인
     */
    boolean existsByUserAndProvider(User user, String provider);
}