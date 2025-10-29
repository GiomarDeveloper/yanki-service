package com.bank.yanki.application.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de creación de billetera Yanki.
 *
 * <p>Este evento se publica cuando un usuario registra exitosamente una nueva
 * billetera digital en el sistema Yanki. El evento notifica a otros sistemas
 * sobre la creación de la billetera para posibles integraciones o procesos
 * posteriores.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreatedEvent {
  private String walletId;
  private String phoneNumber;
  private String documentNumber;
  private String documentType;
  private String email;
  private LocalDateTime createdAt;
}