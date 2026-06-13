package com.example.agilepm.domain.event;

import java.time.Instant;

public record ProductDescriptionChanged(String tenantId, String productId, String description,
                                        Instant occurredAt) implements DomainEvent {
    public String aggregateId() { return productId; }
}
