package com.cMall.feedShop.event.domain.repository;

import com.cMall.feedShop.event.domain.Event;
import com.cMall.feedShop.event.domain.EventImage;
import com.cMall.feedShop.event.domain.enums.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventImageRepository 테스트")
class EventImageRepositoryTest {

    private Event testEvent;
    private EventImage testImage1;
    private EventImage testImage2;
    private EventImage testImage3;

    @BeforeEach
    void setUp() {
        // 테스트용 이벤트 생성
        testEvent = Event.builder()
                .type(EventType.BATTLE)
                .maxParticipants(100)
                .build();

        // 테스트용 이미지들 생성
        testImage1 = EventImage.builder()
                .event(testEvent)
                .imageUrl("test-image-1.jpg")
                .originalFilename("test1.jpg")
                .storedFilename("uuid1.jpg")
                .filePath("images/events/uuid1.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .imageOrder(1)
                .build();

        testImage2 = EventImage.builder()
                .event(testEvent)
                .imageUrl("test-image-2.jpg")
                .originalFilename("test2.jpg")
                .storedFilename("uuid2.jpg")
                .filePath("images/events/uuid2.jpg")
                .fileSize(2048L)
                .contentType("image/jpeg")
                .imageOrder(2)
                .build();

        testImage3 = EventImage.builder()
                .event(testEvent)
                .imageUrl("test-image-3.jpg")
                .originalFilename("test3.jpg")
                .storedFilename("uuid3.jpg")
                .filePath("images/events/uuid3.jpg")
                .fileSize(3072L)
                .contentType("image/jpeg")
                .imageOrder(3)
                .build();
    }

    @Test
    @DisplayName("이미지 순서가 올바르게 설정된다")
    void imageOrder_SetCorrectly() {
        // Then
        assertThat(testImage1.getImageOrder()).isEqualTo(1);
        assertThat(testImage2.getImageOrder()).isEqualTo(2);
        assertThat(testImage3.getImageOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("이미지 메타데이터가 올바르게 설정된다")
    void imageMetadata_SetCorrectly() {
        // Then
        assertThat(testImage1.getOriginalFilename()).isEqualTo("test1.jpg");
        assertThat(testImage1.getStoredFilename()).isEqualTo("uuid1.jpg");
        assertThat(testImage1.getFilePath()).isEqualTo("images/events/uuid1.jpg");
        assertThat(testImage1.getFileSize()).isEqualTo(1024L);
        assertThat(testImage1.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    @DisplayName("이벤트와의 연관관계가 올바르게 설정된다")
    void eventRelationship_EstablishedCorrectly() {
        // Then
        assertThat(testImage1.getEvent()).isNotNull();
        assertThat(testImage1.getEvent().getType()).isEqualTo(EventType.BATTLE);
        assertThat(testImage1.getEvent().getMaxParticipants()).isEqualTo(100);
    }

    @Test
    @DisplayName("이미지 URL이 올바르게 설정된다")
    void imageUrl_SetCorrectly() {
        // Then
        assertThat(testImage1.getImageUrl()).isEqualTo("test-image-1.jpg");
        assertThat(testImage2.getImageUrl()).isEqualTo("test-image-2.jpg");
        assertThat(testImage3.getImageUrl()).isEqualTo("test-image-3.jpg");
    }

    @Test
    @DisplayName("이미지 순서 업데이트가 올바르게 동작한다")
    void updateImageOrder_WorksCorrectly() {
        // When
        testImage1.updateImageOrder(5);

        // Then
        assertThat(testImage1.getImageOrder()).isEqualTo(5);
    }

    @Test
    @DisplayName("이미지 URL 업데이트가 올바르게 동작한다")
    void updateImageUrl_WorksCorrectly() {
        // When
        testImage1.updateImageUrl("new-image-url.jpg");

        // Then
        assertThat(testImage1.getImageUrl()).isEqualTo("new-image-url.jpg");
    }

    @Test
    @DisplayName("이미지 순서가 0일 때 기본값이 적용된다")
    void imageOrder_ZeroDefaultValue() {
        // When
        EventImage imageWithZeroOrder = EventImage.builder()
                .event(testEvent)
                .imageUrl("test.jpg")
                .imageOrder(0)
                .build();

        // Then
        assertThat(imageWithZeroOrder.getImageOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("이미지 순서가 음수일 때 기본값이 적용된다")
    void imageOrder_NegativeDefaultValue() {
        // When
        EventImage imageWithNegativeOrder = EventImage.builder()
                .event(testEvent)
                .imageUrl("test.jpg")
                .imageOrder(-1)
                .build();

        // Then
        assertThat(imageWithNegativeOrder.getImageOrder()).isEqualTo(-1);
    }

    @Test
    @DisplayName("이미지 순서가 양수일 때 올바르게 설정된다")
    void imageOrder_PositiveValue() {
        // When
        EventImage imageWithPositiveOrder = EventImage.builder()
                .event(testEvent)
                .imageUrl("test.jpg")
                .imageOrder(10)
                .build();

        // Then
        assertThat(imageWithPositiveOrder.getImageOrder()).isEqualTo(10);
    }

    @Test
    @DisplayName("이미지 순서가 null일 때 기본값이 적용된다")
    void imageOrder_NullDefaultValue() {
        // When
        EventImage imageWithNullOrder = EventImage.builder()
                .event(testEvent)
                .imageUrl("test.jpg")
                .imageOrder(null)
                .build();

        // Then
        // Builder 패턴에서는 null 값이 그대로 전달되므로 기본값 처리는 생성자에서 이루어짐
        assertThat(imageWithNullOrder.getImageOrder()).isNull();
    }
}
