package com.bank.yanki.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de transacción entre billeteras Yanki.
 *
 * <p>Esta clase representa la estructura de datos requerida para realizar
 * transferencias entre usuarios del sistema Yanki. Incluye validaciones
 * básicas para garantizar la integridad de los datos.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
  @NotBlank
  private String fromPhoneNumber;

  @NotBlank
  private String toPhoneNumber;

  @NotNull
  @DecimalMin(value = "0.01")
  private BigDecimal amount;

  private String description;
}