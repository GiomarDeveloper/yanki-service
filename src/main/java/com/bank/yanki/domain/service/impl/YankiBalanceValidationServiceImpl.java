package com.bank.yanki.domain.service.impl;

import com.bank.yanki.application.event.YankiBalanceValidationEvent;
import com.bank.yanki.application.event.YankiBalanceValidationResponse;
import com.bank.yanki.domain.model.YankiWallet;
import com.bank.yanki.domain.service.YankiBalanceValidationService;
import com.bank.yanki.domain.service.YankiWalletService;
import com.bank.yanki.infrastructure.messaging.KafkaEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementación del servicio de validación de saldos de billeteras Yanki.
 *
 * <p>Este servicio procesa eventos de validación de saldo para determinar
 * si una billetera Yanki tiene fondos suficientes para una transacción.
 * Se integra con el sistema de mensajería Kafka para enviar respuestas asíncronas.</p>
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YankiBalanceValidationServiceImpl implements YankiBalanceValidationService {

  private final YankiWalletService walletService;
  private final KafkaEventProducer kafkaProducer;

  /**
   * Procesa un evento de validación de saldo de billetera Yanki.
   *
   * <p>Este método realiza las siguientes validaciones:
   * <ol>
   *   <li>Verifica que la billetera exista</li>
   *   <li>Valida que la billetera esté activa</li>
   *   <li>Comprueba que el saldo sea suficiente para el monto requerido</li>
   * </ol>
   * </p>
   */
  @Override
  public Mono<Void> processBalanceValidation(YankiBalanceValidationEvent event) {
    log.info("🔍 Processing Yanki balance validation - ValidationId: {}, Phone: {}, Amount: {}",
      event.getValidationId(), event.getPhoneNumber(), event.getRequiredAmount());

    return walletService.findByPhoneNumber(event.getPhoneNumber())
      .switchIfEmpty(Mono.defer(() -> {
        // SOLO se ejecuta si el wallet NO existe
        log.warn("❌ Yanki wallet not found for validation: {} - Phone: {}",
          event.getValidationId(), event.getPhoneNumber());
        sendValidationResponse(event, false, "WALLET_NOT_FOUND",
          "Wallet Yanki no encontrado para el teléfono: " + event.getPhoneNumber());
        return Mono.empty();
      }))
      .flatMap(wallet -> {
        log.info("✅ Wallet encontrado - ValidationId: {}, WalletId: {}",
          event.getValidationId(), wallet.getId());
        return validateWalletBalance(wallet, event);
      })
      .onErrorResume(ex -> {
        log.error("❌ Error processing Yanki balance validation: {} - {}",
          event.getValidationId(), ex.getMessage(), ex);
        sendValidationResponse(event, false, "ERROR",
          "Error validando saldo Yanki: " + ex.getMessage());
        return Mono.empty();
      })
      .then();
  }

  /**
   * Valida el saldo de una billetera Yanki específica.
   *
   * @param wallet la billetera a validar
   * @param event el evento de validación original
   * @return un {@link Mono} que completa cuando la validación termina
   */
  private Mono<Void> validateWalletBalance(YankiWallet wallet, YankiBalanceValidationEvent event) {
    return Mono.fromRunnable(() -> {
      log.debug("💰 Validating Yanki wallet balance - Wallet: {}, Balance: {}, Required: {}",
        wallet.getId(), wallet.getBalance(), event.getRequiredAmount());

      // Validar que el wallet esté activo
      if (wallet.getStatus() != YankiWallet.YankiWalletStatus.ACTIVE) {
        log.warn("🚫 Yanki wallet inactive: {} - Status: {}", wallet.getId(), wallet.getStatus());
        sendValidationResponse(event, false, "WALLET_INACTIVE",
          "El wallet Yanki no está activo. Estado: " + wallet.getStatus());
        return;
      }

      // Validar saldo suficiente
      boolean sufficientBalance = wallet.getBalance().doubleValue() >= event.getRequiredAmount();
      String status = sufficientBalance ? "SUFFICIENT_FUNDS" : "INSUFFICIENT_FUNDS";
      String message = sufficientBalance ?
        "Saldo Yanki suficiente" :
        String.format("Saldo Yanki insuficiente. Disponible: %.2f, Requerido: %.2f",
          wallet.getBalance(), event.getRequiredAmount());

      log.info(
        "💰 Yanki balance validation result - Wallet: {}, Sufficient: {}, Available: {}, Required: {}",
        wallet.getId(), sufficientBalance, wallet.getBalance(), event.getRequiredAmount());

      sendValidationResponse(event, sufficientBalance, status, message,
        wallet.getBalance().doubleValue());
    });
  }

  /**
   * Envía una respuesta de validación con información de saldo actual.
   *
   * @param event el evento de validación original
   * @param sufficient indica si hay saldo suficiente
   * @param status el estado de la validación
   * @param message el mensaje descriptivo
   * @param currentBalance el saldo actual de la billetera
   */
  private void sendValidationResponse(YankiBalanceValidationEvent event, boolean sufficient,
                                      String status, String message, Double currentBalance) {
    log.info("📤 PREPARANDO respuesta Yanki - ValidationId: {}, Sufficient: {}, Status: {}",
      event.getValidationId(), sufficient, status);

    YankiBalanceValidationResponse response = YankiBalanceValidationResponse.builder()
      .validationId(event.getValidationId())
      .requestService("yanki-service")
      .phoneNumber(event.getPhoneNumber())
      .currentBalance(currentBalance)
      .requiredAmount(event.getRequiredAmount())
      .sufficientBalance(sufficient)
      .status(status)
      .message(message)
      .timestamp(System.currentTimeMillis())
      .build();

    kafkaProducer.sendYankiBalanceValidationResponse(response)
      .doOnSubscribe(
        s -> log.info("🚀 ENVIANDO respuesta Yanki - ValidationId: {}", event.getValidationId()))
      .subscribe(
        success -> log.info("✅ Yanki validation response SENT - ValidationId: {}",
          event.getValidationId()),
        error -> log.error("❌ Error sending Yanki validation response: {}", error.getMessage())
      );
  }

  /**
   * Envía una respuesta de validación sin información de saldo actual.
   *
   * @param event el evento de validación original
   * @param sufficient indica si hay saldo suficiente
   * @param status el estado de la validación
   * @param message el mensaje descriptivo
   */
  private void sendValidationResponse(YankiBalanceValidationEvent event, boolean sufficient,
                                      String status, String message) {
    sendValidationResponse(event, sufficient, status, message, 0.0);
  }

  /**
   * Procesa una respuesta de validación de saldo recibida de otros servicios.
   *
   * <p>Este método está destinado para escenarios donde Yanki necesita validar
   * saldos en otros sistemas. Actualmente es un placeholder para futuras integraciones.</p>
   *
   * @param response la respuesta de validación recibida
   */
  @Override
  public void processBalanceValidationResponse(YankiBalanceValidationResponse response) {
    log.info("📨 Processing Yanki balance validation response - ValidationId: {}, Sufficient: {}",
      response.getValidationId(), response.getSufficientBalance());
    // Aquí puedes procesar respuestas si Yanki necesita validar otros servicios
  }
}