package id.holigo.services.holigopaymentservice.config;

import java.util.EnumSet;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.events.PaymentVirtualAccountEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@EnableStateMachineFactory(name = "paymentVirtualAccountSMF")
@Configuration
public class PaymentVirtualAccountSMConfig
        extends StateMachineConfigurerAdapter<PaymentStatusEnum, PaymentVirtualAccountEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<PaymentStatusEnum, PaymentVirtualAccountEvent> states)
            throws Exception {

        states.withStates().initial(PaymentStatusEnum.WAITING_PAYMENT)
                .states(EnumSet.allOf(PaymentStatusEnum.class))
                .end(PaymentStatusEnum.PAID)
                .end(PaymentStatusEnum.PAYMENT_CANCELED)
                .end(PaymentStatusEnum.PAYMENT_EXPIRED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentStatusEnum, PaymentVirtualAccountEvent> transitions)
            throws Exception {
        transitions.withExternal().source(PaymentStatusEnum.WAITING_PAYMENT)
                .target(PaymentStatusEnum.PAID).event(PaymentVirtualAccountEvent.PAYMENT_PAID);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentStatusEnum, PaymentVirtualAccountEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentStatusEnum, PaymentVirtualAccountEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentStatusEnum, PaymentVirtualAccountEvent> from,
                    State<PaymentStatusEnum, PaymentVirtualAccountEvent> to) {
                log.info(String.format("stateChange(from: %s, to %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }
}
