package com.example.payments.domain.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentInitiated(
        String paymentId,
        String merchantId,
        String customerId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String orderId,
        Instant occurredAt
) implements DomainEvent { }
