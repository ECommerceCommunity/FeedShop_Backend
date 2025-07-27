// src/main/java/com/cMall/feedShop/user/domain/repository/PasswordResetTokenRepository.java
package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.PasswordResetToken;
import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}