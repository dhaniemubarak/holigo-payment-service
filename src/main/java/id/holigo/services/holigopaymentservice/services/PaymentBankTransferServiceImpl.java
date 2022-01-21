package id.holigo.services.holigopaymentservice.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.events.PaymentBankTransferEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PaymentBankTransferServiceImpl implements PaymentBankTransferService {

    public static final String PAYMENT_BANK_TRANSFER_HEADER = "payment_bank_transfer_id";

    @Autowired
    private final PaymentBankTransferRepository paymentBankTransferRepository;

    private final StateMachineFactory<PaymentStatusEnum, PaymentBankTransferEvent> stateMachineFactory;

    private final PaymentBankTransferInterceptor paymentBankTransferInterceptor;

    @Transactional
    @Override
    public StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> paymentHasBeenPaid(UUID id) {
        StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> sm = build(id);
        sendEvent(id, sm, PaymentBankTransferEvent.PAYMENT_PAID);
        return sm;
    }

    private void sendEvent(UUID id,
            StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> sm, PaymentBankTransferEvent event) {
        Message<PaymentBankTransferEvent> message = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_BANK_TRANSFER_HEADER, id).build();
        sm.sendEvent(message);
    }

    private StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> build(UUID id) {
        PaymentBankTransfer paymentBankTransfer = paymentBankTransferRepository.getById(id);

        StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> sm = stateMachineFactory
                .getStateMachine(paymentBankTransfer.getId().toString());

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(paymentBankTransferInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<PaymentStatusEnum, PaymentBankTransferEvent>(
                    paymentBankTransfer.getStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }

}
