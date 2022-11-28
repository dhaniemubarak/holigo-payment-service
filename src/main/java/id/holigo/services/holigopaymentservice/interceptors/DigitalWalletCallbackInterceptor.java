package id.holigo.services.holigopaymentservice.interceptors;

import id.holigo.services.holigopaymentservice.domain.DigitalWalletCallback;
import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.VirtualAccountCallback;
import id.holigo.services.holigopaymentservice.events.DigitalWalletStatusEvent;
import id.holigo.services.holigopaymentservice.events.DigitalWalletStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.DigitalWalletCallbackRepository;
import id.holigo.services.holigopaymentservice.repositories.VirtualAccountCallbackRepository;
import id.holigo.services.holigopaymentservice.services.DigitalWalletCallbackServiceImpl;
import id.holigo.services.holigopaymentservice.services.VirtualAccountCallbackServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DigitalWalletCallbackInterceptor
        extends StateMachineInterceptorAdapter<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> {

    private final DigitalWalletCallbackRepository digitalWalletCallbackRepository;

    @Override
    public void preStateChange(State<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> state,
                               Message<DigitalWalletStatusEvent> message,
                               Transition<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> transition,
                               StateMachine<PaymentCallbackStatusEnum, DigitalWalletStatusEvent> stateMachine) {
        Optional.ofNullable(message).flatMap(msg -> Optional.ofNullable((Long) msg.getHeaders().getOrDefault(DigitalWalletCallbackServiceImpl.DIGITAL_WALLET_CALLBACK_HEADER,
                1L))).ifPresent(digitalWalletCallbackId -> {
            DigitalWalletCallback digitalWalletCallback = digitalWalletCallbackRepository
                    .getById(digitalWalletCallbackId);
            digitalWalletCallback.setProcessStatus(state.getId());
            digitalWalletCallbackRepository.save(digitalWalletCallback);
        });
    }
}
