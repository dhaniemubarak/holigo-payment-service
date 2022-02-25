package id.holigo.services.holigopaymentservice.config;

import java.util.EnumSet;
import java.util.Optional;

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
import id.holigo.services.holigopaymentservice.events.PaymentBankTransferEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentRepository;
import id.holigo.services.holigopaymentservice.services.PaymentBankTransferServiceImpl;
// import id.holigo.services.holigopaymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@EnableStateMachineFactory(name = "paymentBankTrasnferSMF")
@Configuration
public class PaymentBankTransferSMConfig
        extends StateMachineConfigurerAdapter<PaymentStatusEnum, PaymentBankTransferEvent> {

    @Autowired
    private final PaymentRepository paymentRepository;

    // @Autowired
    // private final PaymentService paymentService;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentStatusEnum, PaymentBankTransferEvent> states)
            throws Exception {
        states.withStates().initial(PaymentStatusEnum.WAITING_PAYMENT)
                .states(EnumSet.allOf(PaymentStatusEnum.class))
                .end(PaymentStatusEnum.PAID)
                .end(PaymentStatusEnum.PAYMENT_CANCELED)
                .end(PaymentStatusEnum.PAYMENT_EXPIRED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentStatusEnum, PaymentBankTransferEvent> transitions)
            throws Exception {

        transitions.withExternal().source(PaymentStatusEnum.WAITING_PAYMENT)
                .target(PaymentStatusEnum.PAID).action(paymentPaidAction())
                .event(PaymentBankTransferEvent.PAYMENT_PAID);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentStatusEnum, PaymentBankTransferEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentStatusEnum, PaymentBankTransferEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentStatusEnum, PaymentBankTransferEvent> from,
                    State<PaymentStatusEnum, PaymentBankTransferEvent> to) {
                log.info(String.format("stateChange(from: %s, to %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    public Action<PaymentStatusEnum, PaymentBankTransferEvent> paymentPaidAction() {
        return context -> {
            log.info("paymentPaidAction in PaymentBankTransferSMConfig is running....");
            Optional<Payment> fetchPayment = paymentRepository.findByDetailTypeAndDetailId("bankTransfer",
                    context.getMessageHeader(PaymentBankTransferServiceImpl.PAYMENT_BANK_TRANSFER_HEADER).toString());
            if (fetchPayment.isPresent()) {
                log.info("payment found.");
                log.info("Calling payment service for isseud");
                Payment payment = fetchPayment.get();
                log.info("Payment -> {}", payment);
                // paymentService.paymentHasBeenPaid(payment.getId());
            } else {
                log.info("Payment not found");
            }
        };
    }
}
