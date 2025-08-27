package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    List<User> findByUserProfile_NameAndUserProfile_Phone(String name, String phone);

    /**
     * 통계 정보가 없는 사용자들 조회
     */
    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT us.user.id FROM UserStats us)")
    List<User> findUsersWithoutStats();
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userProfile WHERE u.id = :userId")
    Optional<User> findByIdWithProfile(@Param("userId") Long userId);
}
