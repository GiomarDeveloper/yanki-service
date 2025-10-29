package com.bank.yanki.domain.repository;

import com.bank.yanki.domain.model.YankiTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la gestión de transacciones Yanki en MongoDB.
 *
 * <p>Este repositorio proporciona operaciones de acceso a datos reactivas para
 * la entidad {@link YankiTransaction}. Extiende {@link ReactiveMongoRepository}
 * para obtener operaciones CRUD básicas y define métodos personalizados para
 * consultas específicas del dominio de transacciones Yanki.</p>
 *
 */
public interface YankiTransactionRepository
  extends ReactiveMongoRepository<YankiTransaction, String> {

  /**
   * Busca todas las transacciones donde el número de teléfono especificado es el remitente.
   *
   * @param fromPhoneNumber número de teléfono del remitente
   * @return un {@link Flux} que emite todas las transacciones enviadas por ese teléfono
   */
  Flux<YankiTransaction> findByFromPhoneNumber(String fromPhoneNumber);

  /**
   * Busca todas las transacciones donde el número de teléfono especificado es el destinatario.
   *
   * @param toPhoneNumber número de teléfono del destinatario
   * @return un {@link Flux} que emite todas las transacciones recibidas por ese teléfono
   */
  Flux<YankiTransaction> findByToPhoneNumber(String toPhoneNumber);

  /**
   * Busca todas las transacciones originadas desde una billetera específica.
   *
   * @param fromWalletId identificador único de la billetera de origen
   * @return un {@link Flux} que emite todas las transacciones enviadas desde esa billetera
   */
  Flux<YankiTransaction> findByFromWalletId(String fromWalletId);

  /**
   * Busca todas las transacciones destinadas a una billetera específica.
   *
   * @param toWalletId identificador único de la billetera de destino
   * @return un {@link Flux} que emite todas las transacciones recibidas por esa billetera
   */
  Flux<YankiTransaction> findByToWalletId(String toWalletId);

  /**
   * Busca una transacción por su identificador único de negocio.
   *
   * @param transactionId identificador único de la transacción (no el ID de MongoDB)
   * @return un {@link Mono} que contiene la transacción si existe, o Mono.empty() si no se encuentra
   */
  Mono<YankiTransaction> findByTransactionId(String transactionId);
}