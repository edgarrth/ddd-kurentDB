package com.example.payments.domain.commands;

public record CancelPaymentCommand(String paymentId, String reason) implements PaymentCommand { }
