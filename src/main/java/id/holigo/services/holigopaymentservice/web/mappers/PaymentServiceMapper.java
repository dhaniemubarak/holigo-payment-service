package id.holigo.services.holigopaymentservice.web.mappers;

import id.holigo.services.common.model.TransactionDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.common.model.PaymentServiceDto;
import id.holigo.services.holigopaymentservice.domain.PaymentService;
import org.mapstruct.Mapping;

import java.util.UUID;

@DecoratedWith(PaymentServiceMapperDecorator.class)
@Mapper
public interface PaymentServiceMapper {

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "paymentInstructions", ignore = true)
    @Mapping(target = "isShow", ignore = true)
    @Mapping(target = "indexName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PaymentService paymentServiceDtoToPaymentService(PaymentServiceDto paymentServiceDto);

    @Mapping(target = "note", source = "paymentService.note")
    @Mapping(target = "id", source = "paymentService.id")
    @Mapping(target = "name", ignore = true)
    PaymentServiceDto paymentServiceToPaymentServiceDto(PaymentService paymentService, TransactionDto transactionDto);
}
