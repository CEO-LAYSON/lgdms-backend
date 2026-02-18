package com.crn.lgdms.modules.credit.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.credit.domain.entity.CreditAccount;
import com.crn.lgdms.modules.credit.dto.response.CreditAccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CreditAccountMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "locationName", source = "location.name")
    CreditAccountResponse toResponse(CreditAccount account);
}
