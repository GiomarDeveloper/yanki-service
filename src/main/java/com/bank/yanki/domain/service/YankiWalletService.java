package com.bank.yanki.domain.service;

import com.bank.yanki.application.dto.PendingTransaction;
import com.bank.yanki.application.event.CardAssociatedEvent;
import com.bank.yanki.application.event.CreditBalanceInquiryEvent;
import com.bank.yanki.application.event.CreditBalanceResponseEvent;
import com.bank.yanki.application.event.TransactionConsumptionRequestEvent;
import com.bank.yanki.application.event.TransactionPaymentRequestEvent;
import com.bank.yanki.application.event.WalletCreatedEvent;
import com.bank.yanki.domain.exception.WalletNotFoundException;
import com.bank.yanki.domain.model.Transaction;
import com.bank.yanki.domain.model.YankiTransaction;
import com.bank.yanki.domain.model.YankiWallet;
import com.bank.yanki.domain.repository.YankiTransactionRepository;
import com.bank.yanki.domain.repository.YankiWalletRepository;
import com.bank.yanki.infrastructure.cache.RedisCacheService;
import com.bank.yanki.infrastructure.messaging.KafkaEventProducer;
import com.bank.yanki.model.TransactionStatusEnum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio para la gestión de billeteras Yanki.
 *
 * <p>Este servicio proporciona operaciones para crear, consultar y gestionar billeteras Yanki,
 * incluyendo procesamiento de pagos, asociación de tarjetas y manejo de transacciones
 * con integración a sistemas externos mediante Kafka y Redis.</p>
 *
 */
@Slf4j
@RequiredArgsConstructor
@Service("yankiWalletDomainService")
public class YankiWalletService {

  private final YankiWalletRepository walletRepository;
  private final YankiTransactionRepository yankiTransactionRepository;
  private final KafkaEventProducer eventProducer;
  private final RedisCacheService cacheService;
  private final ConcurrentHashMap<String, PendingTransaction> pendingTransactions =
    new ConcurrentHashMap<>();

  /**
   * Crea una nueva billetera Yanki.
   *
   * <p>Este método valida que no existan billeteras con el mismo número de teléfono o documento,
   * establece valores por defecto para el balance y estado, y publica un evento de creación
   * via Kafka. Además, almacena la billetera en caché Redis.</p>
   *
   * @param wallet La billetera a crear
   * @return Mono que emite la billetera creada
   * @throws RuntimeException si ya existe una billetera con el mismo número de teléfono o documento
   */
  public Mono<YankiWallet> createWallet(YankiWallet wallet) {
    return walletRepository.existsByPhoneNumber(wallet.getPhoneNumber())
      .flatMap(exists -> {
        if (exists) {
          return Mono.error(new RuntimeException("Wallet with phone number already exists"));
        }
        return walletRepository.existsByDocumentNumber(wallet.getDocumentNumber());
      })
      .flatMap(exists -> {
        if (exists) {
          return Mono.error(new RuntimeException("Wallet with document number already exists"));
        }
        if (wallet.getBalance() == null) {
          wallet.setBalance(BigDecimal.valueOf(0.00)); // Valor por defecto
        }
        wallet.setStatus(YankiWallet.YankiWalletStatus.ACTIVE);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());

        return walletRepository.save(wallet);
      })
      .flatMap(savedWallet -> {
        // Publicar evento Kafka
        WalletCreatedEvent event = WalletCreatedEvent.builder()
          .walletId(savedWallet.getId())
          .phoneNumber(savedWallet.getPhoneNumber())
          .documentNumber(savedWallet.getDocumentNumber())
          .documentType(savedWallet.getDocumentType().name())
          .email(savedWallet.getEmail())
          .createdAt(savedWallet.getCreatedAt())
          .build();

        return eventProducer.sendWalletCreatedEvent(event)
          .thenReturn(savedWallet);
      })
      .doOnSuccess(w -> {
        log.info("Wallet created successfully: {}", w.getId());
        // Cachear en Redis
        cacheService.cacheWallet(w.getPhoneNumber(), w).subscribe();
      })
      .doOnError(error -> log.error("Error creating wallet: {}", error.getMessage()));
  }

  /**
   * Busca una billetera por número de teléfono.
   *
   * <p>Este método implementa un patrón cache-aside, buscando primero en Redis cache
   * y luego en la base de datos MongoDB si no se encuentra en caché.</p>
   *
   * @param phoneNumber El número de teléfono asociado a la billetera
   * @return Mono que emite la billetera encontrada
   * @throws WalletNotFoundException si no se encuentra la billetera
   */
  public Mono<YankiWallet> findByPhoneNumber(String phoneNumber) {
    // Primero buscar en Redis cache
    return cacheService.getCachedWallet(phoneNumber)
      .cast(YankiWallet.class)
      .switchIfEmpty(
        // Si no está en cache, buscar en MongoDB
        walletRepository.findByPhoneNumber(phoneNumber)
          .switchIfEmpty(Mono.error(
            new WalletNotFoundException("Wallet not found with phone number: " + phoneNumber)))
          .flatMap(wallet ->
            // Guardar en cache para próximas consultas
            cacheService.cacheWallet(phoneNumber, wallet)
              .thenReturn(wallet)
          )
      );
  }

  /**
   * Asocia una tarjeta de débito a una billetera existente.
   *
   * <p>Este método busca la billetera por número de teléfono, asocia la tarjeta especificada
   * y publica un evento de asociación via Kafka. Actualiza la caché Redis con la billetera modificada.</p>
   *
   * @param phoneNumber El número de teléfono de la billetera
   * @param creditId El identificador de la tarjeta a asociar
   * @return Mono que emite la billetera actualizada
   * @throws WalletNotFoundException si no se encuentra la billetera
   */
  public Mono<YankiWallet> associateDebitCard(String phoneNumber, String creditId) {
    return findByPhoneNumber(phoneNumber)
      .flatMap(wallet -> {
        wallet.associateCard(creditId);
        return walletRepository.save(wallet);
      })
      .flatMap(updatedWallet -> {
        // Publicar evento Kafka
        CardAssociatedEvent event = CardAssociatedEvent.builder()
          .walletId(updatedWallet.getId())
          .phoneNumber(updatedWallet.getPhoneNumber())
          .creditId(creditId)
          .associatedAt(updatedWallet.getUpdatedAt())
          .build();

        return eventProducer.sendCardAssociatedEvent(event)
          .thenReturn(updatedWallet);
      })
      .doOnSuccess(wallet -> {
        log.info("Credit card associated to wallet: {}", wallet.getId());
        // Actualizar cache
        cacheService.cacheWallet(phoneNumber, wallet).subscribe();
      });
  }

  /**
   * Procesa un pago entre dos billeteras Yanki.
   *
   * <p>Este método maneja dos escenarios:
   * <ul>
   *   <li>Si el remitente tiene tarjeta asociada: valida el balance via Kafka y crea transacción pendiente</li>
   *   <li>Si el remitente no tiene tarjeta: procesa el pago inmediatamente validando el balance disponible</li>
   * </ul>
   * </p>
   *
   * @param fromPhoneNumber Número de teléfono del remitente
   * @param toPhoneNumber Número de teléfono del destinatario
   * @param amount Monto del pago
   * @param description Descripción de la transacción
   * @return Mono que emite la transacción creada (COMPLETED o PENDING)
   * @throws WalletNotFoundException si no se encuentra alguna de las billeteras
   * @throws RuntimeException si el remitente no tiene balance suficiente
   */
  public Mono<Transaction> processPayment(String fromPhoneNumber, String toPhoneNumber,
                                          Double amount, String description) {
    BigDecimal paymentAmount = BigDecimal.valueOf(amount);

    return findByPhoneNumber(fromPhoneNumber)
      .flatMap(fromWallet -> {
        if (fromWallet.getAssociatedCreditId() != null &&
          !fromWallet.getAssociatedCreditId().isEmpty()) {
          // Para tarjetas, iniciamos validación y retornamos una transacción pendiente
          return validateCreditBalanceViaKafka(fromWallet, toPhoneNumber, paymentAmount,
            description)
            .flatMap(transactionId -> {
              // Retornar una transacción con estado PENDING
              Transaction pendingTransaction = Transaction.builder()
                .id(transactionId)
                .amount(paymentAmount)
                .fromWalletId(fromWallet.getId())
                .toWalletId(null) // Se completará después
                .fromPhoneNumber(fromWallet.getPhoneNumber())
                .toPhoneNumber(toPhoneNumber)
                .description(description)
                .transactionDate(LocalDateTime.now())
                .status(TransactionStatusEnum.PENDING)
                .build();
              return Mono.just(pendingTransaction);
            });
        } else {
          // Sin tarjeta, procesar inmediatamente
          if (!fromWallet.hasSufficientBalance(paymentAmount)) {
            return Mono.error(new RuntimeException("Insufficient balance in Yanki wallet"));
          }
          return proceedWithYankiPayment(fromWallet, toPhoneNumber, paymentAmount, description);
        }
      })
      .doOnSuccess(transaction -> {
        if (transaction.getStatus() == TransactionStatusEnum.COMPLETED) {
          log.info("Payment processed immediately: {} from {} to {}", amount, fromPhoneNumber,
            toPhoneNumber);
          cacheService.evictWalletCache(fromPhoneNumber).subscribe();
          cacheService.evictWalletCache(toPhoneNumber).subscribe();
        } else {
          log.info("Payment validation in progress - TransactionId: {}", transaction.getId());
        }
      })
      .doOnError(error -> log.error("Payment failed: {}", error.getMessage()));
  }

  private Mono<Transaction> proceedWithYankiPayment(YankiWallet fromWallet, String toPhoneNumber,
                                                    BigDecimal paymentAmount, String description) {
    return findByPhoneNumber(toPhoneNumber)
      .flatMap(toWallet -> {
        // Verificar si el destinatario tiene tarjeta asociada
        boolean toWalletHasCard = toWallet.getAssociatedCreditId() != null &&
          !toWallet.getAssociatedCreditId().isEmpty();

        // LÓGICA CORREGIDA:
        // FROM siempre resta (porque no tiene tarjeta en este método)
        fromWallet.updateBalance(paymentAmount, false);

        // TO solo suma si NO tiene tarjeta
        if (!toWalletHasCard) {
          toWallet.updateBalance(paymentAmount, true);
        }

        // Crear transacción principal
        Transaction transaction = Transaction.builder()
          .id(UUID.randomUUID().toString())
          .amount(paymentAmount)
          .fromWalletId(fromWallet.getId())
          .toWalletId(toWallet.getId())
          .fromPhoneNumber(fromWallet.getPhoneNumber())
          .toPhoneNumber(toPhoneNumber)
          .description(description)
          .transactionDate(LocalDateTime.now())
          .status(TransactionStatusEnum.COMPLETED)
          .build();

        // Guardar ambos wallets - FROM siempre se guarda
        List<Mono<YankiWallet>> saveOperations = new ArrayList<>();
        saveOperations.add(walletRepository.save(fromWallet));

        if (!toWalletHasCard) {
          saveOperations.add(walletRepository.save(toWallet));
        }

        return Mono.when(saveOperations)
          .then(Mono.defer(() -> {
            // SOLO insertar en yanki_transactions si el destinatario NO tiene tarjeta
            if (!toWalletHasCard) {
              YankiTransaction yankiTransaction = YankiTransaction.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(transaction.getId())
                .amount(paymentAmount)
                .fromWalletId(fromWallet.getId())
                .toWalletId(toWallet.getId())
                .fromPhoneNumber(fromWallet.getPhoneNumber())
                .toPhoneNumber(toPhoneNumber)
                .description(description)
                .type(YankiTransaction.TransactionType.TRANSFER)
                .status(TransactionStatusEnum.COMPLETED)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

              return yankiTransactionRepository.save(yankiTransaction)
                .thenReturn(transaction);
            } else {
              // Si el destinatario tiene tarjeta, enviar evento de PAGO
              // para que el dinero llegue a su tarjeta
              TransactionPaymentRequestEvent paymentEvent = TransactionPaymentRequestEvent.builder()
                .paymentId(UUID.randomUUID().toString())
                .creditId(toWallet.getAssociatedCreditId())
                .amount(paymentAmount)
                .description(
                  "Yanki Transfer from " + fromWallet.getPhoneNumber() + ": " + description)
                .customerId(toWallet.getDocumentNumber())
                .paymentDate(LocalDateTime.now())
                .source("YANKI_SERVICE")
                .build();

              return eventProducer.sendTransactionPaymentRequest(paymentEvent)
                .thenReturn(transaction);
            }
          }));
      });
  }

  private Mono<Void> sendKafkaEvents(YankiWallet fromWallet, YankiWallet toWallet,
                                     BigDecimal paymentAmount, String description) {
    // LÓGICA CORREGIDA SEGÚN TU CÓDIGO FUNCIONAL:
    // - ConsumptionEvent: usa creditId del TO para RESTAR (pero lógica invertida)
    // - PaymentEvent: usa creditId del FROM para SUMAR (pero lógica invertida)

    // Si el REMITENTE tiene tarjeta, enviamos evento de CONSUMO
    if (fromWallet.getAssociatedCreditId() != null &&
      !fromWallet.getAssociatedCreditId().isEmpty()) {
      TransactionConsumptionRequestEvent consumptionEvent =
        TransactionConsumptionRequestEvent.builder()
          .consumptionId(UUID.randomUUID().toString())
          .creditId(toWallet.getAssociatedCreditId())
          .amount(paymentAmount)
          .description("Yanki Transfer to " + fromWallet.getPhoneNumber() + ": " + description)
          .merchant("YANKI_TRANSFER")
          .transactionDate(LocalDateTime.now())
          .source("YANKI_SERVICE")
          .build();

      // Si el DESTINATARIO tiene tarjeta, también enviamos evento de PAGO
      if (toWallet.getAssociatedCreditId() != null && !toWallet.getAssociatedCreditId().isEmpty()) {
        TransactionPaymentRequestEvent paymentEvent = TransactionPaymentRequestEvent.builder()
          .paymentId(UUID.randomUUID().toString())
          .creditId(fromWallet.getAssociatedCreditId())
          .amount(paymentAmount)
          .description("Yanki Transfer from " + toWallet.getPhoneNumber() + ": " + description)
          .customerId(fromWallet.getDocumentNumber())
          .paymentDate(LocalDateTime.now())
          .source("YANKI_SERVICE")
          .build();

        return eventProducer.sendTransactionConsumptionRequest(consumptionEvent)
          .then(eventProducer.sendTransactionPaymentRequest(paymentEvent));
      } else {
        // Solo el remitente tiene tarjeta, solo enviamos consumo
        return eventProducer.sendTransactionConsumptionRequest(consumptionEvent);
      }
    }
    // Si solo el DESTINATARIO tiene tarjeta, enviamos evento de PAGO
    else if (toWallet.getAssociatedCreditId() != null &&
      !toWallet.getAssociatedCreditId().isEmpty()) {
      TransactionPaymentRequestEvent paymentEvent = TransactionPaymentRequestEvent.builder()
        .paymentId(UUID.randomUUID().toString())
        .creditId(
          fromWallet.getAssociatedCreditId()) // Del REMITENTE (FROM) - pero para SUMAR al TO
        .amount(paymentAmount)
        .description("Yanki Transfer from " + toWallet.getPhoneNumber() + ": " + description)
        .customerId(fromWallet.getDocumentNumber())
        .paymentDate(LocalDateTime.now())
        .source("YANKI_SERVICE")
        .build();

      return eventProducer.sendTransactionPaymentRequest(paymentEvent);
    }

    // Si ninguno tiene tarjeta, no enviamos eventos Kafka
    return Mono.empty();
  }

  private Mono<String> validateCreditBalanceViaKafka(YankiWallet fromWallet, String toPhoneNumber,
                                                     BigDecimal paymentAmount, String description) {
    String inquiryId = UUID.randomUUID().toString();
    String transactionId = UUID.randomUUID().toString();

    // Crear evento de consulta
    CreditBalanceInquiryEvent inquiryEvent = CreditBalanceInquiryEvent.builder()
      .inquiryId(inquiryId)
      .creditId(fromWallet.getAssociatedCreditId())
      .requiredAmount(paymentAmount)
      .source("YANKI_SERVICE")
      .transactionId(transactionId)
      .fromPhoneNumber(fromWallet.getPhoneNumber())
      .toPhoneNumber(toPhoneNumber)
      .description(description)
      .build();

    // Guardar transacción pendiente
    PendingTransaction pendingTransaction = new PendingTransaction(
      fromWallet, toPhoneNumber, paymentAmount, description, LocalDateTime.now(), transactionId
    );
    pendingTransactions.put(inquiryId, pendingTransaction);

    // Enviar consulta por Kafka y retornar el transactionId
    return eventProducer.sendCreditBalanceInquiry(inquiryEvent)
      .thenReturn(transactionId);
  }

  /**
   * Procesa la respuesta de validación de balance de crédito.
   *
   * <p>Este método es llamado cuando se recibe una respuesta del sistema de crédito
   * sobre la validación de balance para una transacción pendiente.</p>
   *
   * @param responseEvent Evento de respuesta con el resultado de la validación
   * @return Mono que completa cuando se procesa la respuesta
   */
  public Mono<Void> processCreditBalanceResponse(CreditBalanceResponseEvent responseEvent) {
    return Mono.fromRunnable(() -> {
      String inquiryId = responseEvent.getInquiryId();
      PendingTransaction pendingTransaction = pendingTransactions.remove(inquiryId);

      if (pendingTransaction == null) {
        log.warn("No pending transaction found for inquiryId: {}", inquiryId);
        return;
      }

      if (responseEvent.getIsValid()) {
        // Si la validación es exitosa, proceder con el pago
        proceedWithPayment(
          pendingTransaction.getFromWallet(),
          pendingTransaction.getToPhoneNumber(),
          pendingTransaction.getAmount(),
          pendingTransaction.getDescription()
        ).subscribe(transaction -> {
          log.info("Payment completed after credit validation - TransactionId: {}",
            transaction.getId());
          // Aquí podrías notificar al usuario que el pago se completó
        });
      } else {
        log.warn("Credit validation failed for inquiryId: {} - Reason: {}",
          inquiryId, responseEvent.getReason());
        // Aquí podrías actualizar la transacción a FAILED en la base de datos
        // y notificar al usuario sobre el fallo
      }
    });
  }

  private Mono<Transaction> proceedWithPayment(YankiWallet fromWallet, String toPhoneNumber,
                                               BigDecimal paymentAmount, String description) {
    return findByPhoneNumber(toPhoneNumber)
      .flatMap(toWallet -> {
        // Verificar si tienen tarjeta
        boolean fromWalletHasNoCard = fromWallet.getAssociatedCreditId() == null ||
          fromWallet.getAssociatedCreditId().isEmpty();
        boolean toWalletHasNoCard = toWallet.getAssociatedCreditId() == null ||
          toWallet.getAssociatedCreditId().isEmpty();

        // LÓGICA CORREGIDA:
        // FROM solo resta si NO tiene tarjeta
        if (fromWalletHasNoCard) {
          fromWallet.updateBalance(paymentAmount, false);
        }

        // TO solo suma si NO tiene tarjeta
        if (toWalletHasNoCard) {
          toWallet.updateBalance(paymentAmount, true);
        }

        // Crear transacción
        Transaction transaction = Transaction.builder()
          .id(UUID.randomUUID().toString())
          .amount(paymentAmount)
          .fromWalletId(fromWallet.getId())
          .toWalletId(toWallet.getId())
          .fromPhoneNumber(fromWallet.getPhoneNumber())
          .toPhoneNumber(toPhoneNumber)
          .description(description)
          .transactionDate(LocalDateTime.now())
          .status(TransactionStatusEnum.COMPLETED)
          .build();

        // Guardar wallets actualizados
        List<Mono<YankiWallet>> saveOperations = new ArrayList<>();

        if (fromWalletHasNoCard) {
          saveOperations.add(walletRepository.save(fromWallet));
        }
        if (toWalletHasNoCard) {
          saveOperations.add(walletRepository.save(toWallet));
        }

        return Mono.when(saveOperations)
          .then(Mono.defer(() -> {
            // SOLO insertar en yanki_transactions si el destinatario NO tiene tarjeta
            if (toWalletHasNoCard) {
              YankiTransaction yankiTransaction = YankiTransaction.builder()
                .id(UUID.randomUUID().toString())
                .transactionId(transaction.getId())
                .amount(paymentAmount)
                .fromWalletId(fromWallet.getId())
                .toWalletId(toWallet.getId())
                .fromPhoneNumber(fromWallet.getPhoneNumber())
                .toPhoneNumber(toPhoneNumber)
                .description(description)
                .type(YankiTransaction.TransactionType.TRANSFER)
                .status(TransactionStatusEnum.COMPLETED)
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

              return yankiTransactionRepository.save(yankiTransaction)
                .then(sendKafkaEvents(fromWallet, toWallet, paymentAmount, description))
                .thenReturn(transaction);
            } else {
              return sendKafkaEvents(fromWallet, toWallet, paymentAmount, description)
                .thenReturn(transaction);
            }
          }));
      });
  }

}