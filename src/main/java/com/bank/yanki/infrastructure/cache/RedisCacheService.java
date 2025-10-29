package com.bank.yanki.infrastructure.cache;

import com.bank.yanki.domain.model.YankiWallet;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Servicio de caché para billeteras Yanki utilizando Redis.
 *
 * <p>Este servicio proporciona operaciones para almacenar, recuperar y eliminar
 * billeteras del caché Redis con un tiempo de vida (TTL) configurado.</p>
 *
 * <p>Utiliza Reactive Redis Template para operaciones no bloqueantes y
 * serialización JSON de las billeteras.</p>
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

  private static final Duration WALLET_TTL = Duration.ofHours(1);
  private final ReactiveRedisTemplate<String, YankiWallet> redisTemplate;

  /**
   * Almacena una billetera en caché Redis.
   *
   * @param phoneNumber El número de teléfono usado como clave
   * @param wallet La billetera a almacenar
   * @return Mono que emite true si se almacenó correctamente
   */
  public Mono<Boolean> cacheWallet(String phoneNumber, YankiWallet wallet) {
    String key = "wallet:" + phoneNumber;
    return redisTemplate.opsForValue()
      .set(key, wallet, WALLET_TTL)
      .doOnSuccess(result ->
        log.debug("Wallet cached for phone: {}", phoneNumber)
      )
      .doOnError(error ->
        log.error("Error caching wallet: {}", error.getMessage())
      );
  }

  /**
   * Recupera una billetera del caché Redis.
   *
   * @param phoneNumber El número de teléfono usado como clave
   * @return Mono que emite la billetera si existe en caché, o empty si no existe
   */
  public Mono<YankiWallet> getCachedWallet(String phoneNumber) {
    String key = "wallet:" + phoneNumber;
    return redisTemplate.opsForValue()
      .get(key)
      .doOnSuccess(wallet -> {
        if (wallet != null) {
          log.debug("Wallet cache hit for phone: {}", phoneNumber);
        }
      });
  }

  /**
   * Elimina una billetera del caché Redis.
   *
   * @param phoneNumber El número de teléfono usado como clave
   * @return Mono que emite true si se eliminó correctamente
   */
  public Mono<Boolean> evictWalletCache(String phoneNumber) {
    String key = "wallet:" + phoneNumber;
    return redisTemplate.delete(key)
      .map(count -> count > 0)
      .doOnSuccess(result ->
        log.debug("Wallet cache evicted for phone: {}", phoneNumber)
      );
  }
}