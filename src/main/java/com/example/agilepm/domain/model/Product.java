package com.example.agilepm.domain.model;

import com.example.agilepm.domain.event.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

public final class Product {
    private TenantId tenantId;
    private ProductId productId;
    private String name;
    private String description;
    private ProductOwnerId productOwnerId;
    private boolean discussionRequested;
    private DiscussionId discussionId;
    private final Set<String> sprintIds = new HashSet<>();
    private final Set<String> releaseIds = new HashSet<>();
    private long version = -1;
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    private Product() {}

    public static String streamNameFor(TenantId tenantId, ProductId productId) {
        return "agilepm-product-%s-%s".formatted(tenantId.value(), productId.value());
    }

    public static Product initiate(TenantId tenantId, ProductId productId, String name, String description, ProductOwnerId ownerId) {
        Product product = new Product();
        product.raise(new ProductInitiated(tenantId.value(), productId.value(), requireText(name, "name"),
                requireText(description, "description"), ownerId.value(), Instant.now()));
        return product;
    }

    public static Product reconstitute(List<DomainEvent> history) {
        Product product = new Product();
        for (DomainEvent event : history) {
            product.apply(event);
            product.version++;
        }
        return product;
    }

    public void changeDescription(String description) {
        ensureInitiated();
        if (Objects.equals(this.description, description)) return;
        raise(new ProductDescriptionChanged(tenantId.value(), productId.value(), requireText(description, "description"), Instant.now()));
    }

    public void changeProductOwner(ProductOwnerId ownerId) {
        ensureInitiated();
        if (Objects.equals(this.productOwnerId, ownerId)) return;
        raise(new ProductOwnerChanged(tenantId.value(), productId.value(), ownerId.value(), Instant.now()));
    }

    public void requestDiscussion() {
        ensureInitiated();
        if (discussionRequested || discussionId != null) return;
        raise(new ProductDiscussionRequested(tenantId.value(), productId.value(), Instant.now()));
    }

    public void attachDiscussion(DiscussionId discussionId) {
        ensureInitiated();
        if (!discussionRequested) throw new IllegalStateException("Discussion was not requested");
        raise(new ProductDiscussionAttached(tenantId.value(), productId.value(), discussionId.value(), Instant.now()));
    }

    public void timeOutDiscussionRequest() {
        ensureInitiated();
        if (!discussionRequested || discussionId != null) return;
        raise(new ProductDiscussionRequestTimedOut(tenantId.value(), productId.value(), Instant.now()));
    }

    public void planSprint(SprintId sprintId, String name, LocalDate startsOn, LocalDate endsOn) {
        ensureInitiated();
        if (!startsOn.isBefore(endsOn)) throw new IllegalArgumentException("Sprint start date must be before end date");
        if (sprintIds.contains(sprintId.value())) return;
        raise(new SprintPlanned(tenantId.value(), productId.value(), sprintId.value(), requireText(name, "sprint name"), startsOn, endsOn, Instant.now()));
    }

    public void scheduleRelease(ReleaseId releaseId, String name, LocalDate scheduledFor) {
        ensureInitiated();
        if (releaseIds.contains(releaseId.value())) return;
        raise(new ReleaseScheduled(tenantId.value(), productId.value(), releaseId.value(), requireText(name, "release name"), scheduledFor, Instant.now()));
    }

    private void raise(DomainEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    public void apply(DomainEvent event) {
        switch (event) {
            case ProductInitiated e -> {
                this.tenantId = new TenantId(e.tenantId());
                this.productId = new ProductId(e.productId());
                this.name = e.name();
                this.description = e.description();
                this.productOwnerId = new ProductOwnerId(e.productOwnerId());
            }
            case ProductDescriptionChanged e -> this.description = e.description();
            case ProductOwnerChanged e -> this.productOwnerId = new ProductOwnerId(e.productOwnerId());
            case ProductDiscussionRequested ignored -> this.discussionRequested = true;
            case ProductDiscussionAttached e -> {
                this.discussionRequested = false;
                this.discussionId = new DiscussionId(e.discussionId());
            }
            case ProductDiscussionRequestTimedOut ignored -> this.discussionRequested = false;
            case SprintPlanned e -> this.sprintIds.add(e.sprintId());
            case ReleaseScheduled e -> this.releaseIds.add(e.releaseId());
        }
    }

    public List<DomainEvent> pullUncommittedEvents() {
        List<DomainEvent> copy = List.copyOf(uncommittedEvents);
        version += copy.size();
        uncommittedEvents.clear();
        return copy;
    }

    public long version() { return version; }
    public ProductId productId() { return productId; }
    public TenantId tenantId() { return tenantId; }
    public String name() { return name; }
    public String description() { return description; }
    public ProductOwnerId productOwnerId() { return productOwnerId; }
    public Optional<DiscussionId> discussionId() { return Optional.ofNullable(discussionId); }
    public Set<String> sprintIds() { return Set.copyOf(sprintIds); }
    public Set<String> releaseIds() { return Set.copyOf(releaseIds); }

    private void ensureInitiated() {
        if (productId == null) throw new IllegalStateException("Product must be initiated first");
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
