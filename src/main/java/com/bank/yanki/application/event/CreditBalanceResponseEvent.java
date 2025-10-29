package com.bank.yanki.application.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de respuesta a la consulta de saldo de crédito.
 *
 * <p>Este evento es la respuesta del servicio de créditos a una consulta de saldo
 * previamente solicitada mediante {@link CreditBalanceInquiryEvent}. Contiene
 * la información actualizada del saldo y disponibilidad de la tarjeta de crédito.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditBalanceResponseEvent {
  private String inquiryId;
  private String creditId;
  private Boolean isValid;
  private BigDecimal currentBalance;
  private BigDecimal availableBalance;
  private String currency;
  private LocalDateTime timestamp;
  private String reason;
  private String transactionId;
}