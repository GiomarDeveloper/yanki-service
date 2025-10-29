package com.bank.yanki.domain.model;

import com.bank.yanki.model.TransactionStatusEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidad de persistencia para transacciones Yanki en MongoDB.
 *
 * <p>Esta clase representa el modelo de datos que se persiste en la base de datos
 * MongoDB para el registro histórico de todas las transacciones del sistema Yanki.</p>
 *
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "yanki_transactions")
public class YankiTransaction {
  @Id
  private String id;
  private String transactionId;
  private BigDecimal amount;
  private String fromWalletId;
  private String toWalletId;
  private String fromPhoneNumber;
  private String toPhoneNumber;
  private String description;
  private TransactionType type;
  private TransactionStatusEnum status;
  private LocalDateTime transactionDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public enum TransactionType {
    TRANSFER,  // Transferencia entre wallets
    PAYMENT,   // Pago a tarjeta
    DEPOSIT    // Depósito a wallet
  }
}