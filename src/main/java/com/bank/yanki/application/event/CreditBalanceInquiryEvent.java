package com.bank.yanki.application.event;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de consulta de saldo de crédito para transacciones Yanki.
 *
 * <p>Este evento se genera cuando el sistema Yanki necesita validar el saldo disponible
 * en una tarjeta de crédito asociada antes de procesar una transacción. Se envía al
 * servicio de créditos para realizar la validación correspondiente.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBalanceInquiryEvent {
  private String inquiryId;
  private String creditId;
  private BigDecimal requiredAmount;
  private String source;
  private String transactionId;
  private String fromPhoneNumber;
  private String toPhoneNumber;
  private String description;
}