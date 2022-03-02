package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.web.model.PaymentVirtualAccountDto;

@DecoratedWith(PaymentVirtualAccountMapperDecorator.class)
@Mapper
public interface PaymentVirtualAccountMapper {
        PaymentVirtualAccount paymentVirtualAccountDtoToPaymentVirtualAccount(
                        PaymentVirtualAccountDto paymentVirtualAccountDto);

        PaymentVirtualAccountDto paymentVirtualAccountToPaymentVirtualAccountDto(
                        PaymentVirtualAccount paymentVirtualAccount, boolean withPaymentService,
                        boolean withPaymentInstructions);
}
