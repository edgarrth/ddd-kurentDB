package com.example.agilepm.adapter.in.rest;

import com.example.agilepm.application.port.in.ProductCommands;
import com.example.agilepm.application.port.in.ProductQueries;
import com.example.agilepm.application.service.ProductActorRegistry;
import com.example.agilepm.application.service.ProductView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/agilepm/v1/tenants/{tenantId}/products")
public class ProductController {
    private final ProductActorRegistry actors;
    private final ProductQueries queries;

    public ProductController(ProductActorRegistry actors, ProductQueries queries) {
        this.actors = actors;
        this.queries = queries;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> initiate(@PathVariable String tenantId, @Valid @RequestBody InitiateProductRequest request) {
        String productId = request.productId();
        ProductCommands actor = actors.actorFor(tenantId, productId);
        actor.initiate(tenantId, productId, request.name(), request.description(), request.productOwnerId()).toCompletableFuture().join();
        return ResponseEntity.created(URI.create("/agilepm/v1/tenants/%s/products/%s".formatted(tenantId, productId)))
                .body(Map.of("tenantId", tenantId, "productId", productId, "status", "PRODUCT_INITIATED"));
    }

    @PatchMapping("/{productId}/description")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> changeDescription(@PathVariable String tenantId, @PathVariable String productId,
                                                 @Valid @RequestBody ChangeDescriptionRequest request) {
        actors.actorFor(tenantId, productId).changeDescription(tenantId, productId, request.description()).toCompletableFuture().join();
        return accepted(productId, "PRODUCT_DESCRIPTION_CHANGED");
    }

    @PatchMapping("/{productId}/owner")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> changeOwner(@PathVariable String tenantId, @PathVariable String productId,
                                           @Valid @RequestBody ChangeOwnerRequest request) {
        actors.actorFor(tenantId, productId).changeProductOwner(tenantId, productId, request.productOwnerId()).toCompletableFuture().join();
        return accepted(productId, "PRODUCT_OWNER_CHANGED");
    }

    @PostMapping("/{productId}/discussion-requests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> requestDiscussion(@PathVariable String tenantId, @PathVariable String productId) {
        actors.actorFor(tenantId, productId).requestDiscussion(tenantId, productId).toCompletableFuture().join();
        return accepted(productId, "PRODUCT_DISCUSSION_REQUESTED");
    }

    @PutMapping("/{productId}/discussion")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> attachDiscussion(@PathVariable String tenantId, @PathVariable String productId,
                                                @Valid @RequestBody AttachDiscussionRequest request) {
        actors.actorFor(tenantId, productId).attachDiscussion(tenantId, productId, request.discussionId()).toCompletableFuture().join();
        return accepted(productId, "PRODUCT_DISCUSSION_ATTACHED");
    }

    @PostMapping("/{productId}/discussion-request-timeouts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> timeOutDiscussionRequest(@PathVariable String tenantId, @PathVariable String productId) {
        actors.actorFor(tenantId, productId).timeOutDiscussionRequest(tenantId, productId).toCompletableFuture().join();
        return accepted(productId, "PRODUCT_DISCUSSION_REQUEST_TIMED_OUT");
    }

    @PostMapping("/{productId}/sprints")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> planSprint(@PathVariable String tenantId, @PathVariable String productId,
                                          @Valid @RequestBody PlanSprintRequest request) {
        actors.actorFor(tenantId, productId).planSprint(tenantId, productId, request.sprintId(), request.name(), request.startsOn(), request.endsOn()).toCompletableFuture().join();
        return accepted(productId, "SPRINT_PLANNED");
    }

    @PostMapping("/{productId}/releases")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> scheduleRelease(@PathVariable String tenantId, @PathVariable String productId,
                                               @Valid @RequestBody ScheduleReleaseRequest request) {
        actors.actorFor(tenantId, productId).scheduleRelease(tenantId, productId, request.releaseId(), request.name(), request.scheduledFor()).toCompletableFuture().join();
        return accepted(productId, "RELEASE_SCHEDULED");
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductView> productOf(@PathVariable String tenantId, @PathVariable String productId) {
        return queries.productOf(tenantId, productId).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static Map<String, String> accepted(String productId, String status) { return Map.of("productId", productId, "status", status); }

    public record InitiateProductRequest(@NotBlank String productId, @NotBlank String name, @NotBlank String description, @NotBlank String productOwnerId) {}
    public record ChangeDescriptionRequest(@NotBlank String description) {}
    public record ChangeOwnerRequest(@NotBlank String productOwnerId) {}
    public record AttachDiscussionRequest(@NotBlank String discussionId) {}
    public record PlanSprintRequest(@NotBlank String sprintId, @NotBlank String name, @NotNull LocalDate startsOn, @NotNull LocalDate endsOn) {}
    public record ScheduleReleaseRequest(@NotBlank String releaseId, @NotBlank String name, @NotNull LocalDate scheduledFor) {}
}
