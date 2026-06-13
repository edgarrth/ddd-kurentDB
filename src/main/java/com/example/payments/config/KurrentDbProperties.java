package com.example.payments.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kurrentdb")
public record KurrentDbProperties(String connectionString) { }
