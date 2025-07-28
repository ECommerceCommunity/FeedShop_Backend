package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email); // email 중복 여부 확인 (signUp 시 사용)

    Optional<User> findByVerificationToken(String verificationToken);

}
