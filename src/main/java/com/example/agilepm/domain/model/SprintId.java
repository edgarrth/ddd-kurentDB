package com.example.agilepm.domain.model;

public record SprintId(String value) {
    public SprintId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("sprintId is required");
    }
}
