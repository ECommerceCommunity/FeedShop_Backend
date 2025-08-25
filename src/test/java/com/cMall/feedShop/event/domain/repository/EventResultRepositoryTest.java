package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.EventResult;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.enums.UserRole;
import com.cMall.feedShop.user.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EventResultRepository 테스트")
class EventResultRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventResultRepository eventResultRepository;

    private Event testEvent;
    private EventDetail testEventDetail;
    private User testUser;
    private EventResult testEventResult;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .loginId("testuser")
                .password("password")
                .email("test@test.com")
                .role(UserRole.USER)
                .build();
        entityManager.persistAndFlush(testUser);

        // 테스트 이벤트 생성
        testEvent = Event.builder()
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING)
                .maxParticipants(10)
                .build();
        entityManager.persistAndFlush(testEvent);

        // 테스트 이벤트 상세 정보 생성
        testEventDetail = EventDetail.builder()
                .title("테스트 이벤트")
                .description("테스트 이벤트 설명")
                .participationMethod("피드 업로드")
                .selectionCriteria("투표 수")
                .precautions("주의사항")
                .eventStartDate(LocalDate.now().minusDays(1))
                .eventEndDate(LocalDate.now().plusDays(1))
                .build();
        testEventDetail.setEvent(testEvent);
        entityManager.persistAndFlush(testEventDetail);

        // 테스트 이벤트 결과 생성
        testEventResult = EventResult.createForEvent(
                testEvent,
                EventResult.ResultType.BATTLE_WINNER,
                2,
                25L
        );
        entityManager.persistAndFlush(testEventResult);
    }

    @Test
    @DisplayName("이벤트 ID로 결과 조회 - 성공")
    void findByEventId_Success() {
        // when
        Optional<EventResult> result = eventResultRepository.findByEventId(testEvent.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEvent().getId()).isEqualTo(testEvent.getId());
        assertThat(result.get().getResultType()).isEqualTo(EventResult.ResultType.BATTLE_WINNER);
    }

    @Test
    @DisplayName("이벤트 ID로 결과 조회 - 존재하지 않는 경우")
    void findByEventId_NotFound() {
        // when
        Optional<EventResult> result = eventResultRepository.findByEventId(999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이벤트 ID로 결과 존재 여부 확인 - 존재함")
    void existsByEventId_Exists() {
        // when
        boolean exists = eventResultRepository.existsByEventId(testEvent.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이벤트 ID로 결과 존재 여부 확인 - 존재하지 않음")
    void existsByEventId_NotExists() {
        // when
        boolean exists = eventResultRepository.existsByEventId(999L);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("이벤트 타입별 결과 조회 - BATTLE")
    void findByEventType_Battle() {
        // given
        Event rankingEvent = Event.builder()
                .type(EventType.RANKING)
                .status(EventStatus.UPCOMING)
                .maxParticipants(20)
                .build();
        entityManager.persistAndFlush(rankingEvent);

        EventDetail rankingEventDetail = EventDetail.builder()
                .title("랭킹 이벤트")
                .description("랭킹 이벤트 설명")
                .participationMethod("피드 업로드")
                .selectionCriteria("투표 수")
                .precautions("주의사항")
                .eventStartDate(LocalDate.now().minusDays(1))
                .eventEndDate(LocalDate.now().plusDays(1))
                .build();
        rankingEventDetail.setEvent(rankingEvent);
        entityManager.persistAndFlush(rankingEventDetail);

        EventResult rankingResult = EventResult.createForEvent(
                rankingEvent,
                EventResult.ResultType.RANKING_TOP3,
                3,
                45L
        );
        entityManager.persistAndFlush(rankingResult);

        // when
        List<EventResult> battleResults = eventResultRepository.findByEventType("BATTLE");

        // then
        assertThat(battleResults).hasSize(1);
        assertThat(battleResults.get(0).getEvent().getType()).isEqualTo(EventType.BATTLE);
    }

    @Test
    @DisplayName("이벤트 타입별 결과 조회 - RANKING")
    void findByEventType_Ranking() {
        // given
        Event rankingEvent = Event.builder()
                .type(EventType.RANKING)
                .status(EventStatus.UPCOMING)
                .maxParticipants(20)
                .build();
        entityManager.persistAndFlush(rankingEvent);

        EventDetail rankingEventDetail = EventDetail.builder()
                .title("랭킹 이벤트")
                .description("랭킹 이벤트 설명")
                .participationMethod("피드 업로드")
                .selectionCriteria("투표 수")
                .precautions("주의사항")
                .eventStartDate(LocalDate.now().minusDays(1))
                .eventEndDate(LocalDate.now().plusDays(1))
                .build();
        rankingEventDetail.setEvent(rankingEvent);
        entityManager.persistAndFlush(rankingEventDetail);

        EventResult rankingResult = EventResult.createForEvent(
                rankingEvent,
                EventResult.ResultType.RANKING_TOP3,
                3,
                45L
        );
        entityManager.persistAndFlush(rankingResult);

        // when
        List<EventResult> rankingResults = eventResultRepository.findByEventType("RANKING");

        // then
        assertThat(rankingResults).hasSize(1);
        assertThat(rankingResults.get(0).getEvent().getType()).isEqualTo(EventType.RANKING);
    }

    @Test
    @DisplayName("특정 기간 내 발표된 결과 조회 - 성공")
    void findByAnnouncedAtBetween_Success() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(2);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);

        // when
        List<EventResult> results = eventResultRepository.findByAnnouncedAtBetween(startDate, endDate);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(testEventResult.getId());
    }

    @Test
    @DisplayName("특정 기간 내 발표된 결과 조회 - 기간 밖")
    void findByAnnouncedAtBetween_OutsidePeriod() {
        // given
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(20);

        // when
        List<EventResult> results = eventResultRepository.findByAnnouncedAtBetween(startDate, endDate);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("이벤트 결과 저장 - 성공")
    void save_Success() {
        // given
        Event newEvent = Event.builder()
                .type(EventType.BATTLE)
                .status(EventStatus.UPCOMING)
                .maxParticipants(5)
                .build();
        entityManager.persistAndFlush(newEvent);

        EventDetail newEventDetail = EventDetail.builder()
                .title("새 이벤트")
                .description("새 이벤트 설명")
                .participationMethod("피드 업로드")
                .selectionCriteria("투표 수")
                .precautions("주의사항")
                .eventStartDate(LocalDate.now().minusDays(1))
                .eventEndDate(LocalDate.now().plusDays(1))
                .build();
        newEventDetail.setEvent(newEvent);
        entityManager.persistAndFlush(newEventDetail);

        EventResult newResult = EventResult.createForEvent(
                newEvent,
                EventResult.ResultType.BATTLE_WINNER,
                1,
                10L
        );

        // when
        EventResult savedResult = eventResultRepository.save(newResult);

        // then
        assertThat(savedResult.getId()).isNotNull();
        assertThat(savedResult.getEvent().getId()).isEqualTo(newEvent.getId());
        assertThat(savedResult.getResultType()).isEqualTo(EventResult.ResultType.BATTLE_WINNER);
    }

    @Test
    @DisplayName("이벤트 결과 수정 - 성공")
    void update_Success() {
        // given
        // 새로운 EventResult를 생성하여 totalParticipants를 설정
        EventResult updatedResult = EventResult.builder()
                .id(testEventResult.getId())
                .event(testEventResult.getEvent())
                .resultType(testEventResult.getResultType())
                .announcedAt(testEventResult.getAnnouncedAt())
                .totalParticipants(5) // 수정된 값
                .totalVotes(testEventResult.getTotalVotes())
                .resultDetails(testEventResult.getResultDetails())
                .build();

        // when
        EventResult savedResult = eventResultRepository.save(updatedResult);

        // then
        assertThat(savedResult.getTotalParticipants()).isEqualTo(5);
    }

    @Test
    @DisplayName("이벤트 결과 삭제 - 성공")
    void delete_Success() {
        // given
        Long resultId = testEventResult.getId();

        // when
        eventResultRepository.deleteById(resultId);

        // then
        Optional<EventResult> deletedResult = eventResultRepository.findById(resultId);
        assertThat(deletedResult).isEmpty();
    }
}
