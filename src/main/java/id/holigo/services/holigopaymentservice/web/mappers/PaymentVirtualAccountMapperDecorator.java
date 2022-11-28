package id.holigo.services.holigopaymentservice.web.mappers;

import java.util.stream.Collectors;

import id.holigo.services.common.model.PaymentServiceDto;
import org.springframework.beans.factory.annotation.Autowired;

import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.web.model.PaymentVirtualAccountDto;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public abstract class PaymentVirtualAccountMapperDecorator implements PaymentVirtualAccountMapper {

    private MessageSource messageSource;

    private PaymentVirtualAccountMapper paymentVirtualAccountMapper;

    private PaymentInstructionMapper paymentInstructionMapper;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Autowired
    public void setPaymentVirtualAccountMapper(PaymentVirtualAccountMapper paymentVirtualAccountMapper) {
        this.paymentVirtualAccountMapper = paymentVirtualAccountMapper;
    }

    @Autowired
    public void setPaymentInstructionMapper(PaymentInstructionMapper paymentInstructionMapper) {
        this.paymentInstructionMapper = paymentInstructionMapper;
    }

    @Override
    public PaymentVirtualAccount paymentVirtualAccountDtoToPaymentVirtualAccount(
            PaymentVirtualAccountDto paymentVirtualAccountDto) {
        return paymentVirtualAccountMapper.paymentVirtualAccountDtoToPaymentVirtualAccount(paymentVirtualAccountDto);
    }

    @Override
    public PaymentVirtualAccountDto paymentVirtualAccountToPaymentVirtualAccountDto(
            PaymentVirtualAccount paymentVirtualAccount, boolean withPaymentService, boolean withPaymentInstructions) {
        PaymentVirtualAccountDto paymentVirtualAccountDto = paymentVirtualAccountMapper
                .paymentVirtualAccountToPaymentVirtualAccountDto(paymentVirtualAccount, withPaymentService,
                        withPaymentInstructions);
        paymentVirtualAccountDto.setName(messageSource.getMessage(paymentVirtualAccount.getPaymentService().getIndexName()
                , null, LocaleContextHolder.getLocale()));
        PaymentServiceDto paymentServiceDto = paymentVirtualAccountDto.getPaymentService();
        paymentServiceDto.setName(paymentVirtualAccountDto.getName());
        paymentVirtualAccountDto.setPaymentService(paymentServiceDto);
        if (withPaymentInstructions) {
            paymentVirtualAccountDto.setPaymentInstructions(paymentVirtualAccount.getPaymentService()
                    .getPaymentInstructions().stream()
                    .map(paymentInstructionMapper::paymentInstructionToPaymentInstructionDto)
                    .collect(Collectors.toList()));
        }
        return paymentVirtualAccountDto;
    }

}
