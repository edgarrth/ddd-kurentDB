package com.example.payments.application.actors;

import com.example.payments.domain.ports.PaymentEventStore;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentActorSystem {
    private final PaymentEventStore eventStore;
    private final ConcurrentHashMap<String, PaymentActor> actors = new ConcurrentHashMap<>();

    public PaymentActorSystem(PaymentEventStore eventStore) {
        this.eventStore = eventStore;
    }

    public PaymentActor actorFor(String paymentId) {
        return actors.computeIfAbsent(paymentId, id -> new PaymentActor(id, eventStore));
    }
}
