package com.bank.yanki.application.dto;

import com.bank.yanki.domain.model.YankiWallet;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO que representa una transacción pendiente en el sistema Yanki.
 *
 * <p>Esta clase se utiliza para almacenar temporalmente información de transacciones
 * que están siendo procesadas, especialmente durante validaciones de saldo
 * o autorizaciones pendientes.</p>
 *
 */
@Data
@AllArgsConstructor
public class PendingTransaction {
  private YankiWallet fromWallet;
  private String toPhoneNumber;
  private BigDecimal amount;
  private String description;
  private LocalDateTime createdAt;
  private String transactionId;
}