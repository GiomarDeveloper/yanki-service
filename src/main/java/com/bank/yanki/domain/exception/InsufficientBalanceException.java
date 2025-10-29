package com.bank.yanki.domain.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando una billetera Yanki no tiene saldo suficiente para una operación.
 *
 * <p>Esta excepción se produce durante transacciones como transferencias, pagos o retiros
 * cuando el saldo disponible en la billetera es menor al monto requerido para la operación.</p>
 *
 * <p>La excepción incluye información detallada sobre el saldo actual y el monto requerido
 * para facilitar el diagnóstico y la comunicación al usuario.</p>
 *
 */
public class InsufficientBalanceException extends RuntimeException {
  public InsufficientBalanceException(BigDecimal currentBalance, BigDecimal requiredAmount) {

    /**
     * Crea una nueva excepción con información detallada del saldo.
     *
     * @param currentBalance saldo actual disponible en la billetera
     * @param requiredAmount monto requerido para la operación
     */
    super(String.format("Insufficient balance. Current: %s, Required: %s", currentBalance,
      requiredAmount));
  }
}