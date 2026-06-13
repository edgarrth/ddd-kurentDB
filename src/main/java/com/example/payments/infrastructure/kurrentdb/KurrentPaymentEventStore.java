package com.example.payments.infrastructure.kurrentdb;

import com.example.payments.domain.events.*;
import com.example.payments.domain.ports.PaymentEventStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kurrent.dbclient.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class KurrentPaymentEventStore implements PaymentEventStore {
    private final KurrentDBClient client;
    private final ObjectMapper objectMapper;

    public KurrentPaymentEventStore(KurrentDBClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<DomainEvent> load(String paymentId) {
        try {
            ReadStreamOptions options = ReadStreamOptions.get().forwards().fromStart();
            ReadResult result = client.readStream(streamName(paymentId), options).get();
            List<DomainEvent> events = new ArrayList<>();
            for (ResolvedEvent resolved : result.getEvents()) {
                RecordedEvent recorded = resolved.getOriginalEvent();
                events.add(deserialize(recorded.getEventType(), recorded.getEventData()));
            }
            return events;
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("not found")) return List.of();
            return List.of();
        }
    }

    @Override
    public void append(String paymentId, List<DomainEvent> events) {
        if (events == null || events.isEmpty()) return;
        try {
            List<EventData> eventData = events.stream()
                    .map(this::serialize)
                    .toList();
            client.appendToStream(streamName(paymentId), eventData.iterator()).get();
        } catch (Exception ex) {
            throw new RuntimeException("Could not append events to KurrentDB", ex);
        }
    }

    private EventData serialize(DomainEvent event) {
        try {
            return EventData.builderAsJson(event.eventType(), objectMapper.writeValueAsBytes(event))
                    .eventId(UUID.randomUUID())
                    .build();
        } catch (Exception ex) {
            throw new RuntimeException("Could not serialize event " + event.eventType(), ex);
        }
    }

    private DomainEvent deserialize(String eventType, byte[] data) {
        try {
            return switch (eventType) {
                case "PaymentInitiated" -> objectMapper.readValue(data, PaymentInitiated.class);
                case "PaymentAuthorized" -> objectMapper.readValue(data, PaymentAuthorized.class);
                case "PaymentCaptured" -> objectMapper.readValue(data, PaymentCaptured.class);
                case "PaymentFailed" -> objectMapper.readValue(data, PaymentFailed.class);
                case "PaymentRefunded" -> objectMapper.readValue(data, PaymentRefunded.class);
                case "PaymentCancelled" -> objectMapper.readValue(data, PaymentCancelled.class);
                default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
            };
        } catch (Exception ex) {
            throw new RuntimeException("Could not deserialize event " + eventType, ex);
        }
    }

    private String streamName(String paymentId) {
        return "payment-" + paymentId;
    }
}
