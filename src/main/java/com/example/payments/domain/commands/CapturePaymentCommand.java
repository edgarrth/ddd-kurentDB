package com.example.payments.domain.commands;

public record CapturePaymentCommand(String paymentId, String captureReference) implements PaymentCommand { }
