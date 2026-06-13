package com.example.agilepm.domain.event;

import java.time.Instant;

public sealed interface DomainEvent permits ProductInitiated, ProductDescriptionChanged, ProductOwnerChanged,
        ProductDiscussionRequested, ProductDiscussionAttached, ProductDiscussionRequestTimedOut,
        SprintPlanned, ReleaseScheduled {
    String aggregateId();
    Instant occurredAt();
    default String eventType() { return this.getClass().getSimpleName(); }
}
