package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.web.model.PaymentVirtualAccountDto;

@Mapper
public interface PaymentVirtualAccountMapper {
    PaymentVirtualAccount paymentVirtualAccountDtoToPaymentVirtualAccount(
            PaymentVirtualAccountDto paymentVirtualAccountDto);

    PaymentVirtualAccountDto paymentVirtualAccountToPaymentVirtualAccountDto(
            PaymentVirtualAccount paymentVirtualAccount);
}
