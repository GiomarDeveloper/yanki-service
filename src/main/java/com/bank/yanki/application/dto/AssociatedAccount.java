package com.bank.yanki.application.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * DTO que representa una cuenta bancaria asociada a una billetera Yanki.
 *
 * <p>Esta clase contiene información sobre las cuentas bancarias que un usuario
 * ha vinculado a su billetera digital para realizar operaciones de transferencia
 * y recarga.</p>
 *
 */
@Data
public class AssociatedAccount {
  private String accountId;
  private Integer sequenceOrder;
  private LocalDateTime associatedAt;
  private String status;
}