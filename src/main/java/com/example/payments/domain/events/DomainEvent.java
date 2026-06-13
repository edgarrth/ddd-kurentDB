package com.example.payments.domain.events;

import java.time.Instant;

public sealed interface DomainEvent permits PaymentInitiated, PaymentAuthorized, PaymentCaptured, PaymentFailed, PaymentRefunded, PaymentCancelled {
    String paymentId();
    Instant occurredAt();
    default String eventType() { return getClass().getSimpleName(); }
}
