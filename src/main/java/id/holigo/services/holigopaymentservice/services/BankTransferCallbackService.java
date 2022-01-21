package id.holigo.services.holigopaymentservice.services;

import org.springframework.statemachine.StateMachine;

import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.domain.BankTransferStatusEnum;
import id.holigo.services.holigopaymentservice.events.BankTransferStatusEvent;

public interface BankTransferCallbackService {

    BankTransferCallback newBankTransfer(BankTransferCallback bankTransferCallback);

    StateMachine<BankTransferStatusEnum, BankTransferStatusEvent> findTransaction(Long bankTransferCallbackId);

    StateMachine<BankTransferStatusEnum, BankTransferStatusEvent> transactionNotFound(Long bankTransferCallbackId);

    StateMachine<BankTransferStatusEnum, BankTransferStatusEvent> processIssuedTransaction(Long bankTransferCallbackId);

    StateMachine<BankTransferStatusEnum, BankTransferStatusEvent> issuedTransaction(Long bankTransferCallbackId);

    StateMachine<BankTransferStatusEnum, BankTransferStatusEvent> failedTransaction(Long bankTransferCallbackId);
}
