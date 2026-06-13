package com.example.agilepm.domain.event;

import java.time.Instant;

public record ProductDiscussionRequestTimedOut(String tenantId, String productId, Instant occurredAt) implements DomainEvent {
    public String aggregateId() { return productId; }
}
