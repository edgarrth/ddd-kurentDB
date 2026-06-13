package com.example.payments.domain.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentRefunded(
        String paymentId,
        BigDecimal amount,
        String reason,
        Instant occurredAt
) implements DomainEvent { }
