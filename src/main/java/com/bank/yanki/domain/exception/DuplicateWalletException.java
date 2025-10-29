package com.bank.yanki.domain.exception;

/**
 * Excepción lanzada cuando se intenta crear una billetera Yanki con datos que ya existen.
 *
 * <p>Esta excepción se produce durante el registro de nuevas billeteras cuando
 * se detecta que ya existe una billetera con el mismo número de teléfono o
 * documento de identidad en el sistema.</p>
 *
 */
public class DuplicateWalletException extends RuntimeException {

  /**
   * Crea una nueva excepción indicando el campo y valor que causaron el duplicado.
   *
   * @param field el campo que causó el duplicado (ej: "phoneNumber", "documentNumber")
   * @param value el valor duplicado que ya existe en el sistema
   */
  public DuplicateWalletException(String field, String value) {
    super(String.format("Wallet already exists with %s: %s", field, value));
  }
}