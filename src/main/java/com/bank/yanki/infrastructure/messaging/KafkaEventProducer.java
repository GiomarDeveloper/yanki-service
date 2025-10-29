package com.bank.yanki.infrastructure.messaging;

import com.bank.yanki.application.event.CardAssociatedEvent;
import com.bank.yanki.application.event.CreditBalanceInquiryEvent;
import com.bank.yanki.application.event.CreditPaymentRequestEvent;
import com.bank.yanki.application.event.TransactionConsumptionRequestEvent;
import com.bank.yanki.application.event.TransactionPaymentRequestEvent;
import com.bank.yanki.application.event.WalletCreatedEvent;
import com.bank.yanki.application.event.YankiBalanceValidationEvent;
import com.bank.yanki.application.event.YankiBalanceValidationResponse;
import com.bank.yanki.application.event.YankiPaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Productor de eventos Kafka para el servicio Yanki.
 * Este componente proporciona métodos para publicar diferentes tipos de eventos
 * en los tópicos correspondientes de Kafka.
 *
 * @author Bank Application Team
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {

  private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;

  /**
   * Publica un evento de wallet creado.
   *
   * @param event El evento de wallet creado
   * @return Mono<Void> que completa cuando el evento es enviado
   */
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

  /**
   * Publica un evento de tarjeta asociada.
   *
   * @param event El evento de tarjeta asociada a un wallet
   * @return Mono<Void> que completa cuando el evento es enviado
   */
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

  /**
   * Publica una consulta de saldo de crédito.
   *
   * @param event El evento de consulta de saldo de crédito
   * @return Mono<Void> que completa cuando el evento es enviado
   */
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

  /**
   * Publica una solicitud de pago con crédito.
   *
   * @param event El evento de solicitud de pago con crédito
   * @return Mono<Void> que completa cuando el evento es enviado
   */
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

  /**
   * Publica una solicitud de pago de transacción.
   *
   * @param event El evento de solicitud de pago de transacción
   * @return Mono<Void> que completa cuando el evento es enviado
   */
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

  /**
   * Publica una solicitud de consumo de transacción.
   *
   * @param event El evento de solicitud de consumo de transacción
   * @return Mono<Void> que completa cuando el evento es enviado
   */
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

  /**
   * Publica una solicitud de validación de saldo Yanki.
   *
   * @param event El evento de validación de saldo Yanki
   * @return Mono<Void> que completa cuando el evento es enviado
   */
  public Mono<Void> sendYankiBalanceValidation(YankiBalanceValidationEvent event) {
    return kafkaTemplate.send("yanki-balance-validation-request", event.getValidationId(), event)
      .doOnSuccess(result ->
        log.info("✅ Yanki balance validation sent - ValidationId: {}, Phone: {}, Amount: {}",
          event.getValidationId(), event.getPhoneNumber(), event.getRequiredAmount())
      )
      .doOnError(error ->
        log.error("❌ Failed to send Yanki balance validation: {}", error.getMessage())
      )
      .then();
  }

  /**
   * Publica una respuesta de validación de saldo Yanki.
   *
   * @param response La respuesta de validación de saldo Yanki
   * @return Mono<Void> que completa cuando el evento es enviado
   */
  public Mono<Void> sendYankiBalanceValidationResponse(YankiBalanceValidationResponse response) {
    return kafkaTemplate.send("yanki-balance-validation-response", response.getValidationId(),
        response)
      .doOnSuccess(result ->
        log.info("✅ Yanki balance validation response sent - ValidationId: {}, Sufficient: {}",
          response.getValidationId(), response.getSufficientBalance())
      )
      .doOnError(error ->
        log.error("❌ Failed to send Yanki balance validation response: {}", error.getMessage())
      )
      .then();
  }

  /**
   * Publica una respuesta de pago Yanki completado.
   *
   * @param response El evento de pago Yanki completado
   * @return Mono<Void> que completa cuando el evento es enviado
   */
  public Mono<Void> sendYankiPaymentResponse(YankiPaymentCompletedEvent response) {
    return kafkaTemplate.send("yanki-payment-completed", response.getPaymentId(), response)
      .doOnSuccess(result ->
        log.info("✅ Yanki payment response sent - PaymentId: {}, Success: {}",
          response.getPaymentId(), response.isSuccess()))
      .doOnError(error ->
        log.error("❌ Failed to send Yanki payment response: {}", error.getMessage()))
      .then();
  }
}