package id.holigo.services.holigopaymentservice.services;

import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.VirtualAccountCallback;
import id.holigo.services.holigopaymentservice.events.VirtualAccountStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.VirtualAccountCallbackRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class VirtualAccountCallbackInterceptor
        extends StateMachineInterceptorAdapter<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> {

    private final VirtualAccountCallbackRepository virtualAccountCallbackRepository;

    @Override
    public void preStateChange(State<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> state,
            Message<VirtualAccountStatusEvent> message,
            Transition<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> transition,
            StateMachine<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> stateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(Long.class.cast(
                    msg.getHeaders().getOrDefault(VirtualAccountCallbackServiceImpl.VIRTUAL_ACCOUNT_CALLBACK_HEADER,
                            1L)))
                    .ifPresent(virtualAccountCallbackId -> {
                        VirtualAccountCallback virtualAccountCallback = virtualAccountCallbackRepository
                                .getById(virtualAccountCallbackId);
                        virtualAccountCallback.setProcessStatus(state.getId());
                        virtualAccountCallbackRepository.save(virtualAccountCallback);
                    });
        });
    }
}
