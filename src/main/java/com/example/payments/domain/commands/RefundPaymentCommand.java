package com.example.payments.domain.commands;

import java.math.BigDecimal;

public record RefundPaymentCommand(String paymentId, BigDecimal amount, String reason) implements PaymentCommand { }
