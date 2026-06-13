package com.example.payments.application.service;

import com.example.payments.application.actors.PaymentActorSystem;
import com.example.payments.domain.commands.PaymentCommand;
import com.example.payments.domain.events.DomainEvent;
import com.example.payments.domain.model.Payment;
import com.example.payments.domain.ports.PaymentEventStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentApplicationService {
    private final PaymentActorSystem actorSystem;
    private final PaymentEventStore eventStore;

    public PaymentApplicationService(PaymentActorSystem actorSystem, PaymentEventStore eventStore) {
        this.actorSystem = actorSystem;
        this.eventStore = eventStore;
    }

    public Payment execute(PaymentCommand command) {
        return actorSystem.actorFor(command.paymentId()).handle(command);
    }

    public Payment getPayment(String paymentId) {
        List<DomainEvent> history = eventStore.load(paymentId);
        if (history.isEmpty()) throw new IllegalArgumentException("Payment does not exist: " + paymentId);
        return Payment.rehydrate(history);
    }

    public List<DomainEvent> getEvents(String paymentId) {
        return eventStore.load(paymentId);
    }
}
