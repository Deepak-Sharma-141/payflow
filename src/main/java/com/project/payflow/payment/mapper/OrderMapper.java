package com.project.payflow.payment.mapper;

import com.project.payflow.payment.dto.response.OrderResponse;
import com.project.payflow.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderResponse toResponse(OrderRecord orderRecord);
}
