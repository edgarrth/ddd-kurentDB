package com.example.payments.domain.events;

import java.time.Instant;

public record PaymentCaptured(
        String paymentId,
        String captureReference,
        Instant occurredAt
) implements DomainEvent { }
