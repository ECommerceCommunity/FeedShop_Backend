package com.cMall.feedShop.event.application.service;

import com.cMall.feedShop.event.application.dto.response.EventSummaryDto;
import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventDetail;
import com.cMall.feedShop.event.domain.EventReward;
import com.cMall.feedShop.event.domain.enums.EventStatus;
import com.cMall.feedShop.event.domain.enums.EventType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EventMapperTest {
    private final EventMapper mapper = new EventMapper();

    @Test
    void toSummaryDto_모든필드_정상매핑() {
        EventDetail detail = EventDetail.builder()
                .title("이벤트 제목")
                .description("설명")
                .eventStartDate(LocalDate.of(2024, 7, 1))
                .eventEndDate(LocalDate.of(2024, 7, 10))
                .purchaseStartDate(LocalDate.of(2024, 6, 1))
                .purchaseEndDate(LocalDate.of(2024, 6, 30))
                .announcement(LocalDate.of(2024, 7, 15))
                .participationMethod("참여방법")
                .selectionCriteria("선정기준")
                .imageUrl("http://img.com")
                .precautions("주의사항")
                .build();
        EventReward reward = EventReward.builder()
                .conditionValue(1)
                .rewardValue("상품권")
                .build();
        Event event = Event.builder()
                .id(1L)
                .type(EventType.BATTLE)
                .status(EventStatus.ONGOING)
                .maxParticipants(100)
                .createdBy(LocalDateTime.of(2024, 5, 1, 12, 0))
                .eventDetail(detail)
                .rewards(List.of(reward))
                .build();
        detail.setEvent(event);
        EventSummaryDto dto = mapper.toSummaryDto(event);
        assertThat(dto.getEventId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("이벤트 제목");
        assertThat(dto.getType()).isEqualTo("battle");
        assertThat(dto.getStatus()).isEqualTo("ended");
        assertThat(dto.getEventStartDate()).isEqualTo("2024-07-01");
        assertThat(dto.getEventEndDate()).isEqualTo("2024-07-10");
        assertThat(dto.getImageUrl()).isEqualTo("http://img.com");
        assertThat(dto.getMaxParticipants()).isEqualTo(100);
        assertThat(dto.getDescription()).isEqualTo("설명");
        assertThat(dto.getRewards()).hasSize(1);
        assertThat(dto.getRewards().get(0).getRank()).isEqualTo(1);
        assertThat(dto.getRewards().get(0).getReward()).isEqualTo("상품권");
        assertThat(dto.getParticipationMethod()).isEqualTo("참여방법");
        assertThat(dto.getSelectionCriteria()).isEqualTo("선정기준");
        assertThat(dto.getPrecautions()).isEqualTo("주의사항");
        assertThat(dto.getCreatedAt()).isEqualTo("2024-05-01T12:00");
        assertThat(dto.getPurchasePeriod()).isEqualTo("2024-06-01 ~ 2024-06-30");
        assertThat(dto.getVotePeriod()).isEqualTo("2024-07-01 ~ 2024-07-10");
        assertThat(dto.getAnnouncementDate()).isEqualTo("2024-07-15");
    }

    @Test
    void toSummaryDto_nullSafe_테스트() {
        Event event = Event.builder()
                .id(2L)
                .type(null)
                .status(null)
                .maxParticipants(null)
                .createdBy(null)
                .eventDetail(null)
                .rewards(null)
                .build();
        EventSummaryDto dto = mapper.toSummaryDto(event);
        assertThat(dto.getType()).isNull();
        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getTitle()).isEqualTo("");
        assertThat(dto.getDescription()).isEqualTo("");
        assertThat(dto.getRewards()).isEmpty();
        assertThat(dto.getPurchasePeriod()).isEqualTo("");
        assertThat(dto.getVotePeriod()).isEqualTo("");
        assertThat(dto.getAnnouncementDate()).isEqualTo("");
    }
} 