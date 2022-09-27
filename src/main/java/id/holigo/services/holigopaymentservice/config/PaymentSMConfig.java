package id.holigo.services.holigopaymentservice.config;

import java.util.EnumSet;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.events.PaymentStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.PaymentServiceImpl;
import id.holigo.services.holigopaymentservice.services.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@EnableStateMachineFactory(name = "paymentSMF")
@Configuration
public class PaymentSMConfig extends StateMachineConfigurerAdapter<PaymentStatusEnum, PaymentStatusEvent> {

    @Autowired
    private final TransactionService transactionService;

    @Autowired
    private final PaymentRepository paymentRepository;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentStatusEnum, PaymentStatusEvent> states)
            throws Exception {
        states.withStates().initial(PaymentStatusEnum.WAITING_PAYMENT)
                .states(EnumSet.allOf(PaymentStatusEnum.class))
                .end(PaymentStatusEnum.PAYMENT_CANCELED)
                .end(PaymentStatusEnum.PAYMENT_EXPIRED)
                .end(PaymentStatusEnum.REFUNDED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentStatusEnum, PaymentStatusEvent> transitions)
            throws Exception {
        transitions.withExternal().source(PaymentStatusEnum.WAITING_PAYMENT).target(PaymentStatusEnum.PAID)
                .event(PaymentStatusEvent.PAYMENT_PAID).action(paidAction())
                .and().withExternal().source(PaymentStatusEnum.WAITING_PAYMENT).target(PaymentStatusEnum.PAYMENT_EXPIRED)
                .event(PaymentStatusEvent.PAYMENT_EXPIRED)
                .and().withExternal().source(PaymentStatusEnum.PAID).target(PaymentStatusEnum.REFUNDED)
                .event(PaymentStatusEvent.PAYMENT_REFUND);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentStatusEnum, PaymentStatusEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentStatusEnum, PaymentStatusEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentStatusEnum, PaymentStatusEvent> from,
                                     State<PaymentStatusEnum, PaymentStatusEvent> to) {
                log.info(String.format("stateChange(from: %s, to %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    public Action<PaymentStatusEnum, PaymentStatusEvent> paidAction() {
        return context -> {
            Payment payment = paymentRepository
                    .getById(UUID.fromString(context.getMessageHeader(PaymentServiceImpl.PAYMENT_HEADER).toString()));
            transactionService.issuedTransaction(payment.getTransactionId(), payment);
        };
    }
}
