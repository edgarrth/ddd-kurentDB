package com.example.payments.domain.commands;

public record AuthorizePaymentCommand(String paymentId, String authorizationCode) implements PaymentCommand { }
