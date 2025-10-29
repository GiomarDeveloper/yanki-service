package com.bank.yanki.domain.repository;

import com.bank.yanki.domain.model.YankiWallet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la gestión de billeteras Yanki en MongoDB.
 *
 * <p>Este repositorio proporciona operaciones de acceso a datos reactivas para
 * la entidad {@link YankiWallet}. Incluye métodos para búsqueda por diferentes
 * criterios y validación de existencia de billeteras.</p>
 *
 */
public interface YankiWalletRepository extends ReactiveMongoRepository<YankiWallet, String> {

  /**
   * Busca una billetera Yanki por número de teléfono.
   *
   * @param phoneNumber número de teléfono asociado a la billetera
   * @return un {@link Mono} que contiene la billetera si existe, o Mono.empty() si no se encuentra
   */
  Mono<YankiWallet> findByPhoneNumber(String phoneNumber);

  /**
   * Busca una billetera Yanki por número de documento de identidad.
   *
   * @param documentNumber número de documento de identidad del titular
   * @return un {@link Mono} que contiene la billetera si existe, o Mono.empty() si no se encuentra
   */
  Mono<YankiWallet> findByDocumentNumber(String documentNumber);

  /**
   * Verifica si existe una billetera con el número de teléfono especificado.
   *
   * @param phoneNumber número de teléfono a verificar
   * @return un {@link Mono} que emite true si existe, false en caso contrario
   */
  Mono<Boolean> existsByPhoneNumber(String phoneNumber);

  /**
   * Verifica si existe una billetera con el número de documento especificado.
   *
   * @param documentNumber número de documento a verificar
   * @return un {@link Mono} que emite true si existe, false en caso contrario
   */
  Mono<Boolean> existsByDocumentNumber(String documentNumber);
}