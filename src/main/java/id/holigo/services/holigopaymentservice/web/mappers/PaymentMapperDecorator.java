package id.holigo.services.holigopaymentservice.web.mappers;

import org.springframework.beans.factory.annotation.Autowired;

import id.holigo.services.common.model.PaymentDtoForUser;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.web.model.RequestPaymentDto;

public abstract class PaymentMapperDecorator implements PaymentMapper {

    private PaymentMapper paymentMapper;

    private PaymentServiceMapper paymentServiceMapper;

    @Autowired
    public void setPaymentMapper(PaymentMapper paymentMapper) {
        this.paymentMapper = paymentMapper;
    }

    @Autowired
    public void setPaymentServiceMapper(PaymentServiceMapper paymentServiceMapper) {
        this.paymentServiceMapper = paymentServiceMapper;
    }

    public Payment requestPaymentDtoToPayment(RequestPaymentDto requestPaymentDto) {
        return paymentMapper.requestPaymentDtoToPayment(requestPaymentDto);
    }

    public PaymentDtoForUser paymentToPaymentDtoForUser(Payment payment) {
        PaymentDtoForUser paymentDtoForUser = paymentMapper.paymentToPaymentDtoForUser(payment);
        paymentDtoForUser
                .setPaymentService(paymentServiceMapper.paymentServiceToPaymentServiceDto(payment.getPaymentService(), null));
        return paymentDtoForUser;
    }
}
