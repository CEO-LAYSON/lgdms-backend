package com.crn.lgdms.modules.payments.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.payments.domain.entity.PaymentAllocation;
import com.crn.lgdms.modules.payments.dto.response.AllocationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface AllocationMapper {

    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "paymentNumber", source = "payment.paymentNumber")
    @Mapping(target = "saleId", source = "sale.id")
    @Mapping(target = "saleInvoice", source = "sale.invoiceNumber")
    AllocationResponse toResponse(PaymentAllocation allocation);
}
