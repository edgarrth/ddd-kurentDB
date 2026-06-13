package com.example.agilepm.application.service;

import java.util.Set;

public record ProductView(String tenantId, String productId, String name, String description,
                          String productOwnerId, String discussionId,
                          Set<String> sprintIds, Set<String> releaseIds, long version) {}
