package com.example.agilepm.domain.event;

import java.time.Instant;
import java.time.LocalDate;

public record ReleaseScheduled(String tenantId, String productId, String releaseId, String name,
                               LocalDate scheduledFor, Instant occurredAt) implements DomainEvent {
    public String aggregateId() { return productId; }
}
