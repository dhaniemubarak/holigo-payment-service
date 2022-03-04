package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.common.model.PaymentDtoForUser;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.web.model.RequestPaymentDto;

@DecoratedWith(PaymentMapperDecorator.class)
@Mapper
public interface PaymentMapper {
    Payment requestPaymentDtoToPayment(RequestPaymentDto requestPaymentDto);

    PaymentDtoForUser paymentToPaymentDtoForUser(Payment payment);
}
