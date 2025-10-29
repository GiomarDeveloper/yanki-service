package com.bank.yanki.application.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de solicitud de pago de transacción con tarjeta de crédito.
 *
 * <p>Este evento se utiliza específicamente para solicitar el pago de transacciones
 * pendientes o el abono de saldos utilizando fondos de tarjeta de crédito.
 * Se diferencia de {@link CreditPaymentRequestEvent} en que está más orientado
 * a operaciones de pago de obligaciones existentes.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPaymentRequestEvent {
  private String paymentId;
  private String creditId;
  private BigDecimal amount;
  private String description;
  private String customerId;
  private LocalDateTime paymentDate;
  private String source; // "YANKI_SERVICE"
}