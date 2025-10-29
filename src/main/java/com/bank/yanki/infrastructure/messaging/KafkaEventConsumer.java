package com.bank.yanki.infrastructure.messaging;

import com.bank.yanki.application.event.TransactionCreatedEvent;
import com.bank.yanki.application.event.YankiBalanceValidationEvent;
import com.bank.yanki.application.event.YankiBalanceValidationResponse;
import com.bank.yanki.application.event.YankiPaymentRequestEvent;
import com.bank.yanki.domain.service.YankiBalanceValidationService;
import com.bank.yanki.domain.service.YankiPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor principal de Kafka para el servicio Yanki.
 * Este componente escucha múltiples tópicos relacionados con transacciones,
 * actualizaciones de wallet, validaciones de saldo y pagos Yanki.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

  private final ObjectMapper objectMapper;
  private final YankiBalanceValidationService yankiBalanceValidationService;
  private final YankiPaymentService yankiPaymentService;

  /**
   * Consume eventos de transacción creada.
   * Este método procesa eventos que notifican la creación de nuevas transacciones
   * en el sistema. Principalmente para propósitos de notificación y dashboard.
   *
   * @param event El evento de transacción creada
   */
  @KafkaListener(
    topics = "${app.kafka.topics.transaction-created:yanki.transactions.created}",
    groupId = "${spring.kafka.consumer.group-id:yanki-service}"
  )
  public void consumeTransactionCreated(TransactionCreatedEvent event) {
    log.info("Received transaction created event: {}", event.getTransactionId());
    // Aquí podrías procesar notificaciones, actualizar dashboards, etc.
    // Pero NO llamar a otros microservicios via REST
  }

  /**
   * Consume eventos de wallet actualizado.
   * Este método procesa eventos que notifican actualizaciones en los wallets,
   * permitiendo invalidar cachés si es necesario.
   *
   * @param walletId El identificador del wallet que fue actualizado
   */
  @KafkaListener(
    topics = "${app.kafka.topics.wallet-updated:yanki.wallet.updated}",
    groupId = "${spring.kafka.consumer.group-id:yanki-service}"
  )
  public void consumeWalletUpdated(String walletId) {
    log.info("Received wallet updated event: {}", walletId);
    // Invalidar cache si es necesario
  }

  /**
   * Consume y procesa solicitudes de validación de saldo Yanki.
   * Este método deserializa manualmente el mensaje JSON y delega la validación
   * al servicio correspondiente.
   *
   * @param message El mensaje JSON recibido de Kafka como String
   */
  @KafkaListener(
    topics = "${app.kafka.topics.yanki-balance-validation-request:yanki-balance-validation-request}",
    groupId = "${spring.kafka.consumer.group-id:yanki-service}"
  )
  public void consumeYankiBalanceValidationRequest(String message) { // ← Cambiar a String
    try {
      log.info("📨 Received Yanki balance validation request raw: {}", message);

      // Deserializar manualmente
      YankiBalanceValidationEvent event =
        objectMapper.readValue(message, YankiBalanceValidationEvent.class);

      log.info("🎯 Parsed Yanki balance validation - ValidationId: {}, Phone: {}, Amount: {}",
        event.getValidationId(), event.getPhoneNumber(), event.getRequiredAmount());

      yankiBalanceValidationService.processBalanceValidation(event)
        .subscribe(
          success -> log.info("✅ Yanki balance validation processed - ValidationId: {}",
            event.getValidationId()),
          error -> log.error("❌ Error processing Yanki balance validation: {}", error.getMessage())
        );
    } catch (Exception e) {
      log.error("❌ Error processing Yanki balance validation request: {}", e.getMessage(), e);
    }
  }

  /**
   * Consume y procesa respuestas de validación de saldo Yanki.
   * Este método procesa las respuestas de validación de saldo que indican
   * si un wallet tiene fondos suficientes para una operación.
   *
   * @param message El mensaje JSON recibido de Kafka como String
   */
  @KafkaListener(
    topics = "${app.kafka.topics.yanki-balance-validation-response:yanki-balance-validation-response}",
    groupId = "${spring.kafka.consumer.group-id:yanki-service}"
  )
  public void consumeYankiBalanceValidationResponse(String message) { // ← Cambiar a String
    try {
      log.info("📨 Received Yanki balance validation response raw: {}", message);

      // Deserializar manualmente
      YankiBalanceValidationResponse response =
        objectMapper.readValue(message, YankiBalanceValidationResponse.class);

      log.info("🎯 Parsed Yanki balance validation response - ValidationId: {}, Sufficient: {}",
        response.getValidationId(), response.getSufficientBalance());

      yankiBalanceValidationService.processBalanceValidationResponse(response);
    } catch (Exception e) {
      log.error("❌ Error processing Yanki balance validation response: {}", e.getMessage(), e);
    }
  }

  /**
   * Consume y procesa solicitudes de pago Yanki.
   * Este método deserializa y procesa solicitudes de transferencia entre wallets Yanki.
   *
   * @param message El mensaje JSON recibido de Kafka como String
   */
  @KafkaListener(
    topics = "${app.kafka.topics.yanki-payment-request:yanki-payment-request}",
    groupId = "${spring.kafka.consumer.group-id:yanki-service}"
  )
  public void consumeYankiPaymentRequest(String message) {
    try {
      log.info("📨 Received Yanki payment request raw: {}", message);

      YankiPaymentRequestEvent event =
        objectMapper.readValue(message, YankiPaymentRequestEvent.class);

      log.info("🎯 Parsed Yanki payment request - PaymentId: {}, From: {}, To: {}, Amount: {}",
        event.getPaymentId(), event.getFromPhoneNumber(), event.getToPhoneNumber(),
        event.getAmount());

      // Procesar el pago Yanki
      yankiPaymentService.processYankiPayment(event)
        .subscribe(
          success -> log.info("✅ Yanki payment processed - PaymentId: {}", event.getPaymentId()),
          error -> log.error("❌ Error processing Yanki payment: {}", error.getMessage())
        );

    } catch (Exception e) {
      log.error("❌ Error processing Yanki payment request: {}", e.getMessage(), e);
    }
  }
}