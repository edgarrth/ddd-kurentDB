package com.example.agilepm.domain.event;

import java.time.Instant;

public record ProductOwnerChanged(String tenantId, String productId, String productOwnerId,
                                  Instant occurredAt) implements DomainEvent {
    public String aggregateId() { return productId; }
}
