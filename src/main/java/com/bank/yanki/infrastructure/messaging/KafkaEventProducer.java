package com.bank.yanki.infrastructure.messaging;

import com.bank.yanki.application.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

    public Mono<Void> sendWalletCreatedEvent(WalletCreatedEvent event) {
        return kafkaTemplate.send("yanki.wallet.created", event.getWalletId(), event)
                .doOnSuccess(result ->
                        log.info("Wallet created event sent: {}", event.getWalletId())
                )
                .doOnError(error ->
                        log.error("Failed to send wallet created event: {}", error.getMessage())
                )
                .then();
    }

    public Mono<Void> sendCardAssociatedEvent(CardAssociatedEvent event) {
        return kafkaTemplate.send("yanki.card.associated", event.getWalletId(), event)
                .doOnSuccess(result ->
                        log.info("Card associated event sent: {}", event.getWalletId())
                )
                .doOnError(error ->
                        log.error("Failed to send card association event: {}", error.getMessage())
                )
                .then();
    }

    public Mono<Void> sendCreditBalanceInquiry(CreditBalanceInquiryEvent event) {
        return kafkaTemplate.send("yanki.credit.balance.inquiry", event.getInquiryId(), event)
                .doOnSuccess(result ->
                        log.info("Credit balance inquiry sent - InquiryId: {}, CreditId: {}",
                                event.getInquiryId(), event.getCreditId())
                )
                .doOnError(error ->
                        log.error("Failed to send credit balance inquiry: {}", error.getMessage())
                )
                .then();
    }

    public Mono<Void> sendCreditPaymentRequest(CreditPaymentRequestEvent event) {
        return kafkaTemplate.send("yanki.credit.payment.request", event.getPaymentId(), event)
                .doOnSuccess(result ->
                        log.info("Credit payment request sent: {}", event.getPaymentId())
                )
                .doOnError(error ->
                        log.error("Failed to send payment request: {}", error.getMessage())
                )
                .then();
    }

    public Mono<Void> sendTransactionPaymentRequest(TransactionPaymentRequestEvent event) {
        return kafkaTemplate.send("transaction.payment.request", event.getPaymentId(), event)
                .doOnSuccess(result ->
                        log.info("Transaction payment request sent - PaymentId: {}, CreditId: {}",
                                event.getPaymentId(), event.getCreditId())
                )
                .doOnError(error ->
                        log.error("Failed to send transaction payment request: {}", error.getMessage())
                )
                .then();
    }

    public Mono<Void> sendTransactionConsumptionRequest(TransactionConsumptionRequestEvent event) {
        return kafkaTemplate.send("transaction.consumption.request", event.getConsumptionId(), event)
                .doOnSuccess(result ->
                        log.info("Transaction consumption request sent - ConsumptionId: {}, CreditId: {}",
                                event.getConsumptionId(), event.getCreditId())
                )
                .doOnError(error ->
                        log.error("Failed to send transaction consumption request: {}", error.getMessage())
                )
                .then();
    }
}