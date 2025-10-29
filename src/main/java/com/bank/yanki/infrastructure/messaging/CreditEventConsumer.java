package com.bank.yanki.infrastructure.messaging;

import com.bank.yanki.application.event.CreditBalanceResponseEvent;
import com.bank.yanki.application.event.CreditPaymentResponseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor de Kafka para procesar eventos relacionados con créditos.
 * Este componente escucha múltiples tópicos relacionados con operaciones
 * de crédito como validación de saldo y procesamiento de pagos.
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditEventConsumer {

  /**
   * Consume y procesa respuestas de validación de saldo de crédito.
   * Este método procesa eventos que indican si un crédito tiene saldo suficiente
   * para realizar una operación.
   *
   * @param event El evento de respuesta de validación de saldo
   */
  @KafkaListener(
    topics = "credit.balance.response",
    groupId = "${spring.kafka.consumer.group-id:yanki-service}"
  )
  public void consumeCreditBalanceResponse(CreditBalanceResponseEvent event) {
    log.info("Received credit balance response: {}", event.getInquiryId());

    // Aquí procesas la respuesta del saldo
    // Podrías actualizar cache, notificar al usuario, etc.
    if (event.getIsValid()) {
      log.info("Credit {} has balance: {}", event.getCreditId(), event.getCurrentBalance());
    } else {
      log.warn("Credit {} is invalid", event.getCreditId());
    }
  }

  /**
   * Consume y procesa respuestas de pago de crédito.
   * Este método procesa eventos que indican el resultado de un intento de pago
   * con crédito, ya sea exitoso o fallido.
   *
   * @param event El evento de respuesta de pago de crédito
   */
  @KafkaListener(
    topics = "credit.payment.response",
    groupId = "${spring.kafka.consumer.group-id:yanki-service}"
  )
  public void consumeCreditPaymentResponse(CreditPaymentResponseEvent event) {
    log.info("Received credit payment response: {}", event.getPaymentId());

    if (event.getSuccess()) {
      log.info("Payment successful for credit {}. New balance: {}",
        event.getCreditId(), event.getNewBalance());
      // Aquí podrías actualizar el saldo del wallet
    } else {
      log.error("Payment failed for credit {}: {}",
        event.getCreditId(), event.getMessage());
    }
  }
}