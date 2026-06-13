package com.example.agilepm.application.port.out;

import com.example.agilepm.domain.event.DomainEvent;
import java.util.List;

public interface EventStreamStore {
    List<DomainEvent> read(String streamName);
    void append(String streamName, long expectedVersion, List<DomainEvent> events);
}
