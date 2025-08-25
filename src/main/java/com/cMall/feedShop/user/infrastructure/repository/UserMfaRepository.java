package com.cMall.feedShop.user.infrastructure.repository;

import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.cMall.feedShop.user.domain.model.UserMfa;

import java.util.Optional;

@Repository
public interface UserMfaRepository extends JpaRepository<UserMfa, Long> {
    Optional<UserMfa> findByUser(User user);
    Optional<UserMfa> findByUserEmail(String email);
    boolean existsByUser(User user);
    boolean existsByUserEmail(String email);
    void deleteByUser(User user);

    @Query("SELECT um FROM UserMfa um JOIN FETCH um.user WHERE um.user.email = :email")
    Optional<UserMfa> findByUserEmailWithUser(@Param("email") String email);
}