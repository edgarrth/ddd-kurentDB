package com.example.agilepm.domain.model;

public record DiscussionId(String value) {
    public DiscussionId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("discussionId is required");
    }
}
