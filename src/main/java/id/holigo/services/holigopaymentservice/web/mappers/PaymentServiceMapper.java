package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.common.model.PaymentServiceDto;
import id.holigo.services.holigopaymentservice.domain.PaymentService;

@DecoratedWith(PaymentServiceMapperDecorator.class)
@Mapper
public interface PaymentServiceMapper {

    PaymentService paymentServiceDtoToPaymentService(PaymentServiceDto paymentServiceDto);

    PaymentServiceDto paymentServiceToPaymentServiceDto(PaymentService paymentService);
}
