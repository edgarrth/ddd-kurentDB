package com.example.payments.application.actors;

import com.example.payments.domain.commands.*;
import com.example.payments.domain.events.DomainEvent;
import com.example.payments.domain.model.Money;
import com.example.payments.domain.model.Payment;
import com.example.payments.domain.ports.PaymentEventStore;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public final class PaymentActor {
    private final String paymentId;
    private final PaymentEventStore eventStore;
    private final ReentrantLock lock = new ReentrantLock(true);

    public PaymentActor(String paymentId, PaymentEventStore eventStore) {
        this.paymentId = paymentId;
        this.eventStore = eventStore;
    }

    public Payment handle(PaymentCommand command) {
        lock.lock();
        try {
            List<DomainEvent> history = eventStore.load(paymentId);
            Payment payment = history.isEmpty() && command instanceof InitiatePaymentCommand initiate
                    ? Payment.initiate(initiate.paymentId(), initiate.merchantId(), initiate.customerId(), Money.of(initiate.amount(), initiate.currency()), initiate.paymentMethod(), initiate.orderId())
                    : Payment.rehydrate(history);

            if (!(command instanceof InitiatePaymentCommand)) {
                if (history.isEmpty()) throw new IllegalArgumentException("Payment does not exist: " + paymentId);
                switch (command) {
                    case AuthorizePaymentCommand c -> payment.authorize(c.authorizationCode());
                    case CapturePaymentCommand c -> payment.capture(c.captureReference());
                    case FailPaymentCommand c -> payment.fail(c.reason());
                    case RefundPaymentCommand c -> payment.refund(c.amount(), c.reason());
                    case CancelPaymentCommand c -> payment.cancel(c.reason());
                    case InitiatePaymentCommand ignored -> { }
                }
            } else if (!history.isEmpty()) {
                throw new IllegalStateException("Payment already exists: " + paymentId);
            }

            eventStore.append(paymentId, payment.pullEvents());
            return payment;
        } finally {
            lock.unlock();
        }
    }
}
