package id.holigo.services.holigopaymentservice.services;

import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import id.holigo.services.holigopaymentservice.domain.BankTransferCallback;
import id.holigo.services.holigopaymentservice.domain.BankTransferStatusEnum;
import id.holigo.services.holigopaymentservice.domain.BankTransferStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.BankTransferCallbackRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class BankTransferCallbackInterceptor
        extends StateMachineInterceptorAdapter<BankTransferStatusEnum, BankTransferStatusEvent> {

    private final BankTransferCallbackRepository bankTransferCallbackRepository;

    @Override
    public void preStateChange(State<BankTransferStatusEnum, BankTransferStatusEvent> state,
            Message<BankTransferStatusEvent> message,
            Transition<BankTransferStatusEnum, BankTransferStatusEvent> transition,
            StateMachine<BankTransferStatusEnum, BankTransferStatusEvent> stateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(Long.class.cast(
                    msg.getHeaders().getOrDefault(BankTransferCallbackServiceImpl.BANK_TRANSFER_CALLBEACK_HEADER, 1L)))
                    .ifPresent(bankTransferCallbackId -> {
                        BankTransferCallback bankTransferCallback = bankTransferCallbackRepository
                                .getById(bankTransferCallbackId);
                        bankTransferCallback.setProcessStatus(state.getId());
                        bankTransferCallbackRepository.save(bankTransferCallback);
                    });
        });
    }
}
