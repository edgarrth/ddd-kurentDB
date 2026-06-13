package com.example.payments.domain.commands;

import java.math.BigDecimal;

public record InitiatePaymentCommand(String paymentId, String merchantId, String customerId, BigDecimal amount, String currency, String paymentMethod, String orderId) implements PaymentCommand { }
