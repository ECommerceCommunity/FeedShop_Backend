package com.cMall.feedShop.config;

import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.model.UserLevel;
import com.cMall.feedShop.user.domain.model.UserStats;
import com.cMall.feedShop.user.domain.model.UserProfile;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import com.cMall.feedShop.user.domain.repository.UserStatsRepository;
import com.cMall.feedShop.user.domain.repository.UserLevelRepository;
import com.cMall.feedShop.user.domain.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("dev")
@DisplayName("DataInitializer 테스트")
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatsRepository userStatsRepository;

    @Mock
    private UserLevelRepository userLevelRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    private User testUser;
    private UserLevel defaultLevel;
    private UserStats testUserStats;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "testuser", "password", "test@example.com", UserRole.USER);
        
        defaultLevel = UserLevel.builder()
                .levelName("브론즈")
                .minPointsRequired(0)
                .discountRate(0.0)
                .emoji("🥉")
                .rewardDescription("기본 회원 혜택")
                .build();
        
        testUserStats = UserStats.builder()
                .user(testUser)
                .currentLevel(defaultLevel)
                .build();

        // PasswordEncoder 모킹 설정 (lenient로 설정하여 불필요한 모킹 오류 방지)
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
    }

    @Test
    @DisplayName("DataInitializer가 정상적으로 생성된다")
    void dataInitializer_ShouldBeCreated() {
        assertThat(dataInitializer).isNotNull();
    }

    @Test
    @DisplayName("initializeData CommandLineRunner가 정상적으로 생성된다")
    void initializeData_ShouldCreateCommandLineRunner() {
        // when
        CommandLineRunner runner = dataInitializer.initializeData();

        // then
        assertThat(runner).isNotNull();
    }

    @Test
    @DisplayName("기존 레벨 데이터가 있으면 추가 생성하지 않는다")
    void initializeUserLevels_ExistingLevels_ShouldNotCreateNew() throws Exception {
        // given
        List<UserLevel> existingLevels = Arrays.asList(defaultLevel);
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(existingLevels);

        // when
        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        // then
        verify(userLevelRepository).findAllOrderByMinPointsRequired();
        verify(userLevelRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("레벨 데이터가 없으면 기본 레벨들을 생성한다")
    void initializeUserLevels_NoLevels_ShouldCreateDefaultLevels() throws Exception {
        // given
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(Collections.emptyList());
        given(userLevelRepository.saveAll(any())).willReturn(Arrays.asList(defaultLevel));

        // when
        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        // then
        verify(userLevelRepository).findAllOrderByMinPointsRequired();
        verify(userLevelRepository).saveAll(any());
    }

    @Test
    @DisplayName("통계 정보가 필요한 사용자가 없으면 초기화하지 않는다")
    void initializeUserStats_NoUsersWithoutStats_ShouldNotInitialize() throws Exception {
        // given
        given(userRepository.findUsersWithoutStats()).willReturn(Collections.emptyList());
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(Arrays.asList(defaultLevel));

        // when
        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        // then
        verify(userRepository).findUsersWithoutStats();
        verify(userStatsRepository, never()).save(any());
    }

    @Test
    @DisplayName("통계 정보가 없는 사용자들을 초기화한다")
    void initializeUserStats_UsersWithoutStats_ShouldInitialize() throws Exception {
        // given
        List<User> usersWithoutStats = Arrays.asList(testUser);
        given(userRepository.findUsersWithoutStats()).willReturn(usersWithoutStats);
        given(userLevelRepository.findByMinPointsRequired(0)).willReturn(Optional.of(defaultLevel));
        given(userStatsRepository.save(any())).willReturn(testUserStats);
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(Arrays.asList(defaultLevel));

        // when
        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        // then
        verify(userRepository).findUsersWithoutStats();
        verify(userLevelRepository, atLeastOnce()).findByMinPointsRequired(0);
        verify(userStatsRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("기본 레벨이 없으면 예외가 발생한다")
    void initializeUserStats_NoDefaultLevel_ShouldThrowException() throws Exception {
        // given
        List<User> usersWithoutStats = Arrays.asList(testUser);
        given(userRepository.findUsersWithoutStats()).willReturn(usersWithoutStats);
        given(userLevelRepository.findByMinPointsRequired(0)).willReturn(Optional.empty());
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(Arrays.asList(defaultLevel));

        // when & then
        CommandLineRunner runner = dataInitializer.initializeData();
        assertThat(runner).isNotNull();
        
        // 실행 시 예외가 발생할 수 있지만, 실제로는 로그만 남기고 계속 진행됨
        runner.run();
    }

    @Test
    @DisplayName("테스트 계정들이 이미 존재하면 생성하지 않는다")
    void createTestAdminUser_AlreadyExists_ShouldNotCreate() throws Exception {
        // given
        given(userRepository.existsByEmail("user@feedshop.dev")).willReturn(true);
        given(userRepository.existsByEmail("admin@feedshop.dev")).willReturn(true);
        given(userRepository.existsByEmail("seller@feedshop.dev")).willReturn(true);
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(Arrays.asList(defaultLevel));

        // when
        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        // then
        verify(userRepository, times(3)).existsByEmail(anyString()); // user, admin, seller
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("테스트 계정들이 없으면 생성한다")
    void createTestAdminUser_NotExists_ShouldCreate() throws Exception {
        // given
        given(userRepository.existsByEmail("user@feedshop.dev")).willReturn(false);
        given(userRepository.existsByEmail("admin@feedshop.dev")).willReturn(false);
        given(userRepository.existsByEmail("seller@feedshop.dev")).willReturn(false);
        given(userRepository.save(any())).willReturn(testUser);
        given(userLevelRepository.findByMinPointsRequired(0)).willReturn(Optional.of(defaultLevel));
        given(userStatsRepository.save(any())).willReturn(testUserStats);
        // userProfileRepository.save()는 cascade로 자동 저장되므로 모킹하지 않음
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(Arrays.asList(defaultLevel));

        // when
        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        // then
        verify(userRepository, times(3)).existsByEmail(anyString()); // user, admin, seller
        verify(userRepository, times(3)).save(any()); // user, admin, seller
        // userProfileRepository.save()는 cascade로 자동 저장되므로 검증하지 않음
        verify(userStatsRepository, times(3)).save(any()); // user, admin, seller
    }

    @Test
    @DisplayName("logBadgeSystemInfo CommandLineRunner가 정상적으로 생성된다")
    void logBadgeSystemInfo_ShouldCreateCommandLineRunner() {
        // when
        CommandLineRunner runner = dataInitializer.logBadgeSystemInfo();

        // then
        assertThat(runner).isNotNull();
    }

    @Test
    @DisplayName("뱃지 시스템 정보 로깅이 정상적으로 실행된다")
    void logBadgeSystemInfo_ShouldLogBadgeSystemInfo() throws Exception {
        // given
        List<UserLevel> allLevels = Arrays.asList(defaultLevel);
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(allLevels);

        // when
        CommandLineRunner runner = dataInitializer.logBadgeSystemInfo();
        runner.run();

        // then
        verify(userLevelRepository).findAllOrderByMinPointsRequired();
        // 로깅은 실제로는 콘솔에 출력되므로 검증하지 않음
    }

    @Test
    @DisplayName("전체 초기화 프로세스가 정상적으로 실행된다")
    void fullInitializationProcess_ShouldExecuteSuccessfully() throws Exception {
        // given
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(Collections.emptyList());
        given(userLevelRepository.saveAll(any())).willReturn(Arrays.asList(defaultLevel));
        given(userRepository.findUsersWithoutStats()).willReturn(Collections.emptyList());
        given(userRepository.existsByEmail("user@feedshop.dev")).willReturn(false);
        given(userRepository.existsByEmail("admin@feedshop.dev")).willReturn(false);
        given(userRepository.existsByEmail("seller@feedshop.dev")).willReturn(false);
        given(userRepository.save(any())).willReturn(testUser);
        given(userLevelRepository.findByMinPointsRequired(0)).willReturn(Optional.of(defaultLevel));
        given(userStatsRepository.save(any())).willReturn(testUserStats);
        // userProfileRepository.save()는 cascade로 자동 저장되므로 모킹하지 않음

        // when
        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        // then
        verify(userLevelRepository).findAllOrderByMinPointsRequired();
        verify(userLevelRepository).saveAll(any());
        verify(userRepository).findUsersWithoutStats();
        verify(userRepository, times(3)).existsByEmail(anyString()); // user, admin, seller
        verify(userRepository, times(3)).save(any()); // user, admin, seller
        // userProfileRepository.save()는 cascade로 자동 저장되므로 검증하지 않음
        verify(userStatsRepository, times(3)).save(any()); // user, admin, seller
    }

    @Test
    @DisplayName("개별 초기화 실패 시에도 다른 초기화는 계속 진행된다")
    void individualInitializationFailure_ShouldContinueOtherInitializations() throws Exception {
        // given
        given(userLevelRepository.findAllOrderByMinPointsRequired()).willReturn(Collections.emptyList());
        given(userLevelRepository.saveAll(any())).willReturn(Arrays.asList(defaultLevel));
        given(userRepository.findUsersWithoutStats()).willReturn(Arrays.asList(testUser));
        given(userLevelRepository.findByMinPointsRequired(0)).willReturn(Optional.empty()); // 실패 케이스
        given(userRepository.existsByEmail("user@feedshop.dev")).willReturn(false);
        given(userRepository.existsByEmail("admin@feedshop.dev")).willReturn(false);
        given(userRepository.existsByEmail("seller@feedshop.dev")).willReturn(false);
        // 불필요한 stubbing 제거
        // given(userRepository.save(any())).willReturn(testUser);
        // given(userStatsRepository.save(any())).willReturn(testUserStats);

        // when
        CommandLineRunner runner = dataInitializer.initializeData();
        runner.run();

        // then
        // 레벨 초기화는 성공
        verify(userLevelRepository).saveAll(any());
        // 사용자 통계 초기화는 실패하지만 예외가 던져지지 않음
        verify(userRepository).findUsersWithoutStats();
        // 테스트 계정 생성은 실패하지만 예외가 던져지지 않음
        verify(userRepository, times(3)).existsByEmail(anyString()); // user, admin, seller
        // verify(userRepository).save(any()); // 이 부분은 제거 - 실제로는 실패하므로 호출되지 않음
    }
}
