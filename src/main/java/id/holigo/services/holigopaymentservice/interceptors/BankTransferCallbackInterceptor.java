package id.holigo.services.holigopaymentservice.interceptors;

import java.util.Optional;

import id.holigo.services.holigopaymentservice.services.BankTransferCallbackServiceImpl;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.events.BankTransferStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.BankTransferCallbackRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BankTransferCallbackInterceptor
        extends StateMachineInterceptorAdapter<PaymentCallbackStatusEnum, BankTransferStatusEvent> {

    private final BankTransferCallbackRepository bankTransferCallbackRepository;

    @Override
    public void preStateChange(State<PaymentCallbackStatusEnum, BankTransferStatusEvent> state,
                               Message<BankTransferStatusEvent> message,
                               Transition<PaymentCallbackStatusEnum, BankTransferStatusEvent> transition,
                               StateMachine<PaymentCallbackStatusEnum, BankTransferStatusEvent> stateMachine) {
        Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable((Long) msg.getHeaders().getOrDefault(BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBACK_HEADER, 1L))).ifPresent(bankTransferCallbackId -> {
            BankTransferCallback bankTransferCallback = bankTransferCallbackRepository
                    .getById(bankTransferCallbackId);
            bankTransferCallback.setProcessStatus(state.getId());
            bankTransferCallbackRepository.save(bankTransferCallback);
        });
    }
}
