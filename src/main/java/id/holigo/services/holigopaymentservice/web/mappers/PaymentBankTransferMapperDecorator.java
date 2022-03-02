package id.holigo.services.holigopaymentservice.web.mappers;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import id.holigo.services.holigopaymentservice.domain.CompanyBankAccount;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.domain.PaymentService;
import id.holigo.services.holigopaymentservice.repositories.CompanyBankAccountRepository;
import id.holigo.services.holigopaymentservice.web.model.CompanyBankAccountDto;
import id.holigo.services.holigopaymentservice.web.model.PaymentBankTransferDto;
import id.holigo.services.holigopaymentservice.web.model.PaymentServiceDto;

public abstract class PaymentBankTransferMapperDecorator implements PaymentBankTransferMapper {

    @Autowired
    private MessageSource messageSource;

    private PaymentBankTransferMapper paymentBankTransferMapper;

    private PaymentInstructionMapper paymentInstructionMapper;

    private CompanyBankAccountMapper companyBankAccountMapper;

    @Autowired
    private CompanyBankAccountRepository companyBankAccountRepository;

    @Autowired
    public void setPaymentBankTransferMapper(PaymentBankTransferMapper paymentBankTransferMapper) {
        this.paymentBankTransferMapper = paymentBankTransferMapper;
    }

    @Autowired
    public void setCompanyBankAccountMapper(CompanyBankAccountMapper companyBankAccountMapper) {
        this.companyBankAccountMapper = companyBankAccountMapper;
    }

    private PaymentServiceMapper paymentServiceMapper;

    @Autowired
    public void setPaymentServiceMapper(PaymentServiceMapper paymentServiceMapper) {
        this.paymentServiceMapper = paymentServiceMapper;
    }

    @Autowired
    public void setPaymentInstructionMapper(PaymentInstructionMapper paymentInstructionMapper) {
        this.paymentInstructionMapper = paymentInstructionMapper;
    }

    @Override
    public PaymentBankTransferDto paymentBankTransferToPaymentBankTransferDto(PaymentBankTransfer paymentBankTransfer) {
        PaymentBankTransferDto paymentBankTransferDto = paymentBankTransferMapper
                .paymentBankTransferToPaymentBankTransferDto(paymentBankTransfer);

        CompanyBankAccount companyBankAccount = companyBankAccountRepository
                .getByPaymentServiceId(paymentBankTransfer.getPaymentServiceId());
        CompanyBankAccountDto companyBankAccountDto = companyBankAccountMapper
                .companyBankAccountToCompanyBankAccountDto(companyBankAccount);
        companyBankAccountDto
                .setPaymentService(paymentServiceToPaymentServiceDto(companyBankAccount.getPaymentService()));
        ;
        paymentBankTransferDto
                .setBank(companyBankAccountDto);
        paymentBankTransferDto
                .setPaymentInstructions(companyBankAccount.getPaymentService().getPaymentInstructions().stream()
                        .map(paymentInstructionMapper::paymentInstructionToPaymentInstructionDto)
                        .collect(Collectors.toList()));

        return paymentBankTransferDto;
    }

    public PaymentServiceDto paymentServiceToPaymentServiceDto(PaymentService paymentService) {
        PaymentServiceDto paymentServiceDto = paymentServiceMapper.paymentServiceToPaymentServiceDto(paymentService);
        paymentServiceDto.setName(
                messageSource.getMessage(paymentService.getIndexName(), null, LocaleContextHolder.getLocale()));
        return paymentServiceDto;
    }

}
