package id.holigo.services.holigopaymentservice.services;

import org.springframework.statemachine.StateMachine;

import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.VirtualAccountCallback;
import id.holigo.services.holigopaymentservice.events.VirtualAccountStatusEvent;

public interface VirtualAccountCallbackService {
    VirtualAccountCallback newVirtualAccount(VirtualAccountCallback virtualAccountCallback);

    StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> findTransaction(Long virtualAccountCallbackId);

    StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> transactionNotFound(
            Long virtualAccountCallbackId);

    StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> processIssuedTransaction(
            Long virtualAccountCallbackId);

    StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> issuedTransaction(Long virtualAccountCallbackId);

    StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> failedTransaction(Long virtualAccountCallbackId);
}
