package com.bank.yanki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Clase principal de la aplicación del servicio Yanki.
 *
 * <p>Yanki es un servicio de billetera digital que permite:
 * <ul>
 *   <li>Gestión de billeteras digitales</li>
 *   <li>Transferencias entre usuarios</li>
 *   <li>Asociación de cuentas bancarias</li>
 *   <li>Transacciones con tarjetas de crédito</li>
 *   <li>Consulta de saldos y movimientos</li>
 * </ul>
 * </p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class YankiServiceApplication {

  /**
   * Método principal que inicia la aplicación Spring Boot.
   *
   * @param args argumentos de línea de comandos
   */
  public static void main(String[] args) {
    SpringApplication.run(YankiServiceApplication.class, args);
  }

}
