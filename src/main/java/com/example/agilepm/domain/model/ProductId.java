package com.example.agilepm.domain.model;

import java.util.UUID;

public record ProductId(String value) {
    public ProductId {
        if (value == null || value.isBlank()) value = UUID.randomUUID().toString();
    }
}
