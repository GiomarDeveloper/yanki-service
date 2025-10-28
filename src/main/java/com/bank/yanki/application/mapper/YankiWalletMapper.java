package com.bank.yanki.application.mapper;

import com.bank.yanki.domain.model.Transaction;
import com.bank.yanki.domain.model.YankiWallet;
import com.bank.yanki.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface YankiWalletMapper {

    // Mapeo de Request a Dominio
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "associatedCreditId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    YankiWallet toDomain(YankiWalletRequest request);

    // Mapeo de Dominio a Response
    @Mapping(target = "associatedCreditId", source = "associatedCreditId")
    YankiWalletResponse toResponse(YankiWallet wallet);

    // Mapeo para Card Association Response
    @Mapping(target = "id", source = "wallet.id")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "creditId", source = "wallet.associatedCreditId")
    @Mapping(target = "status", expression = "java(mapCardStatus(wallet))")
    @Mapping(target = "associatedAt", expression = "java(toOffsetDateTime(wallet.getUpdatedAt()))")
    CardAssociationResponse toCardAssociationResponse(YankiWallet wallet);

    @Mapping(target = "amount", expression = "java(toDouble(transaction.getAmount()))")
    @Mapping(target = "transactionType", expression = "java(com.bank.yanki.model.TransactionTypeEnum.SEND)")
    @Mapping(target = "status", expression = "java(com.bank.yanki.model.TransactionStatusEnum.COMPLETED)")
    @Mapping(target = "transactionDate", expression = "java(toOffsetDateTime(transaction.getTransactionDate()))")
    TransactionResponse toTransactionResponse(Transaction transaction);

    default CardAssociationStatusEnum mapCardStatus(YankiWallet wallet) {
        return wallet.getAssociatedCreditId() != null ?
                CardAssociationStatusEnum.ACTIVE : CardAssociationStatusEnum.PENDING;
    }

    default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
    }

    default LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }

    // Mapeo de enums
    default YankiWallet.DocumentType toDomainDocumentType(DocumentTypeEnum documentType) {
        return documentType != null ? YankiWallet.DocumentType.valueOf(documentType.name()) : null;
    }

    default DocumentTypeEnum toResponseDocumentType(YankiWallet.DocumentType documentType) {
        return documentType != null ? DocumentTypeEnum.valueOf(documentType.name()) : null;
    }

    default WalletStatusEnum toResponseStatus(YankiWallet.YankiWalletStatus status) {
        return status != null ? WalletStatusEnum.valueOf(status.name()) : null;
    }

    default Double toDouble(BigDecimal amount) {
        return amount != null ? amount.doubleValue() : null;
    }

    default BigDecimal toBigDecimal(Double amount) {
        return amount != null ? BigDecimal.valueOf(amount) : null;
    }
}