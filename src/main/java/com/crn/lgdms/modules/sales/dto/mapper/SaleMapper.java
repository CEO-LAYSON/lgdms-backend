package com.crn.lgdms.modules.sales.dto.mapper;

import com.crn.lgdms.common.mapping.BaseMapperConfig;
import com.crn.lgdms.modules.sales.domain.entity.Sale;
import com.crn.lgdms.modules.sales.dto.response.SaleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.stream.Collectors;

@Mapper(config = BaseMapperConfig.class, uses = {SaleItemMapper.class, CustomerMapper.class})
public interface SaleMapper {

    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "salesPersonId", source = "salesPerson.id")
    @Mapping(target = "salesPersonName", source = "salesPerson.username")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "paymentMethods", expression = "java(getPaymentMethods(sale))")
    SaleResponse toResponse(Sale sale);

    @Named("getPaymentMethods")
    default String getPaymentMethods(Sale sale) {
        if (sale.getPayments() == null || sale.getPayments().isEmpty()) {
            return sale.getPaymentMethod() != null ? sale.getPaymentMethod().toString() : "";
        }
        return sale.getPayments().stream()
            .map(p -> p.getPaymentMethod().toString())
            .collect(Collectors.joining(", "));
    }
}
