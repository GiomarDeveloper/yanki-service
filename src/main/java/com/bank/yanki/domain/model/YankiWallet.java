package com.bank.yanki.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Entidad de dominio que representa una billetera digital Yanki.
 *
 * <p>Esta clase modela la billetera digital de un usuario en el sistema Yanki,
 * incluyendo información personal, saldo, estado y métodos de negocio para
 * operaciones financieras.</p>
 *
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "yanki_wallets")
public class YankiWallet {
  @Id
  private String id;
  private String documentNumber;
  private DocumentType documentType;
  private String phoneNumber;
  private String imei;
  private String email;
  private BigDecimal balance;
  private String associatedCreditId;
  private YankiWalletStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public void activate() {
    this.status = YankiWalletStatus.ACTIVE;
    this.updatedAt = LocalDateTime.now();
  }

  public void associateCard(String creditId) {
    this.associatedCreditId = creditId;
    this.updatedAt = LocalDateTime.now();
  }

  public void updateBalance(BigDecimal amount, boolean isAddition) {
    if (isAddition) {
      this.balance = this.balance.add(amount);
    } else {
      this.balance = this.balance.subtract(amount);
    }
    this.updatedAt = LocalDateTime.now();
  }

  public boolean hasSufficientBalance(BigDecimal amount) {
    return this.balance.compareTo(amount) >= 0;
  }

  public enum DocumentType {
    DNI, CEX, PASSPORT
  }

  public enum YankiWalletStatus {
    ACTIVE, BLOCKED, PENDING_CARD_ASSOCIATION, SUSPENDED
  }
}