package com.example.agilepm.domain.model;

public record ProductOwnerId(String value) {
    public ProductOwnerId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("productOwnerId is required");
    }
}
