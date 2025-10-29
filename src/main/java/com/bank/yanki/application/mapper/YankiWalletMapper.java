package com.bank.yanki.application.mapper;

import com.bank.yanki.domain.model.Transaction;
import com.bank.yanki.domain.model.YankiWallet;
import com.bank.yanki.model.CardAssociationResponse;
import com.bank.yanki.model.CardAssociationStatusEnum;
import com.bank.yanki.model.DocumentTypeEnum;
import com.bank.yanki.model.TransactionResponse;
import com.bank.yanki.model.WalletStatusEnum;
import com.bank.yanki.model.YankiWalletRequest;
import com.bank.yanki.model.YankiWalletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para conversión entre entidades, DTOs y modelos de dominio del servicio Yanki.
 *
 * <p>Este mapper utiliza MapStruct para generar automáticamente el código de conversión
 * entre las diferentes representaciones de datos en el sistema Yanki. Proporciona
 * métodos para mapear entre:</p>
 *
 */
@Mapper(componentModel = "spring")
public interface YankiWalletMapper {

  /**
   * Convierte un YankiWalletRequest a un modelo de dominio YankiWallet.
   *
   * <p>Ignora los campos que son generados automáticamente por el sistema:
   * <ul>
   *   <li>id: Generado por la base de datos</li>
   *   <li>associatedCreditId: Asignado posteriormente</li>
   *   <li>status: Asignado por el sistema</li>
   *   <li>createdAt/updatedAt: Timestamps automáticos</li>
   * </ul>
   * </p>
   *
   * @param request DTO de solicitud de creación de billetera
   * @return modelo de dominio YankiWallet
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "associatedCreditId", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  YankiWallet toDomain(YankiWalletRequest request);

  /**
   * Convierte un modelo de dominio YankiWallet a un DTO de respuesta.
   *
   * @param wallet modelo de dominio de billetera
   * @return DTO de respuesta de billetera
   */
  @Mapping(target = "associatedCreditId", source = "associatedCreditId")
  YankiWalletResponse toResponse(YankiWallet wallet);

  /**
   * Convierte un YankiWallet a un CardAssociationResponse especializado.
   *
   * <p>Este mapeo se utiliza específicamente para respuestas de asociación
   * de tarjetas, incluyendo el estado de la asociación y timestamps.</p>
   *
   * @param wallet modelo de dominio de billetera
   * @return DTO de respuesta de asociación de tarjeta
   */
  @Mapping(target = "id", source = "wallet.id")
  @Mapping(target = "walletId", source = "wallet.id")
  @Mapping(target = "creditId", source = "wallet.associatedCreditId")
  @Mapping(target = "status", expression = "java(mapCardStatus(wallet))")
  @Mapping(target = "associatedAt", expression = "java(toOffsetDateTime(wallet.getUpdatedAt()))")
  CardAssociationResponse toCardAssociationResponse(YankiWallet wallet);

  /**
   * Convierte una Transaction de dominio a un TransactionResponse.
   *
   * <p>Incluye conversiones de tipos y asignación de valores por defecto:
   * <ul>
   *   <li>BigDecimal amount → Double amount</li>
   *   <li>TransactionType por defecto: SEND</li>
   *   <li>TransactionStatus por defecto: COMPLETED</li>
   *   <li>LocalDateTime → OffsetDateTime</li>
   * </ul>
   * </p>
   *
   * @param transaction modelo de dominio de transacción
   * @return DTO de respuesta de transacción
   */
  @Mapping(target = "amount", expression = "java(toDouble(transaction.getAmount()))")
  @Mapping(target = "transactionType", expression = "java(com.bank.yanki.model.TransactionTypeEnum.SEND)")
  @Mapping(target = "status", expression = "java(com.bank.yanki.model.TransactionStatusEnum.COMPLETED)")
  @Mapping(target = "transactionDate", expression = "java(toOffsetDateTime(transaction.getTransactionDate()))")
  TransactionResponse toTransactionResponse(Transaction transaction);

  /**
   * Determina el estado de asociación de tarjeta basado en la billetera.
   *
   * @param wallet billetera a evaluar
   * @return ACTIVE si tiene tarjeta asociada, PENDING en caso contrario
   */
  default CardAssociationStatusEnum mapCardStatus(YankiWallet wallet) {
    return wallet.getAssociatedCreditId() != null ?
      CardAssociationStatusEnum.ACTIVE : CardAssociationStatusEnum.PENDING;
  }

  /**
   * Convierte LocalDateTime a OffsetDateTime con zona horaria UTC.
   *
   * @param localDateTime fecha/hora local a convertir
   * @return OffsetDateTime en UTC, o null si la entrada es null
   */
  default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
    return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
  }

  /**
   * Convierte OffsetDateTime a LocalDateTime.
   *
   * @param offsetDateTime fecha/hora con offset a convertir
   * @return LocalDateTime, o null si la entrada es null
   */
  default LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
    return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
  }

  /**
   * Convierte DocumentTypeEnum a DocumentType del dominio.
   *
   * @param documentType enum de la capa externa
   * @return enum del dominio, o null si la entrada es null
   */
  default YankiWallet.DocumentType toDomainDocumentType(DocumentTypeEnum documentType) {
    return documentType != null ? YankiWallet.DocumentType.valueOf(documentType.name()) : null;
  }

  /**
   * Convierte DocumentType del dominio a DocumentTypeEnum.
   *
   * @param documentType enum del dominio
   * @return enum de la capa externa, o null si la entrada es null
   */
  default DocumentTypeEnum toResponseDocumentType(YankiWallet.DocumentType documentType) {
    return documentType != null ? DocumentTypeEnum.valueOf(documentType.name()) : null;
  }

  /**
   * Convierte YankiWalletStatus del dominio a WalletStatusEnum.
   *
   * @param status estado del dominio
   * @return enum de la capa externa, o null si la entrada es null
   */
  default WalletStatusEnum toResponseStatus(YankiWallet.YankiWalletStatus status) {
    return status != null ? WalletStatusEnum.valueOf(status.name()) : null;
  }

  /**
   * Convierte BigDecimal a Double.
   *
   * @param amount monto en BigDecimal
   * @return monto en Double, o null si la entrada es null
   */
  default Double toDouble(BigDecimal amount) {
    return amount != null ? amount.doubleValue() : null;
  }

  /**
   * Convierte Double a BigDecimal.
   *
   * @param amount monto en Double
   * @return monto en BigDecimal, o null si la entrada es null
   */
  default BigDecimal toBigDecimal(Double amount) {
    return amount != null ? BigDecimal.valueOf(amount) : null;
  }
}