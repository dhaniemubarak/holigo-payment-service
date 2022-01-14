package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.PaymentService;
import id.holigo.services.holigopaymentservice.web.model.PaymentServiceDto;

@DecoratedWith(PaymentServiceMapperDecorator.class)
@Mapper
public interface PaymentServiceMapper {

    PaymentService paymentServiceDtoToPaymentService(PaymentServiceDto paymentServiceDto);

    PaymentServiceDto paymentServiceToPaymentServiceDto(PaymentService paymentService);
}
