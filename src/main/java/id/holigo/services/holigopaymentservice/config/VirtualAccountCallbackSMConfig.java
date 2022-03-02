package id.holigo.services.holigopaymentservice.config;

import java.util.EnumSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import id.holigo.services.common.model.PaymentStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentCallbackStatusEnum;
import id.holigo.services.holigopaymentservice.domain.PaymentVirtualAccount;
import id.holigo.services.holigopaymentservice.domain.VirtualAccountCallback;
import id.holigo.services.holigopaymentservice.events.VirtualAccountStatusEvent;
import id.holigo.services.holigopaymentservice.repositories.PaymentVirtualAccountRepository;
import id.holigo.services.holigopaymentservice.repositories.VirtualAccountCallbackRepository;
import id.holigo.services.holigopaymentservice.services.PaymentVirtualAccountService;
import id.holigo.services.holigopaymentservice.services.VirtualAccountCallbackServiceImpl;
// import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @RequiredArgsConstructor
@Slf4j
@EnableStateMachineFactory(name = "virtualAccountCallbackSMF")
@Configuration
public class VirtualAccountCallbackSMConfig
        extends StateMachineConfigurerAdapter<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> {

    @Autowired
    private VirtualAccountCallbackRepository virtualAccountCallbackRepository;

    @Autowired
    private PaymentVirtualAccountRepository paymentVirtualAccountRepository;

    @Autowired
    private PaymentVirtualAccountService paymentVirtualAccountService;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> states)
            throws Exception {
        states.withStates().initial(PaymentCallbackStatusEnum.RECEIVED)
                .states(EnumSet.allOf(PaymentCallbackStatusEnum.class))
                .end(PaymentCallbackStatusEnum.ISSUED)
                .end(PaymentCallbackStatusEnum.TRANSACTION_NOT_FOUND)
                .end(PaymentCallbackStatusEnum.ISSUED_FAILED);
    }

    @Override
    public void configure(
            StateMachineTransitionConfigurer<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> transitions)
            throws Exception {
        transitions.withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.RECEIVED)
                .event(VirtualAccountStatusEvent.FIND_TRANSACTION)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                // .action(processIssuedAction())
                .event(VirtualAccountStatusEvent.PROCESS_ISSUED)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.RECEIVED)
                .target(PaymentCallbackStatusEnum.TRANSACTION_NOT_FOUND)
                // .action(transactionNotFoundAction())
                .event(VirtualAccountStatusEvent.TRANSACTION_NOT_FOUND)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                .target(PaymentCallbackStatusEnum.ISSUED)
                .event(VirtualAccountStatusEvent.ISSUED)
                .and()
                .withExternal().source(PaymentCallbackStatusEnum.PROCESS_ISSUED)
                .target(PaymentCallbackStatusEnum.ISSUED_FAILED)
                .event(VirtualAccountStatusEvent.ISSUED_FAILED);

    }

    @Override
    public void configure(
            StateMachineConfigurationConfigurer<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> config)
            throws Exception {
        StateMachineListenerAdapter<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> adapter = new StateMachineListenerAdapter<>() {
            @Override
            public void stateChanged(State<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> from,
                    State<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> to) {
                log.info(String.format("stateChange(from: %s, to %s)", from, to));
            }
        };
        config.withConfiguration().listener(adapter);
    }

    public Action<PaymentCallbackStatusEnum, VirtualAccountStatusEvent> receivedAction() {
        return context -> {
            log.info("receivedAction was called");
            log.info("header -> {}",
                    context.getMessageHeader(
                            VirtualAccountCallbackServiceImpl.VIRTUAL_ACCOUNT_CALLBACK_HEADER));
            VirtualAccountCallback virtualAccountCallback = virtualAccountCallbackRepository
                    .getById(Long.valueOf(
                            context.getMessageHeader(
                                    VirtualAccountCallbackServiceImpl.VIRTUAL_ACCOUNT_CALLBACK_HEADER)
                                    .toString()));
            log.info("bank Transfer callback -> {}",
                    virtualAccountCallback);

            Optional<PaymentVirtualAccount> fetchPaymentVirtualAccount = paymentVirtualAccountRepository
                    .findByAccountNumberAndStatus(virtualAccountCallback.getAccountNumber(),
                            PaymentStatusEnum.WAITING_PAYMENT);
            if (fetchPaymentVirtualAccount.isPresent()) {
                log.info("Payment found");
                context.getStateMachine()
                        .sendEvent(MessageBuilder
                                .withPayload(VirtualAccountStatusEvent.PROCESS_ISSUED)
                                .setHeader(VirtualAccountCallbackServiceImpl.VIRTUAL_ACCOUNT_CALLBACK_HEADER,
                                        virtualAccountCallback.getId())
                                .build());
                PaymentVirtualAccount paymentVirtualAccount = fetchPaymentVirtualAccount.get();
                paymentVirtualAccount.setCallbackId(virtualAccountCallback.getId());
                paymentVirtualAccountRepository.save(paymentVirtualAccount);

                paymentVirtualAccountService.paymentHasBeenPaid(paymentVirtualAccount.getId());

            } else {
                log.info("Payment not found");
                context.getStateMachine()
                        .sendEvent(MessageBuilder.withPayload(
                                VirtualAccountStatusEvent.TRANSACTION_NOT_FOUND)
                                .setHeader(VirtualAccountCallbackServiceImpl.VIRTUAL_ACCOUNT_CALLBACK_HEADER,
                                        virtualAccountCallback.getId())
                                .build());

            }
        };
    }

}
