package id.holigo.services.holigopaymentservice.web.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import id.holigo.services.holigopaymentservice.domain.PaymentInstruction;
import id.holigo.services.holigopaymentservice.web.model.PaymentInstructionDto;

public abstract class PaymentInstructionMapperDecorator implements PaymentInstructionMapper {
    @Autowired
    private MessageSource messageSource;

    private PaymentInstructionMapper paymentInstructionMapper;

    @Autowired
    public void setPaymentInstructionMapper(PaymentInstructionMapper paymentInstructionMapper) {
        this.paymentInstructionMapper = paymentInstructionMapper;
    }

    @Override
    public PaymentInstructionDto paymentInstructionToPaymentInstructionDto(PaymentInstruction paymentInstruction) {
        PaymentInstructionDto paymentInstructionDto = paymentInstructionMapper
                .paymentInstructionToPaymentInstructionDto(paymentInstruction);
        try {
            paymentInstructionDto
                    .setDescription(messageSource.getMessage(paymentInstruction.getIndexDescription(), null,
                            LocaleContextHolder.getLocale()));
        } catch (Exception e) {
        }

        return paymentInstructionDto;
    }
}
