package com.example.agilepm.application.service;

import com.example.agilepm.application.actor.ProductActor;
import com.example.agilepm.application.port.in.ProductCommands;
import com.example.agilepm.application.port.out.EventStreamStore;
import com.example.agilepm.application.port.out.ProductReadModelRepository;
import com.example.agilepm.domain.model.ProductId;
import com.example.agilepm.domain.model.TenantId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductActorRegistry {
    private final EventStreamStore eventStore;
    private final ProductReadModelRepository readModels;
    private final int mailboxCapacity;
    private final ConcurrentHashMap<String, ProductActor> actors = new ConcurrentHashMap<>();

    public ProductActorRegistry(EventStreamStore eventStore, ProductReadModelRepository readModels,
                                @Value("${agilepm.actor.mailbox-capacity:512}") int mailboxCapacity) {
        this.eventStore = eventStore;
        this.readModels = readModels;
        this.mailboxCapacity = mailboxCapacity;
    }

    public ProductCommands actorFor(String tenantId, String productId) {
        String key = tenantId + ":" + productId;
        return actors.computeIfAbsent(key, ignored -> new ProductActor(eventStore, readModels,
                new TenantId(tenantId), new ProductId(productId), mailboxCapacity));
    }
}
