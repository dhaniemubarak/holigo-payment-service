package id.holigo.services.holigopaymentservice.web.mappers;

import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;
import id.holigo.services.holigopaymentservice.web.model.PaymentDigitalWalletDto;
import org.mapstruct.Mapper;

@Mapper(uses = PaymentServiceMapperDecorator.class)
public interface PaymentDigitalWalletMapper {
    PaymentDigitalWalletDto paymentDigitalWalletToPaymentDigitalWalletDto(PaymentDigitalWallet paymentDigitalWallet);

}
