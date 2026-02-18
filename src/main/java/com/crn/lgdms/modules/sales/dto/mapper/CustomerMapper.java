package com.crn.lgdms.modules.sales.dto.mapper;

import com.crn.lgdms.common.mapping.MapperConfig;
import com.crn.lgdms.modules.sales.domain.entity.Customer;
import com.crn.lgdms.modules.sales.dto.request.CreateCustomerRequest;
import com.crn.lgdms.modules.sales.dto.request.UpdateCustomerRequest;
import com.crn.lgdms.modules.sales.dto.response.CustomerResponse;
import org.mapstruct.*;

@Mapper(config = MapperConfig.class)
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customerNumber", ignore = true)
    @Mapping(target = "currentBalance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(UpdateCustomerRequest request, @MappingTarget Customer customer);

    CustomerResponse toResponse(Customer customer);
}
