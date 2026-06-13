package com.example.payments.domain.ports;

import com.example.payments.domain.events.DomainEvent;

import java.util.List;

public interface PaymentEventStore {
    List<DomainEvent> load(String paymentId);
    void append(String paymentId, List<DomainEvent> events);
}
