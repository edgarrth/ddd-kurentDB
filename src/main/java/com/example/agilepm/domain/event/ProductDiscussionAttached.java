package com.example.agilepm.domain.event;

import java.time.Instant;

public record ProductDiscussionAttached(String tenantId, String productId, String discussionId,
                                        Instant occurredAt) implements DomainEvent {
    public String aggregateId() { return productId; }
}
