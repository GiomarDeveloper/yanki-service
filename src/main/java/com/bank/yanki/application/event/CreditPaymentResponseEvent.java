package com.bank.yanki.application.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de respuesta al procesamiento de pago con tarjeta de crédito.
 *
 * <p>Este evento es la respuesta del servicio de créditos a una solicitud de pago
 * previamente enviada mediante {@link CreditPaymentRequestEvent}. Contiene el
 * resultado del procesamiento y la información actualizada del saldo.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditPaymentResponseEvent {
  private String paymentId;
  private String creditId;
  private Boolean success;
  private String transactionId;
  private BigDecimal newBalance;
  private String message;
  private LocalDateTime timestamp;
}