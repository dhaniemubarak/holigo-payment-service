package id.holigo.services.holigopaymentservice.services;

import javax.transaction.Transactional;

import id.holigo.services.holigopaymentservice.interceptors.VirtualAccountCallbackInterceptor;
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

    private final VirtualAccountCallbackRepository virtualAccountCallbackRepository;

    private final StateMachineFactory<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> stateMachineFactory;

    private final VirtualAccountCallbackInterceptor virtualAccountCallbackInterceptor;

    @Override
    public void newVirtualAccount(VirtualAccountCallback virtualAccountCallback) {
        virtualAccountCallback.setProcessStatus(PaymentCallbackStatusEnum.RECEIVED);
        VirtualAccountCallback savedVirtualAccountCallback = virtualAccountCallbackRepository
                .save(virtualAccountCallback);
        findTransaction(savedVirtualAccountCallback.getId());
    }

    @Transactional
    @Override
    public void findTransaction(
            Long virtualAccountCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm = build(virtualAccountCallbackId);
        sendEvent(virtualAccountCallbackId, sm, VirtualAccountStatusEvent.FIND_TRANSACTION);
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
    public void failedTransaction(
            Long virtualAccountCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> sm = build(virtualAccountCallbackId);
        sendEvent(virtualAccountCallbackId, sm, VirtualAccountStatusEvent.ISSUED_FAILED);
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
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                    virtualAccountCallback.getProcessStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }
}
