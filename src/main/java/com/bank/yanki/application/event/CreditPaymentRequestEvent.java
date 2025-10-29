package com.bank.yanki.application.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de solicitud de pago con tarjeta de crédito en el sistema Yanki.
 *
 * <p>Este evento se genera cuando un usuario realiza un pago utilizando los fondos
 * disponibles en su tarjeta de crédito asociada a la billetera Yanki. El evento
 * se envía al servicio de créditos para procesar el cargo correspondiente.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditPaymentRequestEvent {
  private String paymentId;
  private String creditId;
  private String walletId;
  private String phoneNumber;
  private BigDecimal amount;
  private String description;
  private LocalDateTime timestamp;
}