package id.holigo.services.holigopaymentservice.services;

import java.util.UUID;

import org.springframework.statemachine.StateMachine;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.common.model.TransactionDto;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.events.PaymentBankTransferEvent;

public interface PaymentBankTransferService {

    PaymentBankTransfer createNewBankTransfer(TransactionDto transactionDto, Payment payment);

    StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> paymentHasBeenPaid(UUID id);

    StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> cancelPayment(UUID id);

    void paymentHasBeenExpired(UUID id);
}
