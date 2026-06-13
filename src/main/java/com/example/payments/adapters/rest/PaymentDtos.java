package com.example.payments.adapters.rest;

import com.example.payments.domain.model.Payment;

import java.math.BigDecimal;

public final class PaymentDtos {
    private PaymentDtos() { }

    public record InitiatePaymentRequest(String merchantId, String customerId, BigDecimal amount, String currency, String paymentMethod, String orderId) { }
    public record AuthorizePaymentRequest(String authorizationCode) { }
    public record CapturePaymentRequest(String captureReference) { }
    public record FailPaymentRequest(String reason) { }
    public record RefundPaymentRequest(BigDecimal amount, String reason) { }
    public record CancelPaymentRequest(String reason) { }

    public record PaymentResponse(
            String paymentId,
            String merchantId,
            String customerId,
            BigDecimal amount,
            String currency,
            String paymentMethod,
            String orderId,
            String status,
            String authorizationCode,
            String captureReference
    ) {
        static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.paymentId(),
                    payment.merchantId(),
                    payment.customerId(),
                    payment.money().amount(),
                    payment.money().currency().getCurrencyCode(),
                    payment.paymentMethod(),
                    payment.orderId(),
                    payment.status().name(),
                    payment.authorizationCode(),
                    payment.captureReference()
            );
        }
    }
}
