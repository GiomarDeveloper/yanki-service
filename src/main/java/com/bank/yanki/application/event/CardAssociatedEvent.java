package com.bank.yanki.application.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de dominio para representar la asociación de una tarjeta a una billetera Yanki.
 *
 * <p>Este evento se publica cuando un usuario asocia exitosamente una tarjeta de crédito
 * o débito a su billetera digital Yanki, permitiendo realizar transacciones utilizando
 * los fondos disponibles en la tarjeta.</p>
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardAssociatedEvent {
  private String walletId;
  private String phoneNumber;
  private String creditId;
  private LocalDateTime associatedAt;
}