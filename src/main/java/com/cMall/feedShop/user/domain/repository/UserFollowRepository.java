package com.cMall.feedShop.user.domain.repository;

import com.cMall.feedShop.user.domain.model.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 팔로우 관계를 관리하는 Repository
 */
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    /**
     * 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인
     * 
     * @param followerId 팔로워 ID (팔로우를 하는 사용자)
     * @param followingId 팔로잉 ID (팔로우를 받는 사용자)
     * @return 팔로우 관계 존재 여부
     */
    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    /**
     * 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인 (UserFollow 엔티티 반환)
     * 
     * @param followerId 팔로워 ID
     * @param followingId 팔로잉 ID
     * @return UserFollow 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<UserFollow> findByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    /**
     * 특정 사용자의 팔로워 수 조회
     * 
     * @param userId 팔로잉을 받는 사용자 ID
     * @return 팔로워 수
     */
    long countByFollowing_Id(Long userId);

    /**
     * 특정 사용자의 팔로잉 수 조회
     * 
     * @param userId 팔로우를 하는 사용자 ID
     * @return 팔로잉 수
     */
    long countByFollower_Id(Long userId);

    /**
     * 특정 사용자의 팔로워 목록 조회 (페이징)
     * 
     * @param userId 팔로잉을 받는 사용자 ID
     * @param pageable 페이징 정보
     * @return 팔로워 목록 (UserFollow 엔티티)
     */
    @Query("SELECT uf FROM UserFollow uf " +
           "JOIN FETCH uf.follower f " +
           "JOIN FETCH f.userProfile " +
           "WHERE uf.following.id = :userId " +
           "ORDER BY uf.createdAt DESC")
    Page<UserFollow> findFollowersByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 사용자의 팔로잉 목록 조회 (페이징)
     * 
     * @param userId 팔로우를 하는 사용자 ID
     * @param pageable 페이징 정보
     * @return 팔로잉 목록 (UserFollow 엔티티)
     */
    @Query("SELECT uf FROM UserFollow uf " +
           "JOIN FETCH uf.following f " +
           "JOIN FETCH f.userProfile " +
           "WHERE uf.follower.id = :userId " +
           "ORDER BY uf.createdAt DESC")
    Page<UserFollow> findFollowingsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 사용자가 팔로우하고 있는 사용자 ID 목록 조회
     * 
     * @param userId 팔로우를 하는 사용자 ID
     * @return 팔로잉하고 있는 사용자 ID 목록
     */
    @Query("SELECT uf.following.id FROM UserFollow uf WHERE uf.follower.id = :userId")
    List<Long> findFollowingUserIdsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자를 팔로우하고 있는 사용자 ID 목록 조회
     * 
     * @param userId 팔로잉을 받는 사용자 ID
     * @return 팔로워 ID 목록
     */
    @Query("SELECT uf.follower.id FROM UserFollow uf WHERE uf.following.id = :userId")
    List<Long> findFollowerUserIdsByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 팔로우 관계 삭제
     * 
     * @param followerId 팔로워 ID
     * @param followingId 팔로잉 ID
     */
    void deleteByFollower_IdAndFollowing_Id(Long followerId, Long followingId);
}
