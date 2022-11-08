package id.holigo.services.holigopaymentservice.interceptors;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.domain.PaymentDigitalWallet;
import id.holigo.services.holigopaymentservice.events.PaymentDigitalWalletEvent;
import id.holigo.services.holigopaymentservice.events.PaymentDigitalWalletEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentDigitalWalletRepository;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.PaymentDigitalWalletServiceImpl;
import id.holigo.services.holigopaymentservice.services.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PaymentDigitalWalletInterceptor extends StateMachineInterceptorAdapter<PaymentStatusEnum, PaymentDigitalWalletEvent> {
    private final PaymentDigitalWalletRepository paymentDigitalWalletRepository;

    @Override
    public void preStateChange(State<PaymentStatusEnum, PaymentDigitalWalletEvent> state, Message<PaymentDigitalWalletEvent> message,
                               Transition<PaymentStatusEnum, PaymentDigitalWalletEvent> transition,
                               StateMachine<PaymentStatusEnum, PaymentDigitalWalletEvent> stateMachine) {
        Optional.ofNullable(message).ifPresent(msg -> {
            UUID id = UUID.fromString(Objects.requireNonNull(msg.getHeaders()
                    .get(PaymentDigitalWalletServiceImpl.PAYMENT_DIGITAL_WALLET_HEADER)).toString());
            PaymentDigitalWallet paymentDigitalWallet = paymentDigitalWalletRepository
                    .getById(id);
            paymentDigitalWallet.setStatus(state.getId());
            paymentDigitalWalletRepository.save(paymentDigitalWallet);
        });
    }

}
