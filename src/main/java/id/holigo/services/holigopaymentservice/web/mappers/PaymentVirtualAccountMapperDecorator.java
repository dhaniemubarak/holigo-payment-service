package id.holigo.services.holigopaymentservice.web.mappers;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.web.model.PaymentVirtualAccountDto;

public abstract class PaymentVirtualAccountMapperDecorator implements PaymentVirtualAccountMapper {

    private PaymentVirtualAccountMapper paymentVirtualAccountMapper;

    private PaymentInstructionMapper paymentInstructionMapper;

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

        if (!withPaymentService) {
            paymentVirtualAccountDto.setPaymentService(null);
        }
        if (withPaymentInstructions) {
            paymentVirtualAccountDto.setPaymentInstructions(paymentVirtualAccount.getPaymentService()
                    .getPaymentInstructions().stream()
                    .map(paymentInstructionMapper::paymentInstructionToPaymentInstructionDto)
                    .collect(Collectors.toList()));

        }
        return paymentVirtualAccountDto;
    }

}
