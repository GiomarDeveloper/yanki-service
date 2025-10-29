package com.bank.yanki.domain.service;

import com.bank.yanki.application.event.YankiBalanceValidationEvent;
import com.bank.yanki.application.event.YankiBalanceValidationResponse;
import reactor.core.publisher.Mono;

/**
 * Servicio para la validación de saldos de billeteras Yanki.
 *
 * <p>Define las operaciones para procesar eventos de validación de saldo
 * de manera asíncrona. Las implementaciones deben buscar la billetera,
 * validar su estado y saldo, y enviar una respuesta a través del
 * sistema de mensajería.</p>
 *
 */
public interface YankiBalanceValidationService {

  /**
   * Procesa un evento de validación de saldo de billetera Yanki.
   *
   */
  Mono<Void> processBalanceValidation(YankiBalanceValidationEvent event);

  /**
   * Procesa una respuesta de validación de saldo recibida de otros servicios.
   *
   * <p>Método destinado para escenarios donde Yanki necesita validar
   * saldos en sistemas externos.</p>
   *
   * @param response la respuesta de validación recibida
   */
  void processBalanceValidationResponse(YankiBalanceValidationResponse response);
}