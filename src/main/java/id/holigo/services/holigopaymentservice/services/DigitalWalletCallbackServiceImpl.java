package id.holigo.services.holigopaymentservice.services;

import id.holigo.services.holigopaymentservice.domain.DigitalWalletCallback;
import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.events.DigitalWalletStatusEvent;
import id.holigo.services.holigopaymentservice.events.VirtualAccountStatusEvent;
import id.holigo.services.holigopaymentservice.interceptors.DigitalWalletCallbackInterceptor;
import id.holigo.services.holigopaymentservice.repositories.DigitalWalletCallbackRepository;
import id.holigo.services.holigopaymentservice.web.mappers.DigitalWalletCallbackMapper;
import id.holigo.services.holigopaymentservice.web.model.DigitalWalletCallbackDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DigitalWalletCallbackServiceImpl implements DigitalWalletCallbackService {

    public static final String DIGITAL_WALLET_CALLBACK_HEADER = "digital_wallet_callback_id";

    private DigitalWalletCallbackMapper digitalWalletCallbackMapper;

    private DigitalWalletCallbackRepository digitalWalletCallbackRepository;

    private DigitalWalletCallbackInterceptor digitalWalletCallbackInterceptor;

    @Autowired
    public void setDigitalWalletCallbackInterceptor(DigitalWalletCallbackInterceptor digitalWalletCallbackInterceptor) {
        this.digitalWalletCallbackInterceptor = digitalWalletCallbackInterceptor;
    }

    private final StateMachineFactory<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> stateMachineFactory;

    @Autowired
    public void setDigitalWalletCallbackRepository(DigitalWalletCallbackRepository digitalWalletCallbackRepository) {
        this.digitalWalletCallbackRepository = digitalWalletCallbackRepository;
    }

    @Autowired
    public void setDigitalWalletCallbackMapper(DigitalWalletCallbackMapper digitalWalletCallbackMapper) {
        this.digitalWalletCallbackMapper = digitalWalletCallbackMapper;
    }

    @Override
    public void newDigitalWallet(DigitalWalletCallbackDto digitalWalletCallbackDto) {
        DigitalWalletCallback digitalWalletCallback = digitalWalletCallbackMapper
                .digitalWalletCallbackDtoToDigitalWalletCallback(digitalWalletCallbackDto);
        digitalWalletCallback.setProcessStatus(PaymentCallbackStatusEnum.RECEIVED);
        digitalWalletCallbackRepository.save(digitalWalletCallback);
        findTransaction(digitalWalletCallback.getId());
    }

    @Transactional
    @Override
    public void findTransaction(Long digitalWalletCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> sm = build(digitalWalletCallbackId);
        sendEvent(digitalWalletCallbackId, sm, DigitalWalletStatusEvent.FIND_TRANSACTION);
    }

    @Transactional
    @Override
    public void issuedTransaction(Long digitalWalletCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> sm = build(digitalWalletCallbackId);
        sendEvent(digitalWalletCallbackId, sm, DigitalWalletStatusEvent.ISSUED);
    }

    @Transactional
    @Override
    public void failedTransaction(Long digitalWalletCallbackId) {
        StateMachine<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> sm = build(digitalWalletCallbackId);
        sendEvent(digitalWalletCallbackId, sm, DigitalWalletStatusEvent.ISSUED_FAILED);
    }

    private void sendEvent(Long digitalWalletCallbackId,
                           StateMachine<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> sm, DigitalWalletStatusEvent event) {
        Message<DigitalWalletStatusEvent> message = MessageBuilder.withPayload(event)
                .setHeader(DIGITAL_WALLET_CALLBACK_HEADER, digitalWalletCallbackId).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> build(Long digitalWalletCallbackId) {
        DigitalWalletCallback digitalWalletCallback = digitalWalletCallbackRepository
                .getById(digitalWalletCallbackId);

        StateMachine<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> sm = stateMachineFactory
                .getStateMachine(Long.toString(digitalWalletCallback.getId()));

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(digitalWalletCallbackInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                    digitalWalletCallback.getProcessStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }
}
