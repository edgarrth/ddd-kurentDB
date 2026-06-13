package com.example.payments.domain.events;

import java.time.Instant;

public record PaymentAuthorized(
        String paymentId,
        String authorizationCode,
        Instant occurredAt
) implements DomainEvent { }
