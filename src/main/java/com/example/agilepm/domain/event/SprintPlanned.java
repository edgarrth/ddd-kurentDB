package com.example.agilepm.domain.event;

import java.time.Instant;
import java.time.LocalDate;

public record SprintPlanned(String tenantId, String productId, String sprintId, String name,
                            LocalDate startsOn, LocalDate endsOn, Instant occurredAt) implements DomainEvent {
    public String aggregateId() { return productId; }
}
