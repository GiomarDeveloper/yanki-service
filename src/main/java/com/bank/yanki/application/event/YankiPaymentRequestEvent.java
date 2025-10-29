package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de completación de pago en el sistema Yanki.
 *
 * <p>Este evento se publica cuando se completa el procesamiento de un pago
 * solicitado mediante {@link YankiPaymentRequestEvent}. Notifica al servicio
 * solicitante sobre el resultado de la transacción.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiPaymentRequestEvent {
  private String paymentId;
  private String requestId;
  private String fromPhoneNumber;
  private String toPhoneNumber;
  private Double amount;
  private String description;
  private String currency;
  private Long timestamp;
}