package com.example.agilepm.adapter.out.kurrentdb;

import com.example.agilepm.application.port.out.EventStreamStore;
import com.example.agilepm.domain.event.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kurrent.dbclient.*;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class KurrentEventStreamStore implements EventStreamStore {
    private final KurrentDBClient client;
    private final ObjectMapper objectMapper;

    public KurrentEventStreamStore(KurrentDBClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<DomainEvent> read(String streamName) {
        try {
            ReadStreamOptions options = ReadStreamOptions.get().forwards().fromStart();
            ReadResult result = client.readStream(streamName, options).get();
            return result.getEvents().stream()
                    .map(e -> deserialize(e.getOriginalEvent()))
                    .toList();
        } catch (Exception e) {
            if (isStreamNotFound(e)) return List.of();
            throw new IllegalStateException("Cannot read stream " + streamName, e);
        }
    }

    @Override
    public void append(String streamName, long expectedVersion, List<DomainEvent> events) {
        if (events.isEmpty()) return;
        try {
            AppendToStreamOptions options = expectedVersion < 0
                    ? AppendToStreamOptions.get().streamState(StreamState.noStream())
                    : AppendToStreamOptions.get().streamRevision(expectedVersion);
            List<EventData> eventData = events.stream().map(this::serialize).toList();
            client.appendToStream(streamName, options, eventData.iterator()).get();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot append to stream " + streamName, e);
        }
    }

    private EventData serialize(DomainEvent event) {
        try {
            return EventData.builderAsJson(UUID.randomUUID(), event.eventType(), event).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot serialize event " + event.eventType(), e);
        }
    }

    private DomainEvent deserialize(RecordedEvent event) {
        try {
            String type = event.getEventType();
            byte[] data = event.getEventData();
            return switch (type) {
                case "ProductInitiated" -> objectMapper.readValue(data, ProductInitiated.class);
                case "ProductDescriptionChanged" -> objectMapper.readValue(data, ProductDescriptionChanged.class);
                case "ProductOwnerChanged" -> objectMapper.readValue(data, ProductOwnerChanged.class);
                case "ProductDiscussionRequested" -> objectMapper.readValue(data, ProductDiscussionRequested.class);
                case "ProductDiscussionAttached" -> objectMapper.readValue(data, ProductDiscussionAttached.class);
                case "ProductDiscussionRequestTimedOut" -> objectMapper.readValue(data, ProductDiscussionRequestTimedOut.class);
                case "SprintPlanned" -> objectMapper.readValue(data, SprintPlanned.class);
                case "ReleaseScheduled" -> objectMapper.readValue(data, ReleaseScheduled.class);
                default -> throw new IllegalStateException("Unknown event type: " + type + " payload=" + objectMapper.readValue(data, JsonNode.class));
            };
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot deserialize KurrentDB event", e);
        }
    }

    private boolean isStreamNotFound(Exception e) {
        Throwable current = e;
        while (current != null) {
            if (current.getClass().getSimpleName().contains("StreamNotFound")) return true;
            current = current.getCause();
        }
        return false;
    }
}
