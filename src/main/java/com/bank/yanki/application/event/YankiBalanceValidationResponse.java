package com.bank.yanki.application.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de respuesta a la validación de saldo de billetera Yanki.
 *
 * <p>Este evento es la respuesta del servicio Yanki a una solicitud de validación
 * de saldo previamente enviada mediante {@link YankiBalanceValidationEvent}.
 * Contiene el resultado de la validación y la información actualizada del saldo.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YankiBalanceValidationResponse {
  private String validationId;
  private String requestService;
  private String phoneNumber;
  private Double currentBalance;
  private Double requiredAmount;
  private Boolean sufficientBalance;
  private String status;
  private String message;
  private Long timestamp;
}