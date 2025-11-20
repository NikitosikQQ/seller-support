package ru.seller_support.assignment.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeActivityHistoryEntity;
import ru.seller_support.assignment.adapter.postgres.entity.employee.EmployeeProcessedCapacityEntity;
import ru.seller_support.assignment.controller.dto.response.EmployeeCapacityDto;
import ru.seller_support.assignment.controller.dto.response.EmployeeCapacityDtoResponse;
import ru.seller_support.assignment.domain.enums.CapacityOperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeProcessedCapacityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "operationType", source = "operationType")
    @Mapping(target = "capacity", source = "currentCapacity")
    EmployeeActivityHistoryEntity toEmployeeActivityHistory(EmployeeProcessedCapacityEntity employeeProcessedCapacityEntity,
                                                            CapacityOperationType operationType,
                                                            BigDecimal currentCapacity,
                                                            BigDecimal amount,
                                                            LocalDateTime createdAt);


    EmployeeCapacityDto toDto(EmployeeProcessedCapacityEntity entity);

    @Mapping(target = "workplace", source = "workplace.value")
    EmployeeCapacityDtoResponse toResponse(EmployeeCapacityDto dto);

}
