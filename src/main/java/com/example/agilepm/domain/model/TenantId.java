package com.example.agilepm.domain.model;

public record TenantId(String value) {
    public TenantId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("tenantId is required");
    }
}
