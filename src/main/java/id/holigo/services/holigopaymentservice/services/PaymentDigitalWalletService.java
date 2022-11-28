package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;
import id.holigo.services.holigopaymentservice.events.PaymentDigitalWalletEvent;
import org.springframework.statemachine.StateMachine;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface PaymentDigitalWalletService {
    PaymentDigitalWallet createPaymentDigitalWallet(TransactionDto transactionDto, Payment payment);

    StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> paymentHasBeenPaid(UUID id);

    StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> cancelPayment(UUID id);

    void paymentHasBeenExpired(UUID id);
}
