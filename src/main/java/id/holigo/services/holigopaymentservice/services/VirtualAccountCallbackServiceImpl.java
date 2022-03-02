package id.holigo.services.holigopaymentservice.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.VirtualAccountCallback;
import id.holigo.services.holigopaymentservice.repositories.VirtualAccountCallbackRepository;
import id.holigo.services.holigopaymentservice.events.VirtualAccountStatusEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VirtualAccountCallbackServiceImpl implements VirtualAccountCallbackService {

    public static final String VIRTUAL_ACCOUNT_CALLBACK_HEADER = "virtual_account_callback_id";

    @Autowired
    private final VirtualAccountCallbackRepository virtualAccountCallbackRepository;

    private final StateMachineFactory<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> stateMachineFactory;

    private final VirtualAccountCallbackInterceptor virtualAccountCallbackInterceptor;

    @Override
    public VirtualAccountCallback newVirtualAccount(VirtualAccountCallback virtualAccountCallback) {
        virtualAccountCallback.setProcessStatus(PaymentCallbackStatusEnum.RECEIVED);
        VirtualAccountCallback savedVirtualAccountCallback = virtualAccountCallbackRepository
                .save(virtualAccountCallback);
        findTransaction(savedVirtualAccountCallback.getId());
        return savedVirtualAccountCallback;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> findTransaction(
            Long virtualAccountCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm = build(virtualAccountCallbackId);
        sendEvent(virtualAccountCallbackId, sm, VirtualAccountStatusEvent.FIND_TRANSACTION);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> transactionNotFound(
            Long virtualAccountCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm = build(virtualAccountCallbackId);
        sendEvent(virtualAccountCallbackId, sm, VirtualAccountStatusEvent.TRANSACTION_NOT_FOUND);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> processIssuedTransaction(
            Long virtualAccountCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm = build(virtualAccountCallbackId);
        sendEvent(virtualAccountCallbackId, sm, VirtualAccountStatusEvent.PROCESS_ISSUED);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> issuedTransaction(
            Long virtualAccountCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm = build(virtualAccountCallbackId);
        sendEvent(virtualAccountCallbackId, sm, VirtualAccountStatusEvent.ISSUED);
        return sm;
    }

    @Transactional
    @Override
    public StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> failedTransaction(
            Long virtualAccountCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm = build(virtualAccountCallbackId);
        sendEvent(virtualAccountCallbackId, sm, VirtualAccountStatusEvent.ISSUED_FAILED);
        return sm;
    }

    private void sendEvent(Long bankTransferCallbackId,
            StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm, VirtualAccountStatusEvent event) {
        Message<VirtualAccountStatusEvent> message = MessageBuilder.withPayload(event)
                .setHeader(VIRTUAL_ACCOUNT_CALLBACK_HEADER, bankTransferCallbackId).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> build(Long virtualAccountCallbackId) {
        VirtualAccountCallback virtualAccountCallback = virtualAccountCallbackRepository
                .getById(virtualAccountCallbackId);

        StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm = stateMachineFactory
                .getStateMachine(Long.toString(virtualAccountCallback.getId()));

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(virtualAccountCallbackInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<PaymentCallbackStatusEnum, VirtualAccountStatusEvent>(
                    virtualAccountCallback.getProcessStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }
}
