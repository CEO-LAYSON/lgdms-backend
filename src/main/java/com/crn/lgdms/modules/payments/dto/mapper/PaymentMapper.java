package com.crn.lgdms.modules.payments.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.payments.domain.entity.Payment;
import com.crn.lgdms.modules.payments.dto.response.PaymentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface PaymentMapper {

    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "receivedById", source = "receivedBy.id")
    @Mapping(target = "receivedByName", source = "receivedBy.username")
    PaymentResponse toResponse(Payment payment);
}
