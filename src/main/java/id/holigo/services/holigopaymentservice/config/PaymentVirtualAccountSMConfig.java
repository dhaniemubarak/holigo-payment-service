package id.holigo.services.holigopaymentservice.config;

import java.util.EnumSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.Payment;
import id.holigo.services.holigopaymentservice.events.PaymentStatusEvent;
import id.holigo.services.holigopaymentservice.events.PaymentVirtualAccountEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.interceptors.PaymentInterceptor;
import id.holigo.services.holigopaymentservice.services.PaymentServiceImpl;
import id.holigo.services.holigopaymentservice.services.PaymentVirtualAccountServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@EnableStateMachineFactory(name = "paymentVirtualAccountSMF")
@Configuration
public class PaymentVirtualAccountSMConfig
        extends StateMachineConfigurerAdapter<PaymentStatusEnum, PaymentVirtualAccountEvent> {

    private PaymentRepository paymentRepository;

    private final StateMachineFactory<PaymentStatusEnum, PaymentStatusEvent> stateMachineFactory;

    private PaymentInterceptor paymentInterceptor;

    @Autowired
    public void setPaymentInterceptor(PaymentInterceptor paymentInterceptor) {
        this.paymentInterceptor = paymentInterceptor;
    }

    @Autowired
    public void setPaymentRepository(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

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
                .target(PaymentStatusEnum.PAID).action(paymentPaidAction())
                .event(PaymentVirtualAccountEvent.PAYMENT_PAID)
                .and().withExternal().source(PaymentStatusEnum.WAITING_PAYMENT)
                .target(PaymentStatusEnum.PAYMENT_CANCELED).event(PaymentVirtualAccountEvent.PAYMENT_CANCELED)
                .and().withExternal().source(PaymentStatusEnum.WAITING_PAYMENT)
                .target(PaymentStatusEnum.PAYMENT_EXPIRED).event(PaymentVirtualAccountEvent.PAYMENT_EXPIRED);
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

    public Action<PaymentStatusEnum, PaymentVirtualAccountEvent> paymentPaidAction() {
        return context -> {
            Optional<Payment> fetchPayment = paymentRepository.findByDetailTypeAndDetailId("virtualAccount",
                    context.getMessageHeader(
                                    PaymentVirtualAccountServiceImpl.PAYMENT_VIRTUAL_ACCOUNT_HEADER)
                            .toString());
            if (fetchPayment.isPresent()) {
                Payment payment = fetchPayment.get();
                StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = build(payment);
                sm.sendEvent(MessageBuilder
                        .withPayload(PaymentStatusEvent.PAYMENT_PAID)
                        .setHeader(PaymentServiceImpl.PAYMENT_HEADER,
                                payment.getId().toString())
                        .build());

            }
        };
    }

    private StateMachine<PaymentStatusEnum, PaymentStatusEvent> build(Payment payment) {
        StateMachine<PaymentStatusEnum, PaymentStatusEvent> sm = stateMachineFactory
                .getStateMachine(payment.getId().toString());

        sm.stop();
        sm.getStateMachineAccessor().doWithAllRegions(sma -> {
            sma.addStateMachineInterceptor(paymentInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(
                    payment.getStatus(), null, null, null));
        });
        sm.start();
        return sm;
    }
}
