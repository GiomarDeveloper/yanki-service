package com.bank.yanki.application.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de solicitud de consumo con tarjeta de crédito.
 *
 * <p>Este evento se genera cuando se realiza un consumo o compra utilizando
 * una tarjeta de crédito asociada al sistema Yanki. Representa una transacción
 * de compra en establecimientos comerciales o servicios.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionConsumptionRequestEvent {
  private String consumptionId;
  private String creditId;
  private BigDecimal amount;
  private String description;
  private String merchant;
  private LocalDateTime transactionDate;
  private String source; // "YANKI_SERVICE"
}