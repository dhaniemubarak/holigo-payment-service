package id.holigo.services.holigopaymentservice.web.mappers;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.web.model.PaymentBankTransferDto;

@DecoratedWith(PaymentBankTransferMapperDecorator.class)
@Mapper
public interface PaymentBankTransferMapper {

    PaymentBankTransferDto paymentBankTransferToPaymentBankTransferDto(PaymentBankTransfer paymentBankTransfer);
}
