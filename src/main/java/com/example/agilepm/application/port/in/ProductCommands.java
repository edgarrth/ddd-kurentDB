package com.example.agilepm.application.port.in;

import java.time.LocalDate;
import java.util.concurrent.CompletionStage;

public interface ProductCommands {
    CompletionStage<Void> initiate(String tenantId, String productId, String name, String description, String productOwnerId);
    CompletionStage<Void> changeDescription(String tenantId, String productId, String description);
    CompletionStage<Void> changeProductOwner(String tenantId, String productId, String productOwnerId);
    CompletionStage<Void> requestDiscussion(String tenantId, String productId);
    CompletionStage<Void> attachDiscussion(String tenantId, String productId, String discussionId);
    CompletionStage<Void> timeOutDiscussionRequest(String tenantId, String productId);
    CompletionStage<Void> planSprint(String tenantId, String productId, String sprintId, String name, LocalDate startsOn, LocalDate endsOn);
    CompletionStage<Void> scheduleRelease(String tenantId, String productId, String releaseId, String name, LocalDate scheduledFor);
}
