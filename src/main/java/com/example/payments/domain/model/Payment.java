package com.example.payments.domain.model;

import com.example.payments.domain.events.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Payment {
    private String paymentId;
    private String merchantId;
    private String customerId;
    private Money money;
    private String paymentMethod;
    private String orderId;
    private PaymentStatus status;
    private String authorizationCode;
    private String captureReference;
    private final List<DomainEvent> pendingEvents = new ArrayList<>();

    private Payment() { }

    public static Payment initiate(String paymentId, String merchantId, String customerId, Money money, String paymentMethod, String orderId) {
        Payment payment = new Payment();
        payment.raise(new PaymentInitiated(paymentId, merchantId, customerId, money.amount(), money.currency().getCurrencyCode(), paymentMethod, orderId, Instant.now()));
        return payment;
    }

    public static Payment rehydrate(List<DomainEvent> history) {
        Payment payment = new Payment();
        history.forEach(payment::apply);
        return payment;
    }

    public void authorize(String authorizationCode) {
        requireStatus(PaymentStatus.NEW, "Only NEW payments can be authorized");
        raise(new PaymentAuthorized(paymentId, authorizationCode, Instant.now()));
    }

    public void capture(String captureReference) {
        requireStatus(PaymentStatus.AUTHORIZED, "Only AUTHORIZED payments can be captured");
        raise(new PaymentCaptured(paymentId, captureReference, Instant.now()));
    }

    public void fail(String reason) {
        if (status == PaymentStatus.CAPTURED || status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Captured or refunded payments cannot be failed");
        }
        raise(new PaymentFailed(paymentId, reason, Instant.now()));
    }

    public void refund(BigDecimal amount, String reason) {
        requireStatus(PaymentStatus.CAPTURED, "Only CAPTURED payments can be refunded");
        if (amount == null || amount.signum() <= 0 || amount.compareTo(money.amount()) > 0) {
            throw new IllegalArgumentException("Refund amount must be positive and not greater than captured amount");
        }
        raise(new PaymentRefunded(paymentId, amount, reason, Instant.now()));
    }

    public void cancel(String reason) {
        if (status != PaymentStatus.NEW && status != PaymentStatus.AUTHORIZED) {
            throw new IllegalStateException("Only NEW or AUTHORIZED payments can be cancelled");
        }
        raise(new PaymentCancelled(paymentId, reason, Instant.now()));
    }

    private void raise(DomainEvent event) {
        apply(event);
        pendingEvents.add(event);
    }

    private void apply(DomainEvent event) {
        switch (event) {
            case PaymentInitiated e -> {
                this.paymentId = e.paymentId();
                this.merchantId = e.merchantId();
                this.customerId = e.customerId();
                this.money = Money.of(e.amount(), e.currency());
                this.paymentMethod = e.paymentMethod();
                this.orderId = e.orderId();
                this.status = PaymentStatus.NEW;
            }
            case PaymentAuthorized e -> {
                this.authorizationCode = e.authorizationCode();
                this.status = PaymentStatus.AUTHORIZED;
            }
            case PaymentCaptured e -> {
                this.captureReference = e.captureReference();
                this.status = PaymentStatus.CAPTURED;
            }
            case PaymentFailed ignored -> this.status = PaymentStatus.FAILED;
            case PaymentRefunded ignored -> this.status = PaymentStatus.REFUNDED;
            case PaymentCancelled ignored -> this.status = PaymentStatus.CANCELLED;
        }
    }

    private void requireStatus(PaymentStatus expected, String message) {
        if (status != expected) throw new IllegalStateException(message + ". Current status: " + status);
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
    }

    public String paymentId() { return paymentId; }
    public String merchantId() { return merchantId; }
    public String customerId() { return customerId; }
    public Money money() { return money; }
    public String paymentMethod() { return paymentMethod; }
    public String orderId() { return orderId; }
    public PaymentStatus status() { return status; }
    public String authorizationCode() { return authorizationCode; }
    public String captureReference() { return captureReference; }
}
