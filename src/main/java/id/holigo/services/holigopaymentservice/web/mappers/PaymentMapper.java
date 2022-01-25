package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.web.model.PaymentDtoForUser;
import id.holigo.services.holigopaymentservice.web.model.RequestPaymentDto;

@Mapper
public interface PaymentMapper {
    Payment requestPaymentDtoToPayment(RequestPaymentDto requestPaymentDto);

    PaymentDtoForUser paymentToPaymentDtoForUser(Payment payment);
}
