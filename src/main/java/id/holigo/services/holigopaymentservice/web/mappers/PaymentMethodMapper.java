package id.holigo.services.holigopaymentservice.web.mappers;

import id.holigo.services.common.model.TransactionDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.PaymentMethod;
import id.holigo.services.holigopaymentservice.web.model.PaymentMethodDto;
import org.mapstruct.Mapping;

import java.util.UUID;

@DecoratedWith(PaymentMethodMapperDecorator.class)
@Mapper
public interface PaymentMethodMapper {
    @Mapping(target = "id", source = "paymentMethod.id")
    @Mapping(target = "name", ignore = true)
    PaymentMethodDto paymentMethodToPaymentMethodDto(PaymentMethod paymentMethod, TransactionDto transactionDto);

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isShow", ignore = true)
    @Mapping(target = "indexName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PaymentMethod paymentMethodDtoToPaymentMethod(PaymentMethodDto paymentMethodDto);
}
