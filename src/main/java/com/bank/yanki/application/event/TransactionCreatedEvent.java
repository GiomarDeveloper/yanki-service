package com.bank.yanki.application.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de creación de transacción en el sistema Yanki.
 *
 * <p>Este evento se publica cuando se crea exitosamente una transacción
 * en el sistema Yanki, ya sea una transferencia entre usuarios, recarga,
 * o pago con tarjeta de crédito. Sirve para notificar a otros sistemas
 * sobre la actividad transaccional.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
  private String transactionId;
  private String fromWalletId;
  private String toWalletId;
  private String fromPhoneNumber;
  private String toPhoneNumber;
  private BigDecimal amount;
  private String type;
  private String description;
  private LocalDateTime transactionDate;

  private String creditId;
  private String customerId;
  private String source;
}