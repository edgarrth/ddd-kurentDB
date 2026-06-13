package com.example.agilepm.application.actor;

import com.example.agilepm.application.port.in.ProductCommands;
import com.example.agilepm.application.port.out.EventStreamStore;
import com.example.agilepm.application.port.out.ProductReadModelRepository;
import com.example.agilepm.application.service.ProductView;
import com.example.agilepm.domain.event.DomainEvent;
import com.example.agilepm.domain.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletionStage;

public final class ProductActor extends Actor implements ProductCommands {
    private final EventStreamStore eventStore;
    private final ProductReadModelRepository readModels;
    private final TenantId tenantId;
    private final ProductId productId;

    public ProductActor(EventStreamStore eventStore, ProductReadModelRepository readModels,
                        TenantId tenantId, ProductId productId, int mailboxCapacity) {
        super(mailboxCapacity, "product-actor-" + productId.value());
        this.eventStore = eventStore;
        this.readModels = readModels;
        this.tenantId = tenantId;
        this.productId = productId;
    }

    @Override public CompletionStage<Void> initiate(String tenantId, String productId, String name, String description, String productOwnerId) {
        return tell(() -> persist(Product.initiate(new TenantId(tenantId), new ProductId(productId), name, description, new ProductOwnerId(productOwnerId))));
    }

    @Override public CompletionStage<Void> changeDescription(String tenantId, String productId, String description) {
        return tell(() -> { Product p = load(); p.changeDescription(description); persist(p); });
    }

    @Override public CompletionStage<Void> changeProductOwner(String tenantId, String productId, String productOwnerId) {
        return tell(() -> { Product p = load(); p.changeProductOwner(new ProductOwnerId(productOwnerId)); persist(p); });
    }

    @Override public CompletionStage<Void> requestDiscussion(String tenantId, String productId) {
        return tell(() -> { Product p = load(); p.requestDiscussion(); persist(p); });
    }

    @Override public CompletionStage<Void> attachDiscussion(String tenantId, String productId, String discussionId) {
        return tell(() -> { Product p = load(); p.attachDiscussion(new DiscussionId(discussionId)); persist(p); });
    }

    @Override public CompletionStage<Void> timeOutDiscussionRequest(String tenantId, String productId) {
        return tell(() -> { Product p = load(); p.timeOutDiscussionRequest(); persist(p); });
    }

    @Override public CompletionStage<Void> planSprint(String tenantId, String productId, String sprintId, String name, LocalDate startsOn, LocalDate endsOn) {
        return tell(() -> { Product p = load(); p.planSprint(new SprintId(sprintId), name, startsOn, endsOn); persist(p); });
    }

    @Override public CompletionStage<Void> scheduleRelease(String tenantId, String productId, String releaseId, String name, LocalDate scheduledFor) {
        return tell(() -> { Product p = load(); p.scheduleRelease(new ReleaseId(releaseId), name, scheduledFor); persist(p); });
    }

    private Product load() {
        return Product.reconstitute(eventStore.read(Product.streamNameFor(tenantId, productId)));
    }

    private void persist(Product product) {
        List<DomainEvent> changes = product.pullUncommittedEvents();
        if (changes.isEmpty()) return;
        eventStore.append(Product.streamNameFor(tenantId, productId), product.version() - changes.size(), changes);
        readModels.upsert(new ProductView(product.tenantId().value(), product.productId().value(), product.name(), product.description(),
                product.productOwnerId().value(), product.discussionId().map(DiscussionId::value).orElse(null),
                product.sprintIds(), product.releaseIds(), product.version()));
    }
}
