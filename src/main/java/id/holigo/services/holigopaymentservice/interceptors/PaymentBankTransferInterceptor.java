package id.holigo.services.holigopaymentservice.interceptors;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import id.holigo.services.holigopaymentservice.services.PaymentBankTransferServiceImpl;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentBankTransfer;
import id.holigo.services.holigopaymentservice.events.PaymentBankTransferEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentBankTransferRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PaymentBankTransferInterceptor
        extends StateMachineInterceptorAdapter<PaymentStatusEnum, PaymentBankTransferEvent> {

    private final PaymentBankTransferRepository paymentBankTransferRepository;

    @Override
    public void preStateChange(State<PaymentStatusEnum, PaymentBankTransferEvent> state,
                               Message<PaymentBankTransferEvent> message,
                               Transition<PaymentStatusEnum, PaymentBankTransferEvent> transition,
                               StateMachine<PaymentStatusEnum, PaymentBankTransferEvent> stateMachine) {
        Optional.ofNullable(message).flatMap(msg -> Optional.of(
                UUID.fromString(Objects.requireNonNull(msg.getHeaders()
                        .get(PaymentBankTransferServiceImpl.PAYMENT_BANK_TRANSFER_HEADER)).toString()))).ifPresent(id -> {
            PaymentBankTransfer paymentBankTransfer = paymentBankTransferRepository
                    .getById(id);
            paymentBankTransfer.setStatus(state.getId());
            paymentBankTransferRepository.save(paymentBankTransfer);
        });
    }
}
