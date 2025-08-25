package com.cMall.feedShop.config;

import com.cMall.feedShop.user.domain.model.UserLevel;
import com.cMall.feedShop.user.domain.repository.UserLevelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializerConfig implements CommandLineRunner {

    private final UserLevelRepository userLevelRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeUserLevels();
    }

    private void initializeUserLevels() {
        if (userLevelRepository.count() > 0) {
            log.info("UserLevel data already exists. Skipping initialization.");
            return;
        }

        log.info("Initializing UserLevel data...");

        UserLevel[] levels = {
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
                .discountRate(2.0)
                .emoji("🥈")
                .rewardDescription("2% 할인 혜택")
                .build(),
            
            UserLevel.builder()
                .levelName("골드")
                .minPointsRequired(300)
                .discountRate(5.0)
                .emoji("🥇")
                .rewardDescription("5% 할인 혜택")
                .build(),
            
            UserLevel.builder()
                .levelName("플래티넘")
                .minPointsRequired(600)
                .discountRate(8.0)
                .emoji("💎")
                .rewardDescription("8% 할인 혜택")
                .build(),
            
            UserLevel.builder()
                .levelName("VIP")
                .minPointsRequired(1000)
                .discountRate(10.0)
                .emoji("👑")
                .rewardDescription("10% 할인 혜택 + 우선 배송")
                .build(),
            
            UserLevel.builder()
                .levelName("VVIP")
                .minPointsRequired(2000)
                .discountRate(15.0)
                .emoji("💫")
                .rewardDescription("15% 할인 혜택 + 전용 상담사")
                .build(),
            
            UserLevel.builder()
                .levelName("다이아몬드")
                .minPointsRequired(3000)
                .discountRate(18.0)
                .emoji("💍")
                .rewardDescription("18% 할인 혜택 + 무료 배송")
                .build(),
            
            UserLevel.builder()
                .levelName("마스터")
                .minPointsRequired(5000)
                .discountRate(20.0)
                .emoji("🔥")
                .rewardDescription("20% 할인 혜택 + 특별 이벤트 초대")
                .build(),
            
            UserLevel.builder()
                .levelName("레전드")
                .minPointsRequired(8000)
                .discountRate(25.0)
                .emoji("⚡")
                .rewardDescription("25% 할인 혜택 + 전용 혜택")
                .build(),
            
            UserLevel.builder()
                .levelName("신화")
                .minPointsRequired(15000)
                .discountRate(30.0)
                .emoji("🌟")
                .rewardDescription("30% 할인 혜택 + 모든 프리미엄 서비스")
                .build()
        };

        for (UserLevel level : levels) {
            userLevelRepository.save(level);
        }

        log.info("UserLevel initialization completed. {} levels created.", levels.length);
    }
}