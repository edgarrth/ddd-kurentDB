package com.example.payments.adapters.rest;

import com.example.payments.application.service.PaymentApplicationService;
import com.example.payments.domain.commands.*;
import com.example.payments.domain.events.DomainEvent;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.example.payments.adapters.rest.PaymentDtos.*;

@RestController
@RequestMapping("/payments/v1/payments")
public class PaymentController {
    private final PaymentApplicationService service;

    public PaymentController(PaymentApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse initiate(@RequestBody InitiatePaymentRequest request) {
        String paymentId = UUID.randomUUID().toString();
        var command = new InitiatePaymentCommand(paymentId, request.merchantId(), request.customerId(), request.amount(), request.currency(), request.paymentMethod(), request.orderId());
        return PaymentResponse.from(service.execute(command));
    }

    @PostMapping("/{paymentId}/authorizations")
    public PaymentResponse authorize(@PathVariable String paymentId, @RequestBody AuthorizePaymentRequest request) {
        return PaymentResponse.from(service.execute(new AuthorizePaymentCommand(paymentId, request.authorizationCode())));
    }

    @PostMapping("/{paymentId}/captures")
    public PaymentResponse capture(@PathVariable String paymentId, @RequestBody CapturePaymentRequest request) {
        return PaymentResponse.from(service.execute(new CapturePaymentCommand(paymentId, request.captureReference())));
    }

    @PostMapping("/{paymentId}/failures")
    public PaymentResponse fail(@PathVariable String paymentId, @RequestBody FailPaymentRequest request) {
        return PaymentResponse.from(service.execute(new FailPaymentCommand(paymentId, request.reason())));
    }

    @PostMapping("/{paymentId}/refunds")
    public PaymentResponse refund(@PathVariable String paymentId, @RequestBody RefundPaymentRequest request) {
        return PaymentResponse.from(service.execute(new RefundPaymentCommand(paymentId, request.amount(), request.reason())));
    }

    @PostMapping("/{paymentId}/cancellations")
    public PaymentResponse cancel(@PathVariable String paymentId, @RequestBody CancelPaymentRequest request) {
        return PaymentResponse.from(service.execute(new CancelPaymentCommand(paymentId, request.reason())));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse get(@PathVariable String paymentId) {
        return PaymentResponse.from(service.getPayment(paymentId));
    }

    @GetMapping("/{paymentId}/events")
    public List<DomainEvent> events(@PathVariable String paymentId) {
        return service.getEvents(paymentId);
    }
}
