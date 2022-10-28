package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;

public interface PaymentDigitalWalletService {
    PaymentDigitalWallet createPaymentDigitalWallet(TransactionDto transactionDto, Payment payment);
}
