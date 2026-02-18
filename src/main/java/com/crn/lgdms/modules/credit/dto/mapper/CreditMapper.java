package com.crn.lgdms.modules.credit.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.credit.domain.entity.CreditTransaction;
import com.crn.lgdms.modules.credit.dto.response.CreditTransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CreditMapper {

    @Mapping(target = "accountNumber", source = "creditAccount.accountNumber")
    @Mapping(target = "customerName", source = "creditAccount.customer.name")
    @Mapping(target = "locationName", source = "creditAccount.location.name")
    @Mapping(target = "saleInvoice", source = "sale.invoiceNumber")
    @Mapping(target = "paymentNumber", source = "payment.paymentNumber")
    CreditTransactionResponse toResponse(CreditTransaction transaction);
}
