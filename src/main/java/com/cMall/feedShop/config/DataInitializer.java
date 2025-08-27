package com.cMall.feedShop.config;

import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserLevel;
import com.cMall.feedShop.user.domain.model.UserStats;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserStatsRepository;
import com.cMall.feedShop.user.domain.repository.UserLevelRepository;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 애플리케이션 시작 시 초기 데이터를 설정하는 클래스
 * 개발 환경에서만 실행되도록 @Profile("dev") 설정
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final UserLevelRepository userLevelRepository;
    private final UserProfileRepository userProfileRepository;

    @Bean
    @Profile("dev")
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("=== 초기 데이터 설정 시작 ===");
            
            // 기본 레벨 데이터 초기화
            initializeUserLevels();
            
            // 사용자 통계 초기화
            initializeUserStats();
            
            // 테스트용 관리자 계정 생성 (개발 환경에서만)
            createTestAdminUser();
            
            log.info("=== 초기 데이터 설정 완료 ===");
        };
    }

    /**
     * 기본 레벨 데이터 초기화
     * user_levels 테이블이 비어있을 경우 기본 레벨들을 생성
     * 기존 DB 구조에 맞춰 브론즈~신화 레벨 시스템 사용
     */
    private void initializeUserLevels() {
        log.info("기본 레벨 데이터 초기화 시작...");
        
        // 기존 레벨 데이터가 있는지 확인
        List<UserLevel> existingLevels = userLevelRepository.findAllOrderByMinPointsRequired();
        
        if (!existingLevels.isEmpty()) {
            log.info("기존 레벨 데이터가 {}개 존재합니다. 추가 생성하지 않습니다.", existingLevels.size());
            return;
        }
        
        log.info("기존 레벨 데이터가 없습니다. 기본 레벨들을 생성합니다.");
        
        // 기존 DB 구조에 맞는 기본 레벨 데이터 생성 (브론즈~신화)
        List<UserLevel> defaultLevels = List.of(
            UserLevel.builder()
                .levelName("브론즈")
                .minPointsRequired(0)
                .discountRate(0.0)
                .emoji("🥉")
                .rewardDescription("기본 회원 혜택")
                .build(),
            UserLevel.builder()
                .levelName("실버")
                .minPointsRequired(100)
                .discountRate(0.02)
                .emoji("🥈")
                .rewardDescription("2% 할인 혜택")
                .build(),
            UserLevel.builder()
                .levelName("골드")
                .minPointsRequired(300)
                .discountRate(0.05)
                .emoji("🥇")
                .rewardDescription("5% 할인 혜택")
                .build(),
            UserLevel.builder()
                .levelName("플래티넘")
                .minPointsRequired(600)
                .discountRate(0.08)
                .emoji("💎")
                .rewardDescription("8% 할인 혜택")
                .build(),
            UserLevel.builder()
                .levelName("VIP")
                .minPointsRequired(1000)
                .discountRate(0.10)
                .emoji("👑")
                .rewardDescription("10% 할인 혜택 + 우선 배송")
                .build(),
            UserLevel.builder()
                .levelName("VVIP")
                .minPointsRequired(2000)
                .discountRate(0.15)
                .emoji("⭐")
                .rewardDescription("15% 할인 혜택 + 전용 상담사")
                .build(),
            UserLevel.builder()
                .levelName("다이아몬드")
                .minPointsRequired(3000)
                .discountRate(0.18)
                .emoji("💍")
                .rewardDescription("18% 할인 혜택 + 무료 배송")
                .build(),
            UserLevel.builder()
                .levelName("마스터")
                .minPointsRequired(5000)
                .discountRate(0.20)
                .emoji("🎯")
                .rewardDescription("20% 할인 혜택 + 특별 이벤트 초대")
                .build(),
            UserLevel.builder()
                .levelName("레전드")
                .minPointsRequired(8000)
                .discountRate(0.25)
                .emoji("⚡")
                .rewardDescription("25% 할인 혜택 + 전용 혜택")
                .build(),
            UserLevel.builder()
                .levelName("신화")
                .minPointsRequired(15000)
                .discountRate(0.30)
                .emoji("✨")
                .rewardDescription("30% 할인 혜택 + 모든 프리미엄 서비스")
                .build()
        );
        
        try {
            userLevelRepository.saveAll(defaultLevels);
            log.info("기본 레벨 데이터 생성 완료: {}개", defaultLevels.size());
        } catch (Exception e) {
            log.error("기본 레벨 데이터 생성 실패: {}", e.getMessage());
        }
    }

    /**
     * 기존 사용자들의 통계 정보 초기화
     * user_stats 테이블이 비어있는 사용자들을 찾아서 기본 통계 정보 생성
     */
    private void initializeUserStats() {
        log.info("사용자 통계 정보 초기화 시작...");
        
        // 통계 정보가 없는 사용자들 조회
        List<User> usersWithoutStats = userRepository.findUsersWithoutStats();
        
        if (usersWithoutStats.isEmpty()) {
            log.info("통계 정보가 필요한 사용자가 없습니다.");
            return;
        }

        int initializedCount = 0;
        for (User user : usersWithoutStats) {
            try {
                // 기본 레벨 조회
                UserLevel defaultLevel = userLevelRepository.findByMinPointsRequired(0)
                        .orElseThrow(() -> new IllegalStateException("기본 레벨을 찾을 수 없습니다."));
                
                // 기본 통계 정보 생성
                UserStats userStats = UserStats.builder()
                        .user(user)
                        .currentLevel(defaultLevel)
                        .build();
                
                userStatsRepository.save(userStats);
                initializedCount++;
                
                log.debug("사용자 {} 의 통계 정보 초기화 완료", user.getEmail());
                
            } catch (Exception e) {
                log.error("사용자 {} 의 통계 정보 초기화 실패: {}", user.getEmail(), e.getMessage());
            }
        }
        
        log.info("사용자 통계 정보 초기화 완료: {} 명", initializedCount);
    }

    /**
     * 개발 환경용 테스트 계정 생성
     * USER, ADMIN, SELLER 역할의 테스트 계정을 모두 생성
     */
    private void createTestAdminUser() {
        createTestUser("user@feedshop.dev", "user", UserRole.USER);
        createTestUser("admin@feedshop.dev", "admin", UserRole.ADMIN);
        createTestUser("seller@feedshop.dev", "seller", UserRole.SELLER);
    }
    
    /**
     * 테스트 계정 생성 헬퍼 메서드
     */
    private void createTestUser(String email, String loginId, UserRole role) {
        // 이미 존재하는지 확인
        if (userRepository.existsByEmail(email)) {
            log.info("테스트 {} 계정이 이미 존재합니다: {}", role.name(), email);
            return;
        }

        try {
            // 테스트 사용자 생성
            User testUser = new User(
                    loginId,
                    "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa", // "password" 암호화
                    email,
                    role
            );
            
            // 테스트 계정은 ACTIVE 상태로 설정
            testUser.setStatus(UserStatus.ACTIVE);
            
            User savedUser = userRepository.save(testUser);
            
            // 사용자 프로필 생성
            String displayName = getDisplayNameByRole(role);
            UserProfile userProfile = UserProfile.builder()
                    .user(savedUser)
                    .name(displayName)
                    .nickname(displayName)
                    .phone("010-1234-5678")
                    .build();
            
            userProfileRepository.save(userProfile);
            
            // 기본 레벨 조회
            UserLevel defaultLevel = userLevelRepository.findByMinPointsRequired(0)
                    .orElseThrow(() -> new IllegalStateException("기본 레벨을 찾을 수 없습니다."));
            
            // 사용자 통계 정보 생성
            UserStats userStats = UserStats.builder()
                    .user(savedUser)
                    .currentLevel(defaultLevel)
                    .build();
            
            userStatsRepository.save(userStats);
            
            log.info("테스트 {} 계정 생성 완료: {} (비밀번호: password, 닉네임: {})", role.name(), email, displayName);
            
        } catch (Exception e) {
            log.error("테스트 {} 계정 생성 실패: {}", role.name(), e.getMessage());
        }
    }
    
    /**
     * 역할에 따른 표시 이름 반환
     */
    private String getDisplayNameByRole(UserRole role) {
        return switch (role) {
            case USER -> "테스트 사용자";
            case ADMIN -> "테스트 관리자";
            case SELLER -> "테스트 판매자";
        };
    }

    /**
     * 뱃지 시스템 정보 로깅
     * BadgeType enum에 정의된 모든 뱃지 정보를 로그로 출력
     */
    @Bean
    @Profile("dev")
    public CommandLineRunner logBadgeSystemInfo() {
        return args -> {
            log.info("=== 뱃지 시스템 정보 ===");
            log.info("총 뱃지 타입 수: {} 개", com.cMall.feedShop.user.domain.model.BadgeType.values().length);
            
            for (com.cMall.feedShop.user.domain.model.BadgeType badgeType : com.cMall.feedShop.user.domain.model.BadgeType.values()) {
                log.info("뱃지: {} - {} (보너스 점수: {}점)", 
                        badgeType.name(), 
                        badgeType.getName(), 
                        badgeType.getBonusPoints());
            }
            
            log.info("=== 레벨 시스템 정보 ===");
            List<UserLevel> allLevels = userLevelRepository.findAllOrderByMinPointsRequired();
            log.info("총 레벨 수: {} 개", allLevels.size());
            
            for (UserLevel level : allLevels) {
                log.info("레벨: {} - {} (필요 점수: {}점)", 
                        level.getLevelId(), 
                        level.getDisplayName(), 
                        level.getMinPointsRequired());
            }
            
            log.info("=== 활동별 점수 체계 ===");
            log.info("구매 완료: +5점");
            log.info("리뷰 작성: +10점");
            log.info("피드 작성: +10점");
            log.info("댓글 작성: +3점");
            log.info("투표 참여: +1점");
            log.info("좋아요 받기: +1점");
            log.info("SNS 공유: +3점");
            log.info("이벤트 참여: +2점");
            log.info("이벤트 수상: +50점");
        };
    }
}
