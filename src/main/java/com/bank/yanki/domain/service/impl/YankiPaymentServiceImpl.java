package com.bank.yanki.domain.service.impl;

import com.bank.yanki.application.event.YankiPaymentCompletedEvent;
import com.bank.yanki.application.event.YankiPaymentRequestEvent;
import com.bank.yanki.domain.model.YankiTransaction;
import com.bank.yanki.domain.model.YankiWallet;
import com.bank.yanki.domain.repository.YankiTransactionRepository;
import com.bank.yanki.domain.repository.YankiWalletRepository;
import com.bank.yanki.domain.service.YankiPaymentService;
import com.bank.yanki.infrastructure.messaging.KafkaEventProducer;
import com.bank.yanki.model.TransactionStatusEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementación del servicio de procesamiento de pagos Yanki.
 *
 * <p>Este servicio procesa eventos de pago entre billeteras Yanki, realizando
 * transferencias de fondos entre usuarios. Incluye validaciones de negocio
 * y registro de transacciones.</p>
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YankiPaymentServiceImpl implements YankiPaymentService {

  private final YankiWalletRepository walletRepository;
  private final YankiTransactionRepository transactionRepository;
  private final KafkaEventProducer kafkaProducer;

  /**
   * Procesa un evento de pago entre billeteras Yanki.
   *
   * <p>Este método realiza las siguientes operaciones:
   * <ol>
   *   <li>Busca ambas billeteras (remitente y destinatario)</li>
   *   <li>Valida que ambas billeteras existan</li>
   *   <li>Verifica saldo suficiente en billetera de origen</li>
   *   <li>Realiza la transferencia de fondos</li>
   *   <li>Crea registro de transacción</li>
   *   <li>Envía respuesta de confirmación</li>
   * </ol>
   * </p>
   *
   */
  @Override
  public Mono<Void> processYankiPayment(YankiPaymentRequestEvent event) {
    log.info("💰 Processing Yanki payment - PaymentId: {}, From: {}, To: {}, Amount: {}",
      event.getPaymentId(), event.getFromPhoneNumber(), event.getToPhoneNumber(),
      event.getAmount());

    return Mono.zip(
      walletRepository.findByPhoneNumber(event.getFromPhoneNumber()),
      walletRepository.findByPhoneNumber(event.getToPhoneNumber())
    ).flatMap(tuple -> {
      YankiWallet fromWallet = tuple.getT1();
      YankiWallet toWallet = tuple.getT2();

      // Validar que ambos wallets existen
      if (fromWallet == null || toWallet == null) {
        String errorMsg = "Wallet no encontrado: " +
          (fromWallet == null ? event.getFromPhoneNumber() : event.getToPhoneNumber());
        log.error("❌ {}", errorMsg);
        return sendPaymentResponse(event, false, errorMsg);
      }

      // Validar saldo del remitente
      BigDecimal amount = BigDecimal.valueOf(event.getAmount());
      if (!fromWallet.hasSufficientBalance(amount)) {
        String errorMsg = String.format("Saldo insuficiente. Disponible: %.2f, Requerido: %.2f",
          fromWallet.getBalance(), amount);
        log.error("❌ {}", errorMsg);
        return sendPaymentResponse(event, false, errorMsg);
      }

      // Procesar la transferencia
      return processTransfer(fromWallet, toWallet, amount, event);
    }).onErrorResume(ex -> {
      log.error("❌ Error processing Yanki payment: {}", ex.getMessage());
      return sendPaymentResponse(event, false, "Error procesando pago: " + ex.getMessage());
    });
  }

  /**
   * Procesa la transferencia de fondos entre dos billeteras.
   *
   * <p>Este método realiza las siguientes operaciones atómicas:
   * <ol>
   *   <li>Actualiza saldo de billetera de origen (resta)</li>
   *   <li>Actualiza saldo de billetera de destino (suma)</li>
   *   <li>Crea registro de transacción</li>
   *   <li>Envía respuesta de confirmación</li>
   * </ol>
   * </p>
   *
   * @param fromWallet billetera de origen
   * @param toWallet billetera de destino
   * @param amount monto a transferir
   * @param event evento de pago original
   * @return un {@link Mono} que completa cuando la transferencia termina
   */
  private Mono<Void> processTransfer(YankiWallet fromWallet, YankiWallet toWallet,
                                     BigDecimal amount, YankiPaymentRequestEvent event) {
    // Realizar la transferencia
    fromWallet.updateBalance(amount, false); // Restar del remitente
    toWallet.updateBalance(amount, true);    // Sumar al destinatario

    return walletRepository.save(fromWallet)
      .then(walletRepository.save(toWallet))
      .then(createTransactionRecord(fromWallet, toWallet, amount, event))
      .then(sendPaymentResponse(event, true, "Pago Yanki procesado exitosamente"))
      .doOnSuccess(
        v -> log.info("✅ Yanki payment completed - PaymentId: {}", event.getPaymentId()));
  }

  /**
   * Crea un registro de transacción en la base de datos.
   *
   * @param fromWallet billetera de origen
   * @param toWallet billetera de destino
   * @param amount monto transferido
   * @param event evento de pago original
   * @return un {@link Mono} que contiene la transacción creada
   */
  private Mono<YankiTransaction> createTransactionRecord(YankiWallet fromWallet,
                                                         YankiWallet toWallet,
                                                         BigDecimal amount,
                                                         YankiPaymentRequestEvent event) {
    YankiTransaction transaction = YankiTransaction.builder()
      .id(UUID.randomUUID().toString())
      .transactionId(event.getPaymentId())
      .amount(amount)
      .fromWalletId(fromWallet.getId())
      .toWalletId(toWallet.getId())
      .fromPhoneNumber(fromWallet.getPhoneNumber())
      .toPhoneNumber(toWallet.getPhoneNumber())
      .description(event.getDescription())
      .type(YankiTransaction.TransactionType.TRANSFER)
      .status(TransactionStatusEnum.COMPLETED)
      .transactionDate(LocalDateTime.now())
      .createdAt(LocalDateTime.now())
      .updatedAt(LocalDateTime.now())
      .build();

    return transactionRepository.save(transaction)
      .doOnSuccess(
        t -> log.info("📝 Transaction record created - TransactionId: {}", t.getTransactionId()));
  }

  /**
   * Envía una respuesta de procesamiento de pago.
   *
   * @param event evento de pago original
   * @param success indica si el pago fue exitoso
   * @param message mensaje descriptivo del resultado
   * @return un {@link Mono} que completa cuando se envía la respuesta
   */
  private Mono<Void> sendPaymentResponse(YankiPaymentRequestEvent event, boolean success,
                                         String message) {
    YankiPaymentCompletedEvent response = YankiPaymentCompletedEvent.builder()
      .paymentId(event.getPaymentId())
      .requestId(event.getRequestId())
      .success(success)
      .message(message)
      .timestamp(System.currentTimeMillis())
      .build();

    return kafkaProducer.sendYankiPaymentResponse(response)
      .doOnSuccess(v -> {
        if (success) {
          log.info("✅ Yanki payment response sent - PaymentId: {}", event.getPaymentId());
        } else {
          log.error("❌ Yanki payment failed response sent - PaymentId: {}", event.getPaymentId());
        }
      });
  }
}