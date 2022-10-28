package id.holigo.services.holigopaymentservice.web.mappers;

import java.util.UUID;
import java.util.stream.Collectors;

import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import id.holigo.services.holigopaymentservice.domain.PaymentMethod;
import id.holigo.services.holigopaymentservice.web.model.PaymentMethodDto;

public abstract class PaymentMethodMapperDecorator implements PaymentMethodMapper {

    @Autowired
    private MessageSource messageSource;

    private PaymentMethodMapper paymentMethodMapper;

    private PaymentServiceMapper paymentServiceMapper;

    @Autowired
    public void setPaymentMethodMapper(PaymentMethodMapper paymentMethodMapper) {
        this.paymentMethodMapper = paymentMethodMapper;
    }

    @Autowired
    public void setPaymentServiceMapper(PaymentServiceMapper paymentServiceMapper) {
        this.paymentServiceMapper = paymentServiceMapper;
    }

    @Override
    public PaymentMethodDto paymentMethodToPaymentMethodDto(PaymentMethod paymentMethod, TransactionDto transactionDto) {
        PaymentMethodDto paymentMethodDto = paymentMethodMapper.paymentMethodToPaymentMethodDto(paymentMethod, transactionDto);
        paymentMethodDto
                .setName(messageSource.getMessage(paymentMethod.getIndexName(), null, LocaleContextHolder.getLocale()));
        paymentMethodDto.setPaymentServices(paymentMethod.getPaymentServices().stream()
                .map(paymentService -> paymentServiceMapper.paymentServiceToPaymentServiceDto(paymentService, transactionDto)).collect(Collectors.toList()));
        return paymentMethodDto;
    }

    @Override
    public PaymentMethod paymentMethodDtoToPaymentMethod(PaymentMethodDto paymentMethodDto) {
        return paymentMethodMapper.paymentMethodDtoToPaymentMethod(paymentMethodDto);
    }
}
