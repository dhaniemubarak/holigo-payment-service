package id.holigo.services.holigopaymentservice.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.events.PaymentVirtualAccountEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentVirtualAccountRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PaymentVirtualAccountInterceptor
        extends StateMachineInterceptorAdapter<PaymentStatusEnum, PaymentVirtualAccountEvent> {

    private final PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    @Override
    public void preStateChange(State<PaymentStatusEnum, PaymentVirtualAccountEvent> state,
            Message<PaymentVirtualAccountEvent> message,
            Transition<PaymentStatusEnum, PaymentVirtualAccountEvent> transition,
            StateMachine<PaymentStatusEnum, PaymentVirtualAccountEvent> stateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(
                    UUID.class.cast(UUID.fromString(msg.getHeaders()
                            .get(PaymentVirtualAccountServiceImpl.PAYMENT_VIRTUAL_ACCOUNT_HEADER).toString())))
                    .ifPresent(id -> {
                        PaymentVirtualAccount paymentVirtualAccount = paymentVirtualAccountRepository.getById(id);
                        paymentVirtualAccount.setStatus(state.getId());
                        paymentVirtualAccountRepository.save(paymentVirtualAccount);
                    });
        });
    }
}
