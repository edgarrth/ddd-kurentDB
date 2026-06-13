package com.example.payments.domain.events;

import java.time.Instant;

public record PaymentFailed(
        String paymentId,
        String reason,
        Instant occurredAt
) implements DomainEvent { }
