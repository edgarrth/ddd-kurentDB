package com.example.agilepm.domain.event;

import java.time.Instant;

public record ProductInitiated(String tenantId, String productId, String name, String description,
                               String productOwnerId, Instant occurredAt) implements DomainEvent {
    public String aggregateId() { return productId; }
}
