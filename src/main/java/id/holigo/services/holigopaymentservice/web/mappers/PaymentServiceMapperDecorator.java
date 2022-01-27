package id.holigo.services.holigopaymentservice.web.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import id.holigo.services.holigopaymentservice.domain.PaymentService;
import id.holigo.services.holigopaymentservice.web.model.PaymentServiceDto;

public abstract class PaymentServiceMapperDecorator implements PaymentServiceMapper {

    @Autowired
    private MessageSource messageSource;

    private PaymentServiceMapper paymentServiceMapper;

    @Autowired
    public void setPaymentServiceMapper(PaymentServiceMapper paymentServiceMapper) {
        this.paymentServiceMapper = paymentServiceMapper;
    }

    @Override
    public PaymentService paymentServiceDtoToPaymentService(PaymentServiceDto paymentServiceDto) {
        return paymentServiceMapper.paymentServiceDtoToPaymentService(paymentServiceDto);
    }

    @Override
    public PaymentServiceDto paymentServiceToPaymentServiceDto(PaymentService paymentService) {
        PaymentServiceDto paymentServiceDto = paymentServiceMapper.paymentServiceToPaymentServiceDto(paymentService);
        paymentServiceDto.setName(
                messageSource.getMessage(paymentService.getIndexName(), null, LocaleContextHolder.getLocale()));
        return paymentServiceDto;
    }
}
