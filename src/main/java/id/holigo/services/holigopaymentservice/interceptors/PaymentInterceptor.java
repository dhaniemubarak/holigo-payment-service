package id.holigo.services.holigopaymentservice.interceptors;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import id.holigo.services.holigopaymentservice.services.PaymentServiceImpl;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.events.PaymentStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PaymentInterceptor extends StateMachineInterceptorAdapter<PaymentStatusEnum, PaymentStatusEvent> {
    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentStatusEnum, PaymentStatusEvent> state, Message<PaymentStatusEvent> message,
                               Transition<PaymentStatusEnum, PaymentStatusEvent> transition,
                               StateMachine<PaymentStatusEnum, PaymentStatusEvent> stateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            UUID id = UUID.fromString(Objects.requireNonNull(msg.getHeaders()
                    .get(PaymentServiceImpl.PAYMENT_HEADER)).toString());
            Payment payment = paymentRepository
                    .getById(id);
            payment.setStatus(state.getId());
            paymentRepository.save(payment);
        });
    }

}
