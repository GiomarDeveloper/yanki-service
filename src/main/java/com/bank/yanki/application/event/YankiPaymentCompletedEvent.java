package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de solicitud de pago entre billeteras Yanki.
 *
 * <p>Este evento se genera cuando un servicio externo solicita realizar un pago
 * o transferencia entre dos billeteras Yanki. El servicio Yanki procesará la
 * transacción y responderá con un {@link YankiPaymentCompletedEvent}.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiPaymentCompletedEvent {
  private String paymentId;
  private String requestId;
  private boolean success;
  private String message;
  private Long timestamp;
}