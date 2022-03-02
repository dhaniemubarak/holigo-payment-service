package id.holigo.services.holigopaymentservice.services;

import org.springframework.statemachine.StateMachine;

import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.events.BankTransferStatusEvent;

public interface BankTransferCallbackService {

    BankTransferCallback newBankTransfer(BankTransferCallback bankTransferCallback);

    StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> findTransaction(Long bankTransferCallbackId);

    StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> transactionNotFound(Long bankTransferCallbackId);

    StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> processIssuedTransaction(Long bankTransferCallbackId);

    StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> issuedTransaction(Long bankTransferCallbackId);

    StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> failedTransaction(Long bankTransferCallbackId);
}
