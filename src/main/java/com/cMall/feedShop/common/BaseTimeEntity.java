package com.cMall.feedShop.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter // Lombok: 모든 필드에 대한 getter 메서드를 자동으로 생성해줍니다.
@MappedSuperclass // JPA: 이 클래스가 엔티티들의 상위 클래스임을 나타냅니다. 이 클래스의 필드들은 자식 엔티티의 테이블 컬럼으로 매핑됩니다. (테이블로 생성되지는 않음)
@EntityListeners(AuditingEntityListener.class) // JPA: 엔티티의 영속성 이벤트(생성, 수정 등)를 감지하는 리스너를 지정합니다. 여기서는 Spring Data JPA의 Auditing 기능을 활성화합니다.
public class BaseTimeEntity {
    @CreatedDate // Spring Data JPA Auditing: 엔티티가 생성될 때 현재 시간을 자동으로 주입합니다.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // Spring Data JPA Auditing: 엔티티가 수정될 때마다 현재 시간을 자동으로 주입합니다.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
