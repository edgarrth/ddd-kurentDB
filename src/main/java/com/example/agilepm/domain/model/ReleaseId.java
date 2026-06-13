package com.example.agilepm.domain.model;

public record ReleaseId(String value) {
    public ReleaseId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("releaseId is required");
    }
}
