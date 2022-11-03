package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.holigopaymentservice.interceptors.BankTransferCallbackInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.events.BankTransferStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.BankTransferCallbackRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BankTransferCallbackServiceImpl implements BankTransferCallbackService {

    public static final String BANK_TRANSFER_CALLBACK_HEADER = "bank_transfer_callback_id";

    @Autowired
    private final BankTransferCallbackRepository bankTransferCallbackRepository;

    private final BankTransferCallbackInterceptor bankTransferCallbackInterceptor;

    private final StateMachineFactory<PaymentCallbackStatusEnum, BankTransferStatusEvent> stateMachineFactory;

    @Override
    public BankTransferCallback newBankTransfer(BankTransferCallback bankTransferCallback) {
        bankTransferCallback.setProcessStatus(PaymentCallbackStatusEnum.RECEIVED);
        BankTransferCallback savedBankTransferCallback = bankTransferCallbackRepository.save(bankTransferCallback);
        findTransaction(savedBankTransferCallback.getId());
        return savedBankTransferCallback;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> findTransaction(Long bankTransferCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> sm = build(bankTransferCallbackId);
        sendEvent(bankTransferCallbackId, sm, BankTransferStatusEvent.FIND_TRANSACTION);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> transactionNotFound(
            Long bankTransferCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> sm = build(bankTransferCallbackId);
        sendEvent(bankTransferCallbackId, sm, BankTransferStatusEvent.TRANSACTION_NOT_FOUND);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> processIssuedTransaction(
            Long bankTransferCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> sm = build(bankTransferCallbackId);
        sendEvent(bankTransferCallbackId, sm, BankTransferStatusEvent.PROCESS_ISSUED);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> issuedTransaction(
            Long bankTransferCallbackId) {

        StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> sm = build(bankTransferCallbackId);
        sendEvent(bankTransferCallbackId, sm, BankTransferStatusEvent.ISSUED);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> failedTransaction(
            Long bankTransferCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> sm = build(bankTransferCallbackId);
        sendEvent(bankTransferCallbackId, sm, BankTransferStatusEvent.ISSUED_FAILED);
        return sm;
    }

    private void sendEvent(Long bankTransferCallbackId,
            StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> sm, BankTransferStatusEvent event) {
        Message<BankTransferStatusEvent> message = MessageBuilder.withPayload(event)
                .setHeader(BANK_TRANSFER_CALLBACK_HEADER, bankTransferCallbackId).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> build(Long bankTransferCallbackId) {
        BankTransferCallback bankTransferCallback = bankTransferCallbackRepository.getById(bankTransferCallbackId);

        StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> sm = stateMachineFactory
                .getStateMachine(Long.toString(bankTransferCallback.getId()));

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(bankTransferCallbackInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                    bankTransferCallback.getProcessStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }

}
