package com.bank.yanki.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una billetera Yanki con los criterios especificados.
 *
 * <p>Esta excepción se produce durante operaciones de búsqueda, consulta o actualización
 * cuando no existe ninguna billetera que coincida con los parámetros proporcionados.</p>
 *
 */
public class WalletNotFoundException extends RuntimeException {

  /**
   * Crea una nueva excepción con un mensaje personalizado.
   *
   * @param message descripción detallada del error
   */
  public WalletNotFoundException(String message) {
    super(message);
  }

  /**
   * Crea una nueva excepción indicando los criterios de búsqueda que no produjeron resultados.
   *
   * @param phoneNumber número de teléfono utilizado en la búsqueda
   * @param documentNumber documento de identidad utilizado en la búsqueda
   */
  public WalletNotFoundException(String phoneNumber, String documentNumber) {
    super(String.format("Wallet not found with phone: %s or document: %s", phoneNumber,
      documentNumber));
  }
}