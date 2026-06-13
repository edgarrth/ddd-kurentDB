package com.example.payments.domain.commands;

public sealed interface PaymentCommand permits InitiatePaymentCommand, AuthorizePaymentCommand, CapturePaymentCommand, FailPaymentCommand, RefundPaymentCommand, CancelPaymentCommand {
    String paymentId();
}
