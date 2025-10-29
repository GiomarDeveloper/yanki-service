package com.bank.yanki.domain.service;

import com.bank.yanki.application.event.YankiPaymentRequestEvent;
import reactor.core.publisher.Mono;

/**
 * Servicio para el procesamiento de pagos entre billeteras Yanki.
 *
 * <p>Define la operación para procesar eventos de pago de manera asíncrona.
 * Las implementaciones deben validar la existencia de las billeteras,
 * verificar saldos suficientes, realizar transferencias y registrar
 * las transacciones.</p>
 *
 */
public interface YankiPaymentService {

  /**
   * Procesa un evento de pago entre billeteras Yanki.
   *
   * <p>Este método debe:
   * <ul>
   *   <li>Validar existencia de ambas billeteras</li>
   *   <li>Verificar saldo suficiente en billetera de origen</li>
   *   <li>Realizar transferencia de fondos</li>
   *   <li>Registrar transacción en base de datos</li>
   *   <li>Enviar respuesta de confirmación</li>
   * </ul>
   * </p>
   *
   * @param event el evento de solicitud de pago
   * @return un {@link Mono} que completa cuando el procesamiento termina
   */
  Mono<Void> processYankiPayment(YankiPaymentRequestEvent event);
}