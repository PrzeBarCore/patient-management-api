package com.przebarcore.laboratoryapi.entity;

import com.przebarcore.laboratoryapi.global.AggregateType;
import com.przebarcore.laboratoryapi.global.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long aggregateId;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private AggregateType aggregateType;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;
    @Column(nullable = false)
    private String payload;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
