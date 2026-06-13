package com.example.payments.domain.commands;

public record FailPaymentCommand(String paymentId, String reason) implements PaymentCommand { }
