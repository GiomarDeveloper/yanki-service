package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de validación de saldo en billetera Yanki.
 *
 * <p>Este evento se genera cuando un servicio externo necesita validar si una
 * billetera Yanki tiene saldo suficiente para realizar una transacción.
 * Es utilizado principalmente por otros microservicios para verificar fondos
 * antes de procesar operaciones que involucren billeteras Yanki.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiBalanceValidationEvent {
  private String validationId;
  private String requestService;
  private String phoneNumber;
  private Double requiredAmount;
  private String currency;
  private Long timestamp;
}