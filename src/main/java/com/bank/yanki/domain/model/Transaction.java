package com.bank.yanki.domain.model;

import com.bank.yanki.model.TransactionStatusEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo de dominio que representa una transacción en el sistema Yanki.
 *
 * <p>Esta clase encapsula la información básica de una transacción financiera
 * entre billeteras Yanki. Es utilizada principalmente para lógica de negocio
 * y transferencia de datos entre capas internas del sistema.</p>
 *
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
  private String id;
  private BigDecimal amount;
  private String fromWalletId;
  private String toWalletId;
  private String fromPhoneNumber;
  private String toPhoneNumber;
  private String description;
  private TransactionStatusEnum status;
  private LocalDateTime transactionDate;
}